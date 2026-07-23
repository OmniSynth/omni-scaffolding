package com.omni.scaffolding.modules.system.service;

import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.api.PageQuery;
import com.omni.scaffolding.common.api.PageResult;
import com.omni.scaffolding.common.cache.CacheKeys;
import com.omni.scaffolding.common.cache.CacheNames;
import com.omni.scaffolding.common.cache.RedisKeys;
import com.omni.scaffolding.common.exception.BusinessException;
import com.omni.scaffolding.common.util.IdGenerator;
import com.omni.scaffolding.config.OmniSecurityProperties;
import com.omni.scaffolding.modules.system.dto.ip.IpVisitItemView;
import com.omni.scaffolding.modules.system.dto.ip.IpVisitTodayView;
import com.omni.scaffolding.modules.system.dto.ip.IpWhitelistSaveRequest;
import com.omni.scaffolding.modules.system.dto.ip.IpWhitelistView;
import com.omni.scaffolding.modules.system.entity.SysIpWhitelist;
import com.omni.scaffolding.modules.system.mapper.SysIpWhitelistQueryMapper;
import com.omni.scaffolding.modules.system.repository.SysIpWhitelistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * IP 白名单领域服务。
 *
 * <ul>
 *   <li>管理端：CRUD、启停、手动刷新 {@link CacheNames#IP_WHITELIST} 缓存</li>
 *   <li>运行时：供切面校验客户端 IP 是否放行</li>
 *   <li>统计：放行后按日累加访问次数（Redis，见 {@link RedisKeys}）</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class IpWhitelistService {

    private static final DateTimeFormatter DAY = DateTimeFormatter.BASIC_ISO_DATE;

    private final SysIpWhitelistRepository whitelistRepository;
    private final SysIpWhitelistQueryMapper whitelistQueryMapper;
    private final OmniSecurityProperties securityProperties;
    private final StringRedisTemplate stringRedisTemplate;
    private final CacheManager cacheManager;

    /**
     * 分页查询白名单。
     *
     * @param keyword 可选，匹配 IP / 备注
     * @param status  可选，启停状态
     * @param page    页码，从 1 开始
     * @param size    每页条数
     * @return 分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<IpWhitelistView> list(String keyword, Boolean status, Long page, Long size) {
        PageQuery pq = PageQuery.of(page, size);
        long total = whitelistQueryMapper.count(keyword, status);
        if (total == 0) {
            return pq.toResult(0, List.of());
        }
        return pq.toResult(total, whitelistQueryMapper.list(keyword, status, pq.getSize(), pq.getOffset()));
    }

    /**
     * 查询白名单详情。
     *
     * @param id 主键
     * @return 读模型
     * @throws BusinessException 记录不存在时抛出
     */
    @Transactional(readOnly = true)
    public IpWhitelistView detail(Long id) {
        IpWhitelistView view = whitelistQueryMapper.findById(id);
        if (view == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "白名单记录不存在");
        }
        return view;
    }

    /**
     * 新增白名单；若同 IP 曾软删则恢复该行。
     *
     * @param request 保存请求
     * @return 新建后的读模型
     * @throws BusinessException IP 已存在或非法时抛出
     */
    @Transactional
    @CacheEvict(cacheNames = CacheNames.IP_WHITELIST, allEntries = true)
    public IpWhitelistView create(IpWhitelistSaveRequest request) {
        String ip = normalize(request.getIpAddr());
        validateIp(ip);
        SysIpWhitelist existing = whitelistRepository.findByIpAddr(ip).orElse(null);
        if (existing != null && existing.getDeleted() != null && existing.getDeleted() == 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "该 IP 已在白名单中");
        }
        SysIpWhitelist entity;
        if (existing != null) {
            entity = existing;
            entity.setDeleted(0);
        } else {
            entity = new SysIpWhitelist();
            entity.setId(IdGenerator.nextId());
            entity.setDeleted(0);
            entity.setIpAddr(ip);
        }
        applyMutable(entity, request, ip);
        whitelistRepository.saveAndFlush(entity);
        return detail(entity.getId());
    }

    /**
     * 修改白名单；唯一键冲突时会释放已软删占用。
     *
     * @param id      主键
     * @param request 保存请求
     * @return 更新后的读模型
     */
    @Transactional
    @CacheEvict(cacheNames = CacheNames.IP_WHITELIST, allEntries = true)
    public IpWhitelistView update(Long id, IpWhitelistSaveRequest request) {
        SysIpWhitelist entity = requireEntity(id);
        String ip = normalize(request.getIpAddr());
        validateIp(ip);
        if (whitelistRepository.existsByIpAddrAndDeletedAndIdNot(ip, 0, id)) {
            throw new BusinessException(ErrorCode.CONFLICT, "该 IP 已在白名单中");
        }
        // 唯一键在整表生效：若目标 IP 曾软删，先释放占用
        whitelistRepository.findByIpAddr(ip).ifPresent(other -> {
            if (!other.getId().equals(id) && other.getDeleted() != null && other.getDeleted() != 0) {
                other.setIpAddr(ip + "#deleted#" + other.getId());
                whitelistRepository.saveAndFlush(other);
            }
        });
        applyMutable(entity, request, ip);
        whitelistRepository.saveAndFlush(entity);
        return detail(id);
    }

    /**
     * 启用或停用白名单记录。
     *
     * @param id      主键
     * @param enabled {@code true} 启用，{@code false} 停用
     * @return 更新后的读模型
     */
    @Transactional
    @CacheEvict(cacheNames = CacheNames.IP_WHITELIST, allEntries = true)
    public IpWhitelistView changeStatus(Long id, boolean enabled) {
        SysIpWhitelist entity = requireEntity(id);
        entity.setStatus(enabled);
        whitelistRepository.saveAndFlush(entity);
        return detail(id);
    }

    /**
     * 逻辑删除白名单记录。
     *
     * @param id 主键
     */
    @Transactional
    @CacheEvict(cacheNames = CacheNames.IP_WHITELIST, allEntries = true)
    public void remove(Long id) {
        SysIpWhitelist entity = requireEntity(id);
        entity.setDeleted(1);
        entity.setStatus(false);
        whitelistRepository.save(entity);
    }

    /**
     * 手动刷新 IP 白名单缓存（清空 {@link CacheNames#IP_WHITELIST}）。
     */
    @CacheEvict(cacheNames = CacheNames.IP_WHITELIST, allEntries = true)
    public void refreshCache() {
        // Spring Cache 代理触发失效
    }

    /**
     * 判断客户端 IP 是否允许访问。
     *
     * <p>合并注解额外 IP、表内启用 IP；表为空时回退 yaml 兜底配置。
     * 本机 IPv4 / IPv6 互通（任一本机地址在白名单即放行）。
     *
     * @param clientIp      客户端 IP
     * @param annotationIps 注解上的额外 IP，可为 {@code null}
     * @return 允许则为 {@code true}
     */
    public boolean isAllowed(String clientIp, String[] annotationIps) {
        if (!StringUtils.hasText(clientIp)) {
            return false;
        }
        Set<String> allowed = loadWhitelist(annotationIps);
        if (allowed.isEmpty()) {
            return false;
        }
        String ip = normalize(clientIp);
        if (allowed.contains(ip)) {
            return true;
        }
        if (isLoopback(ip)) {
            return allowed.stream().anyMatch(IpWhitelistService::isLoopback);
        }
        return false;
    }

    /**
     * 放行后累加该 IP 今日访问次数。
     *
     * @param clientIp 客户端 IP
     * @return 该 IP 当日累计次数；IP 为空时返回 0
     */
    public long recordVisit(String clientIp) {
        String ip = normalize(clientIp);
        if (!StringUtils.hasText(ip)) {
            return 0L;
        }
        String day = today();
        String ipKey = RedisKeys.ipWhitelistVisit(day, ip);
        String totalKey = RedisKeys.ipWhitelistVisitTotal(day);
        Long count = stringRedisTemplate.opsForValue().increment(ipKey);
        Long total = stringRedisTemplate.opsForValue().increment(totalKey);
        Duration ttl = ttlUntilDayAfterTomorrow();
        if (count != null && count == 1L) {
            stringRedisTemplate.expire(ipKey, ttl);
        }
        if (total != null && total == 1L) {
            stringRedisTemplate.expire(totalKey, ttl);
        }
        return count == null ? 0L : count;
    }

    /**
     * 查询今日白名单接口访问统计。
     *
     * @return 含合计与按 IP 明细的视图
     */
    public IpVisitTodayView todayStats() {
        String day = today();
        IpVisitTodayView view = new IpVisitTodayView();
        view.setDate(day);
        view.setTotal(parseLong(stringRedisTemplate.opsForValue().get(RedisKeys.ipWhitelistVisitTotal(day))));

        List<IpVisitItemView> items = new ArrayList<>();
        String dayPrefix = RedisKeys.ipWhitelistVisitDayPrefix(day);
        ScanOptions options = ScanOptions.scanOptions().match(RedisKeys.ipWhitelistVisitDayPattern(day)).count(200).build();
        try (Cursor<String> cursor = stringRedisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                String key = cursor.next();
                String suffix = key.substring(dayPrefix.length());
                if (RedisKeys.IP_WHITELIST_VISIT_TOTAL_SUFFIX.equals(suffix)) {
                    continue;
                }
                items.add(new IpVisitItemView(suffix, parseLong(stringRedisTemplate.opsForValue().get(key))));
            }
        }
        items.sort(Comparator.comparingLong(IpVisitItemView::getCount).reversed());
        view.setItems(items);
        return view;
    }

    /**
     * 加载生效白名单集合：注解 IP ∪ 表内启用 IP；表空则回退 yaml。
     */
    private Set<String> loadWhitelist(String[] annotationIps) {
        Set<String> set = new LinkedHashSet<>();
        if (annotationIps != null) {
            for (String ip : annotationIps) {
                addAll(set, ip);
            }
        }
        for (String ip : enabledIps()) {
            String n = normalize(ip);
            if (StringUtils.hasText(n)) {
                set.add(n);
            }
        }
        if (set.isEmpty()) {
            addAll(set, securityProperties.getIpWhitelist());
        }
        return set;
    }

    /**
     * 读取启用 IP 列表（带 {@link CacheNames#IP_WHITELIST} 缓存）。
     */
    @SuppressWarnings("unchecked")
    private List<String> enabledIps() {
        Cache cache = cacheManager.getCache(CacheNames.IP_WHITELIST);
        if (cache != null) {
            List<String> cached = cache.get(CacheKeys.IP_WHITELIST_ENABLED, List.class);
            if (cached != null) {
                return cached;
            }
        }
        List<String> ips = whitelistQueryMapper.listEnabledIps();
        if (ips == null) {
            ips = List.of();
        }
        if (cache != null) {
            cache.put(CacheKeys.IP_WHITELIST_ENABLED, ips);
        }
        return ips;
    }

    /**
     * 按主键加载白名单实体；不存在则 404。
     *
     * @param id 白名单主键
     * @return 未删除的白名单实体
     */
    private SysIpWhitelist requireEntity(Long id) {
        return whitelistRepository.findByIdAndDeleted(id, 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "白名单记录不存在"));
    }

    /**
     * 将请求中的可变字段写入白名单实体。
     *
     * @param entity  目标实体
     * @param request 保存请求
     * @param ip      已规范化的 IP 地址
     */
    private static void applyMutable(SysIpWhitelist entity, IpWhitelistSaveRequest request, String ip) {
        entity.setIpAddr(ip);
        entity.setRemark(StringUtils.hasText(request.getRemark()) ? request.getRemark().trim() : null);
        entity.setStatus(Boolean.TRUE.equals(request.getStatus()));
    }

    /**
     * 校验 IP 非空且长度合法。
     *
     * @param ip 待校验 IP
     */
    private static void validateIp(String ip) {
        if (!StringUtils.hasText(ip)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "IP 不能为空");
        }
        if (ip.length() > 64) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "IP 长度不能超过 64");
        }
    }

    /**
     * 将逗号/分号/换行分隔的 IP 串解析后写入集合（已规范化）。
     *
     * @param set 目标集合
     * @param raw 原始 IP 串
     */
    private static void addAll(Set<String> set, String raw) {
        if (!StringUtils.hasText(raw)) {
            return;
        }
        for (String part : raw.split("[,\\n\\r;]+")) {
            String ip = normalize(part);
            if (StringUtils.hasText(ip)) {
                set.add(ip);
            }
        }
    }

    /**
     * 规范化 IP：去空白、去方括号、{@code localhost} → {@code 127.0.0.1}。
     */
    private static String normalize(String ip) {
        if (ip == null) {
            return "";
        }
        String trimmed = ip.trim();
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }
        if ("localhost".equalsIgnoreCase(trimmed)) {
            return "127.0.0.1";
        }
        return trimmed;
    }

    /**
     * 判断是否为本地回环地址（IPv4 / IPv6）。
     *
     * @param ip 规范化后的 IP
     * @return 回环地址返回 {@code true}
     */
    private static boolean isLoopback(String ip) {
        return "127.0.0.1".equals(ip) || "::1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip);
    }

    /**
     * 当前系统日期的 BASIC_ISO 格式字符串（用于 Redis 计数键）。
     *
     * @return 形如 {@code 20260722}
     */
    private static String today() {
        return LocalDate.now(ZoneId.systemDefault()).format(DAY);
    }

    /**
     * 访问计数 TTL：保留到后天零点，至少 1 小时。
     */
    private static Duration ttlUntilDayAfterTomorrow() {
        LocalDateTime end = LocalDate.now(ZoneId.systemDefault()).plusDays(2).atStartOfDay();
        long seconds = ChronoUnit.SECONDS.between(LocalDateTime.now(ZoneId.systemDefault()), end);
        return Duration.ofSeconds(Math.max(seconds, 3600));
    }

    /**
     * 安全解析长整型；空白或非法时返回 0。
     *
     * @param raw 原始字符串
     * @return 解析结果，失败时为 0
     */
    private static long parseLong(String raw) {
        if (!StringUtils.hasText(raw)) {
            return 0L;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }
}

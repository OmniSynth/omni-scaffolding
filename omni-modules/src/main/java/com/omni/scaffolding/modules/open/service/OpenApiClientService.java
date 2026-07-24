package com.omni.scaffolding.modules.open.service;

import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.api.PageQuery;
import com.omni.scaffolding.common.api.PageResult;
import com.omni.scaffolding.common.cache.RedisKeys;
import com.omni.scaffolding.common.exception.BusinessException;
import com.omni.scaffolding.common.util.DigestUtils;
import com.omni.scaffolding.common.util.IdGenerator;
import com.omni.scaffolding.common.util.UuidUtils;
import com.omni.scaffolding.modules.open.dto.client.OpenClientCredentialsView;
import com.omni.scaffolding.modules.open.dto.client.OpenClientSaveRequest;
import com.omni.scaffolding.modules.open.dto.client.OpenClientView;
import com.omni.scaffolding.modules.open.entity.OpenApiClient;
import com.omni.scaffolding.modules.open.mapper.OpenApiClientQueryMapper;
import com.omni.scaffolding.modules.open.repository.OpenApiClientRepository;
import com.omni.scaffolding.modules.open.repository.OpenApiEndpointRepository;
import com.omni.scaffolding.infra.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 开放 API 客户端服务。
 *
 * <p>负责签发 / 重置 Key（仅存哈希）、维护 IP 白名单与接口绑定、读取当日调用量。
 * 关联表写路径须在主表 {@code saveAndFlush} 之后执行。
 */
@Service
@RequiredArgsConstructor
public class OpenApiClientService {

    private static final DateTimeFormatter DAY = DateTimeFormatter.BASIC_ISO_DATE;
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    private final OpenApiClientRepository clientRepository;
    private final OpenApiEndpointRepository endpointRepository;
    private final OpenApiClientQueryMapper clientQueryMapper;
    private final RedisService redisService;

    /**
     * 客户端分页列表，并填充当日已用次数。
     *
     * @param keyword 可选关键字
     * @param status  可选启停
     * @param page    页码
     * @param size    每页条数
     * @return 分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<OpenClientView> list(String keyword, Boolean status, Long page, Long size) {
        PageQuery pq = PageQuery.of(page, size);
        long total = clientQueryMapper.countClients(keyword, status);
        if (total == 0) {
            return pq.toResult(0, List.of());
        }
        List<OpenClientView> records = clientQueryMapper.listClients(keyword, status, pq.getSize(), pq.getOffset());
        String today = LocalDate.now(ZONE).format(DAY);
        for (OpenClientView view : records) {
            view.setTodayUsed(readTodayUsed(view.getId(), today));
        }
        return pq.toResult(total, records);
    }

    /**
     * 客户端详情（含 IP、绑定接口、当日用量）。
     *
     * @param id 主键
     * @return 读模型
     */
    @Transactional(readOnly = true)
    public OpenClientView detail(Long id) {
        OpenClientView view = clientQueryMapper.findById(id);
        if (view == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "客户端不存在");
        }
        view.setIpList(clientQueryMapper.listIpsByClientId(id));
        view.setEndpointIds(clientQueryMapper.listEndpointIdsByClientId(id));
        view.setTodayUsed(readTodayUsed(id, LocalDate.now(ZONE).format(DAY)));
        return view;
    }

    /**
     * 创建客户端并签发密钥；返回一次性明文凭证。
     *
     * @param request 保存请求
     * @return 明文凭证
     */
    @Transactional
    public OpenClientCredentialsView create(OpenClientSaveRequest request) {
        OpenApiClient client = new OpenApiClient();
        client.setId(IdGenerator.nextId());
        applyMeta(client, request);
        client.setDeleted(0);

        String apiKey = "oak_" + UuidUtils.simpleUuid();
        String accessKey = "ak_" + UuidUtils.simpleUuid().substring(0, 16);
        String accessSecret = "sk_" + UuidUtils.simpleUuid();
        client.setApiKeyHash(DigestUtils.sha256Hex(apiKey));
        client.setAccessKey(accessKey);
        client.setSecretHash(DigestUtils.sha256Hex(accessSecret));

        // 须 flush：随后 MyBatis 写关联表
        clientRepository.saveAndFlush(client);
        replaceIps(client.getId(), request.getIpList());
        replaceEndpoints(client.getId(), request.getEndpointIds());

        return credentials(client.getId(), client.getName(), accessKey, apiKey, accessSecret);
    }

    /**
     * 更新元数据与关联；不轮换密钥。
     *
     * @param id      主键
     * @param request 保存请求
     * @return 更新后读模型
     */
    @Transactional
    public OpenClientView update(Long id, OpenClientSaveRequest request) {
        OpenApiClient client = clientRepository.findByIdAndDeleted(id, 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "客户端不存在"));
        applyMeta(client, request);
        clientRepository.saveAndFlush(client);
        replaceIps(id, request.getIpList());
        replaceEndpoints(id, request.getEndpointIds());
        return detail(id);
    }

    /**
     * 重置 API Key / AccessSecret；旧 Key 立即失效。
     *
     * @param id 主键
     * @return 新明文凭证
     */
    @Transactional
    public OpenClientCredentialsView resetKeys(Long id) {
        OpenApiClient client = clientRepository.findByIdAndDeleted(id, 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "客户端不存在"));
        String apiKey = "oak_" + UuidUtils.simpleUuid();
        String accessKey = "ak_" + UuidUtils.simpleUuid().substring(0, 16);
        String accessSecret = "sk_" + UuidUtils.simpleUuid();
        client.setApiKeyHash(DigestUtils.sha256Hex(apiKey));
        client.setAccessKey(accessKey);
        client.setSecretHash(DigestUtils.sha256Hex(accessSecret));
        clientRepository.saveAndFlush(client);
        return credentials(id, client.getName(), accessKey, apiKey, accessSecret);
    }

    /**
     * 逻辑删除客户端并清理关联。
     *
     * @param id 主键
     */
    @Transactional
    public void remove(Long id) {
        OpenApiClient client = clientRepository.findByIdAndDeleted(id, 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "客户端不存在"));
        client.setDeleted(1);
        clientRepository.saveAndFlush(client);
        clientQueryMapper.deleteIps(id);
        clientQueryMapper.deleteEndpoints(id);
    }

    /**
     * 读取 Redis 日计数（与 {@link com.omni.scaffolding.infra.ratelimit.RedisRateLimiter} 的 {@code rl:} 前缀对齐）。
     */
    private Long readTodayUsed(Long clientId, String day) {
        String raw = redisService.get("rl:" + RedisKeys.openApiDay(clientId, day));
        if (raw == null || raw.isBlank()) {
            return 0L;
        }
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }

    /**
     * 全量替换 IP 白名单；空列表表示不限制。
     */
    private void replaceIps(Long clientId, List<String> ipList) {
        clientQueryMapper.deleteIps(clientId);
        if (ipList == null || ipList.isEmpty()) {
            return;
        }
        Set<String> unique = new LinkedHashSet<>();
        for (String ip : ipList) {
            if (ip == null || ip.isBlank()) {
                continue;
            }
            String trimmed = ip.trim();
            if (trimmed.length() > 64) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "IP 地址过长: " + trimmed);
            }
            unique.add(trimmed);
        }
        for (String ip : unique) {
            clientQueryMapper.insertIp(clientId, ip);
        }
    }

    /**
     * 全量替换接口绑定；校验接口存在且未删除。
     */
    private void replaceEndpoints(Long clientId, List<Long> endpointIds) {
        clientQueryMapper.deleteEndpoints(clientId);
        if (endpointIds == null || endpointIds.isEmpty()) {
            return;
        }
        Set<Long> unique = new LinkedHashSet<>(endpointIds);
        for (Long endpointId : unique) {
            if (endpointId == null) {
                continue;
            }
            endpointRepository.findByIdAndDeleted(endpointId, 0)
                    .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "接口不存在: " + endpointId));
            clientQueryMapper.insertEndpoint(clientId, endpointId);
        }
    }

    private static void applyMeta(OpenApiClient client, OpenClientSaveRequest request) {
        client.setName(request.getName().trim());
        client.setDailyLimit(normalizeLimit(request.getDailyLimit()));
        client.setQpsLimit(normalizeLimit(request.getQpsLimit()));
        client.setExpireAt(request.getExpireAt());
        client.setRemark(request.getRemark());
        client.setStatus(Boolean.TRUE.equals(request.getStatus()));
    }

    /**
     * ≤0 统一存为 null，表示不限制。
     */
    private static Integer normalizeLimit(Integer value) {
        if (value == null || value <= 0) {
            return null;
        }
        return value;
    }

    private static OpenClientCredentialsView credentials(Long id, String name, String accessKey,
                                                         String apiKey, String accessSecret) {
        OpenClientCredentialsView view = new OpenClientCredentialsView();
        view.setId(id);
        view.setName(name);
        view.setAccessKey(accessKey);
        view.setApiKey(apiKey);
        view.setAccessSecret(accessSecret);
        return view;
    }
}

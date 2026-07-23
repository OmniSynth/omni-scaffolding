package com.omni.scaffolding.modules.system.service;

import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.api.PageQuery;
import com.omni.scaffolding.common.api.PageResult;
import com.omni.scaffolding.common.cache.CacheNames;
import com.omni.scaffolding.common.exception.BusinessException;
import com.omni.scaffolding.common.util.IdGenerator;
import com.omni.scaffolding.modules.system.dto.config.ConfigSaveRequest;
import com.omni.scaffolding.modules.system.dto.config.ConfigView;
import com.omni.scaffolding.modules.system.dto.excel.ConfigExportRow;
import com.omni.scaffolding.modules.system.entity.SysConfig;
import com.omni.scaffolding.modules.system.mapper.SysConfigQueryMapper;
import com.omni.scaffolding.modules.system.repository.SysConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 系统参数领域服务。
 *
 * <p>管理端维护键值对；业务侧通过 {@link #getValue(String)} 按键读取启用中的参数值（带缓存）。
 * 写操作会按键或全量失效 {@link CacheNames#SYS_CONFIG} 缓存。
 */
@Service
@RequiredArgsConstructor
public class ConfigService {

    /**
     * 导出最大行数，避免一次拉取过大。
     */
    private static final long EXPORT_LIMIT = 10_000L;

    private final SysConfigRepository configRepository;
    private final SysConfigQueryMapper configQueryMapper;

    /**
     * 分页查询系统参数。
     *
     * @param keyword 可选，匹配参数键 / 名称 / 值
     * @param page    页码，从 1 开始
     * @param size    每页条数
     * @return 分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<ConfigView> list(String keyword, Long page, Long size) {
        PageQuery pq = PageQuery.of(page, size);
        long total = configQueryMapper.countConfigs(keyword);
        if (total == 0) {
            return pq.toResult(0, List.of());
        }
        return pq.toResult(total, configQueryMapper.listConfigs(keyword, pq.getSize(), pq.getOffset()));
    }

    /**
     * 参数详情；不存在则 404。
     *
     * @param configId 参数主键
     * @return 读模型
     */
    @Transactional(readOnly = true)
    public ConfigView detail(Long configId) {
        ConfigView view = configQueryMapper.findById(configId);
        if (view == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "系统参数不存在");
        }
        return view;
    }

    /**
     * 导出系统参数 Excel 行（过滤条件与列表一致，最多 {@link #EXPORT_LIMIT} 行）。
     *
     * @param keyword 可选，匹配参数键 / 名称 / 值
     * @return 导出行列表
     */
    @Transactional(readOnly = true)
    public List<ConfigExportRow> export(String keyword) {
        return configQueryMapper.listConfigs(keyword, EXPORT_LIMIT, 0).stream()
                .map(this::toExportRow)
                .toList();
    }

    /**
     * 按键读取启用中的参数值；不存在或停用返回 {@code null}。
     *
     * <p>结果缓存于 {@link CacheNames#SYS_CONFIG}，键为 {@code configKey}。
     *
     * @param configKey 参数键
     * @return 参数值，不存在或停用则为 {@code null}
     */
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.SYS_CONFIG, key = "#configKey")
    public String getValue(String configKey) {
        if (configKey == null || configKey.isBlank()) {
            return null;
        }
        ConfigView view = configQueryMapper.findByKey(configKey.trim());
        return view == null ? null : view.getConfigValue();
    }

    /**
     * 新增自定义参数；参数键在未删除范围内唯一；新建一律非内置。
     *
     * @param request 创建请求
     * @return 新建参数读模型
     */
    @Transactional
    @CacheEvict(cacheNames = CacheNames.SYS_CONFIG, key = "#request.configKey")
    public ConfigView create(ConfigSaveRequest request) {
        String key = request.getConfigKey().trim();
        if (configRepository.existsByConfigKeyAndDeleted(key, 0)) {
            throw new BusinessException(ErrorCode.CONFLICT, "参数键已存在");
        }
        SysConfig config = new SysConfig();
        config.setId(IdGenerator.nextId());
        config.setConfigKey(key);
        applyMutable(config, request);
        config.setBuiltin(false);
        config.setDeleted(0);
        configRepository.save(config);
        return detail(config.getId());
    }

    /**
     * 修改参数；内置参数允许改值 / 名称 / 状态，不允许改键；非内置改键时校验唯一。
     *
     * @param configId 参数主键
     * @param request  修改请求
     * @return 更新后的读模型
     */
    @Transactional
    @CacheEvict(cacheNames = CacheNames.SYS_CONFIG, allEntries = true)
    public ConfigView update(Long configId, ConfigSaveRequest request) {
        SysConfig config = configRepository.findByIdAndDeleted(configId, 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "系统参数不存在"));
        String newKey = request.getConfigKey().trim();
        if (Boolean.TRUE.equals(config.getBuiltin()) && !config.getConfigKey().equals(newKey)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "内置参数键不可修改");
        }
        if (!config.getConfigKey().equals(newKey)) {
            configRepository.findByConfigKeyAndDeleted(newKey, 0).ifPresent(other -> {
                if (!other.getId().equals(configId)) {
                    throw new BusinessException(ErrorCode.CONFLICT, "参数键已存在");
                }
            });
            config.setConfigKey(newKey);
        }
        applyMutable(config, request);
        configRepository.save(config);
        return detail(configId);
    }

    /**
     * 单独切换参数启停；停用后 {@link #getValue(String)} 不再返回该键。
     *
     * @param configId 参数主键
     * @param status   是否启用
     * @return 更新后的读模型
     */
    @Transactional
    @CacheEvict(cacheNames = CacheNames.SYS_CONFIG, key = "#result.configKey")
    public ConfigView changeStatus(Long configId, boolean status) {
        SysConfig config = configRepository.findByIdAndDeleted(configId, 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "系统参数不存在"));
        config.setStatus(status);
        configRepository.save(config);
        return detail(configId);
    }

    /**
     * 逻辑删除参数；内置参数不可删。
     *
     * @param configId 参数主键
     * @return 被删参数键，供缓存失效使用
     */
    @Transactional
    @CacheEvict(cacheNames = CacheNames.SYS_CONFIG, key = "#result")
    public String remove(Long configId) {
        SysConfig config = configRepository.findByIdAndDeleted(configId, 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "系统参数不存在"));
        if (Boolean.TRUE.equals(config.getBuiltin())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "内置参数不可删除");
        }
        String key = config.getConfigKey();
        config.setDeleted(1);
        configRepository.save(config);
        return key;
    }

    /**
     * 手动刷新系统参数缓存（清空 {@link CacheNames#SYS_CONFIG}）。
     */
    @CacheEvict(cacheNames = CacheNames.SYS_CONFIG, allEntries = true)
    public void refreshCache() {
        // Spring Cache 代理触发失效
    }

    /**
     * 将请求中的可变字段写入实体（名称、值、备注、排序、状态）。
     *
     * @param config  目标实体
     * @param request 保存请求
     */
    private void applyMutable(SysConfig config, ConfigSaveRequest request) {
        config.setConfigName(request.getConfigName().trim());
        config.setConfigValue(request.getConfigValue() == null ? "" : request.getConfigValue());
        config.setRemark(blankToNull(request.getRemark()));
        config.setSort(request.getSort());
        config.setStatus(Boolean.TRUE.equals(request.getStatus()));
    }

    /**
     * 读模型转导出行（状态 / 内置转中文）。
     *
     * @param view 参数配置读模型
     * @return 导出行
     */
    private ConfigExportRow toExportRow(ConfigView view) {
        ConfigExportRow row = new ConfigExportRow();
        row.setId(view.getId());
        row.setConfigKey(view.getConfigKey());
        row.setConfigName(view.getConfigName());
        row.setConfigValue(view.getConfigValue());
        row.setSort(view.getSort());
        row.setStatus(Boolean.TRUE.equals(view.getStatus()) ? "启用" : "停用");
        row.setBuiltin(Boolean.TRUE.equals(view.getBuiltin()) ? "是" : "否");
        row.setRemark(view.getRemark());
        return row;
    }

    /**
     * 空白字符串转 {@code null}。
     *
     * @param value 原始字符串
     * @return 非空白时返回 trim 后的值，否则 {@code null}
     */
    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}


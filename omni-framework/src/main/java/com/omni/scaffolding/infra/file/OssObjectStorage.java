package com.omni.scaffolding.infra.file;

import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.exception.BusinessException;
import com.omni.scaffolding.common.file.ObjectStorage;
import com.omni.scaffolding.common.file.OssProviderPlugin;
import com.omni.scaffolding.common.file.StorageTypes;
import com.omni.scaffolding.common.file.StoreRequest;
import com.omni.scaffolding.common.file.StoredObject;
import com.omni.scaffolding.common.file.StoredObjectRef;
import com.omni.scaffolding.config.OmniFileProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * OSS 路由存储：按配置或对象上的 {@code ossProvider} 委托给 {@link OssProviderPlugin}。
 *
 * <p>写入使用 {@code omni.file.oss.provider}；读/删优先使用元数据中的 provider，
 * 以便切换厂商后仍能访问旧对象。
 */
@Component
@RequiredArgsConstructor
public class OssObjectStorage implements ObjectStorage {

    private final OmniFileProperties fileProperties;
    private final List<OssProviderPlugin> plugins;

    /**
     * {@inheritDoc}
     */
    @Override
    public String type() {
        return StorageTypes.OSS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StoredObject store(StoreRequest request) {
        return resolve(fileProperties.getOss().getProvider()).store(request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream open(StoredObjectRef ref) {
        String provider = StringUtils.hasText(ref.ossProvider())
                ? ref.ossProvider()
                : fileProperties.getOss().getProvider();
        return resolve(provider).open(ref);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(StoredObjectRef ref) {
        String provider = StringUtils.hasText(ref.ossProvider())
                ? ref.ossProvider()
                : fileProperties.getOss().getProvider();
        resolve(provider).delete(ref);
    }

    /**
     * 按插件 id 解析实现；大小写不敏感。
     *
     * @param providerId 插件 id
     * @return 插件实例
     */
    private OssProviderPlugin resolve(String providerId) {
        if (!StringUtils.hasText(providerId)) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "未配置 omni.file.oss.provider");
        }
        Map<String, OssProviderPlugin> map = plugins.stream()
                .collect(Collectors.toMap(
                        p -> p.providerId().toLowerCase(Locale.ROOT),
                        Function.identity(),
                        (a, b) -> a));
        OssProviderPlugin plugin = map.get(providerId.toLowerCase(Locale.ROOT));
        if (plugin == null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR,
                    "未找到 OSS 插件: " + providerId + "，已注册: " + map.keySet());
        }
        return plugin;
    }
}

package com.omni.scaffolding.infra.file;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.exception.BusinessException;
import com.omni.scaffolding.common.file.OssProviderPlugin;
import com.omni.scaffolding.common.file.StorageTypes;
import com.omni.scaffolding.common.file.StoreRequest;
import com.omni.scaffolding.common.file.StoredObject;
import com.omni.scaffolding.common.file.StoredObjectRef;
import com.omni.scaffolding.config.OmniFileProperties;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.util.Map;

/**
 * 阿里云 OSS 插件（{@code omni.file.oss.provider=aliyun}）。
 *
 * <p>凭证取自 {@code omni.file.oss.providers.aliyun}：
 * {@code endpoint}、{@code access-key-id}、{@code access-key-secret}、{@code bucket}，
 * 可选 {@code dir-prefix}。
 */
@Component
@RequiredArgsConstructor
public class AliyunOssProviderPlugin implements OssProviderPlugin {

    /**
     * 插件 id，与配置 {@code omni.file.oss.provider} 对齐。
     */
    public static final String PROVIDER_ID = "aliyun";

    private final OmniFileProperties fileProperties;
    private volatile OSS client;

    /**
     * {@inheritDoc}
     */
    @Override
    public String providerId() {
        return PROVIDER_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StoredObject store(StoreRequest request) {
        Map<String, String> cfg = requireConfig();
        String key = withPrefix(cfg.get("dir-prefix"), request.objectKey());
        try {
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(request.sizeBytes());
            if (StringUtils.hasText(request.contentType())) {
                meta.setContentType(request.contentType());
            }
            client().putObject(cfg.get("bucket"), key, request.content(), meta);
            return new StoredObject(StorageTypes.OSS, PROVIDER_ID, key);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "阿里云 OSS 上传失败: " + ex.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream open(StoredObjectRef ref) {
        Map<String, String> cfg = requireConfig();
        try {
            return client().getObject(cfg.get("bucket"), ref.objectKey()).getObjectContent();
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "阿里云 OSS 读取失败");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(StoredObjectRef ref) {
        Map<String, String> cfg = requireConfig();
        try {
            client().deleteObject(cfg.get("bucket"), ref.objectKey());
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "阿里云 OSS 删除失败");
        }
    }

    /**
     * 关闭 OSS 客户端。
     */
    @PreDestroy
    void destroy() {
        if (client != null) {
            client.shutdown();
        }
    }

    /**
     * 懒加载并缓存 OSS 客户端（双重检查）。
     */
    private OSS client() {
        OSS existing = client;
        if (existing != null) {
            return existing;
        }
        synchronized (this) {
            if (client == null) {
                Map<String, String> cfg = requireConfig();
                client = new OSSClientBuilder().build(
                        cfg.get("endpoint"),
                        cfg.get("access-key-id"),
                        cfg.get("access-key-secret"));
            }
            return client;
        }
    }

    /**
     * 读取并校验阿里云配置项。
     *
     * @return 厂商配置 Map
     */
    private Map<String, String> requireConfig() {
        Map<String, String> cfg = fileProperties.getOss().getProviders().get(PROVIDER_ID);
        if (cfg == null
                || !StringUtils.hasText(cfg.get("endpoint"))
                || !StringUtils.hasText(cfg.get("access-key-id"))
                || !StringUtils.hasText(cfg.get("access-key-secret"))
                || !StringUtils.hasText(cfg.get("bucket"))) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR,
                    "请配置 omni.file.oss.providers.aliyun（endpoint/access-key-id/access-key-secret/bucket）");
        }
        return cfg;
    }

    /**
     * 拼接可选目录前缀与对象键。
     */
    private static String withPrefix(String prefix, String objectKey) {
        if (!StringUtils.hasText(prefix)) {
            return objectKey;
        }
        String p = prefix.replaceAll("^/+", "").replaceAll("/+$", "");
        return p + "/" + objectKey.replaceAll("^/+", "");
    }
}

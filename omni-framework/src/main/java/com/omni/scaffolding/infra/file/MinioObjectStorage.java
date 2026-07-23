package com.omni.scaffolding.infra.file;

import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.exception.BusinessException;
import com.omni.scaffolding.common.file.ObjectStorage;
import com.omni.scaffolding.common.file.StorageTypes;
import com.omni.scaffolding.common.file.StoreRequest;
import com.omni.scaffolding.common.file.StoredObject;
import com.omni.scaffolding.common.file.StoredObjectRef;
import com.omni.scaffolding.config.OmniFileProperties;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.InputStream;

/**
 * MinIO 对象存储。
 *
 * <p>仅当 {@code omni.file.storage-type=minio} 时装配；启动时校验 endpoint/密钥并确保 bucket 存在。
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "omni.file", name = "storage-type", havingValue = "minio")
public class MinioObjectStorage implements ObjectStorage {

    private final OmniFileProperties fileProperties;
    private MinioClient client;

    /**
     * 初始化客户端并确保 bucket 可用。
     */
    @PostConstruct
    void init() {
        OmniFileProperties.Minio cfg = fileProperties.getMinio();
        if (!StringUtils.hasText(cfg.getEndpoint())
                || !StringUtils.hasText(cfg.getAccessKey())
                || !StringUtils.hasText(cfg.getSecretKey())) {
            throw new IllegalStateException("storage-type=minio 时须配置 omni.file.minio.endpoint/access-key/secret-key");
        }
        this.client = MinioClient.builder()
                .endpoint(cfg.getEndpoint())
                .credentials(cfg.getAccessKey(), cfg.getSecretKey())
                .build();
        try {
            boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(cfg.getBucket()).build());
            if (!exists) {
                client.makeBucket(MakeBucketArgs.builder().bucket(cfg.getBucket()).build());
            }
        } catch (Exception ex) {
            throw new IllegalStateException("MinIO bucket 初始化失败: " + ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String type() {
        return StorageTypes.MINIO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StoredObject store(StoreRequest request) {
        OmniFileProperties.Minio cfg = fileProperties.getMinio();
        String key = withPrefix(cfg.getDirPrefix(), request.objectKey());
        try {
            client.putObject(PutObjectArgs.builder()
                    .bucket(cfg.getBucket())
                    .object(key)
                    .stream(request.content(), request.sizeBytes(), -1)
                    .contentType(request.contentType() == null ? "application/octet-stream" : request.contentType())
                    .build());
            return new StoredObject(StorageTypes.MINIO, null, key);
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "MinIO 上传失败: " + ex.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream open(StoredObjectRef ref) {
        try {
            return client.getObject(GetObjectArgs.builder()
                    .bucket(fileProperties.getMinio().getBucket())
                    .object(ref.objectKey())
                    .build());
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "MinIO 文件读取失败");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(StoredObjectRef ref) {
        try {
            client.removeObject(RemoveObjectArgs.builder()
                    .bucket(fileProperties.getMinio().getBucket())
                    .object(ref.objectKey())
                    .build());
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "MinIO 文件删除失败");
        }
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

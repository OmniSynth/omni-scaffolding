package com.omni.scaffolding.infra.file;

import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.exception.BusinessException;
import com.omni.scaffolding.common.file.ObjectStorage;
import com.omni.scaffolding.common.file.StorageTypes;
import com.omni.scaffolding.common.file.StoreRequest;
import com.omni.scaffolding.common.file.StoredObject;
import com.omni.scaffolding.common.file.StoredObjectRef;
import com.omni.scaffolding.config.OmniFileProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 文件存储引擎：写入走当前激活类型；读/删按对象记录的 {@code storageType} 路由。
 *
 * <p>启动时校验 {@code omni.file.storage-type} 对应的 {@link ObjectStorage} 已注册
 * （例如 MinIO 仅在 {@code storage-type=minio} 时装配）。
 */
@Component
@RequiredArgsConstructor
public class FileStorageEngine {

    private final OmniFileProperties fileProperties;
    private final List<ObjectStorage> storages;
    private Map<String, ObjectStorage> storageMap;

    /**
     * 建立类型 → 实现映射，并校验激活类型可用。
     */
    @PostConstruct
    void init() {
        storageMap = storages.stream()
                .collect(Collectors.toMap(ObjectStorage::type, Function.identity(), (a, b) -> a));
        String active = StorageTypes.fromConfig(fileProperties.getStorageType());
        if (!storageMap.containsKey(active)) {
            throw new IllegalStateException("激活的存储类型不可用: " + active
                    + "，已注册: " + storageMap.keySet()
                    + "（MinIO 需配置 omni.file.storage-type=minio 及凭证）");
        }
        if (StorageTypes.OSS.equals(active) && storages.stream().noneMatch(s -> StorageTypes.OSS.equals(s.type()))) {
            throw new IllegalStateException("storage-type=oss 但未注册 OssObjectStorage");
        }
    }

    /**
     * 按当前配置写入对象。
     *
     * @param request 写入请求
     * @return 已存储描述（含实际 storageType / objectKey）
     */
    public StoredObject store(StoreRequest request) {
        return active().store(request);
    }

    /**
     * 按引用上的存储类型打开流。
     *
     * @param ref 对象引用
     * @return 内容流（调用方关闭）
     */
    public InputStream open(StoredObjectRef ref) {
        return resolve(ref.storageType()).open(ref);
    }

    /**
     * 按引用上的存储类型物理删除。
     *
     * @param ref 对象引用
     */
    public void delete(StoredObjectRef ref) {
        resolve(ref.storageType()).delete(ref);
    }

    /**
     * 当前激活的写入存储类型（库表常量）。
     *
     * @return {@link StorageTypes} 常量
     */
    public String activeType() {
        return StorageTypes.fromConfig(fileProperties.getStorageType());
    }

    /**
     * 当前激活的 OSS 插件 id；非 OSS 模式返回 {@code null}。
     *
     * @return providerId 或 {@code null}
     */
    public String activeOssProvider() {
        if (!StorageTypes.OSS.equals(activeType())) {
            return null;
        }
        return fileProperties.getOss().getProvider();
    }

    private ObjectStorage active() {
        return resolve(activeType());
    }

    private ObjectStorage resolve(String storageType) {
        String key = StorageTypes.normalize(storageType);
        ObjectStorage storage = storageMap.get(key);
        if (storage == null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "存储类型未启用: " + key);
        }
        return storage;
    }
}

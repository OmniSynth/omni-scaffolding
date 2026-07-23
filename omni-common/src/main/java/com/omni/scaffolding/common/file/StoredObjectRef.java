package com.omni.scaffolding.common.file;

/**
 * 打开/删除时的对象引用（通常来自 {@code sys_file} 元数据）。
 *
 * @param storageType {@link StorageTypes} 常量
 * @param ossProvider OSS 插件 id，可空
 * @param objectKey   对象键
 */
public record StoredObjectRef(
        String storageType,
        String ossProvider,
        String objectKey
) {

    /**
     * 从写入结果构造引用。
     *
     * @param stored 已存储对象
     * @return 对象引用
     */
    public static StoredObjectRef from(StoredObject stored) {
        return new StoredObjectRef(stored.storageType(), stored.ossProvider(), stored.objectKey());
    }
}

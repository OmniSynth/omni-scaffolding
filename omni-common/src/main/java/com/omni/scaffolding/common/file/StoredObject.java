package com.omni.scaffolding.common.file;

/**
 * 已存储对象描述（写入成功后返回，供元数据落库）。
 *
 * @param storageType {@link StorageTypes} 常量
 * @param ossProvider OSS 插件 id（非 OSS 时为 {@code null}）
 * @param objectKey   最终对象键（可能已加厂商 dir-prefix）
 */
public record StoredObject(
        String storageType,
        String ossProvider,
        String objectKey
) {
}

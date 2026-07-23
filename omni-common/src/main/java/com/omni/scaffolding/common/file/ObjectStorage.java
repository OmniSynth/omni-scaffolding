package com.omni.scaffolding.common.file;

import java.io.InputStream;

/**
 * 对象存储抽象（Local / MinIO / OSS）。
 *
 * <p>由 {@code FileStorageEngine} 按激活类型写入；读/删按 {@link StoredObjectRef#storageType()} 路由，
 * 以便切换存储后端后仍能访问历史文件。
 */
public interface ObjectStorage {

    /**
     * 存储类型标识。
     *
     * @return {@link StorageTypes} 常量之一
     */
    String type();

    /**
     * 写入对象并返回落库所需描述。
     *
     * @param request 写入请求（对象键、MIME、流等）
     * @return 已存储对象描述（含最终 objectKey）
     */
    StoredObject store(StoreRequest request);

    /**
     * 打开对象输入流；调用方负责关闭。
     *
     * @param ref 对象引用（须含 storageType / objectKey）
     * @return 内容流
     */
    InputStream open(StoredObjectRef ref);

    /**
     * 物理删除对象；对象不存在时可忽略或静默成功。
     *
     * @param ref 对象引用
     */
    void delete(StoredObjectRef ref);
}

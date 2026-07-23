package com.omni.scaffolding.common.file;

import java.io.InputStream;

/**
 * OSS 厂商插件 SPI。
 *
 * <p>新增七牛等厂商时：实现本接口并注册为 Spring Bean，配置
 * {@code omni.file.oss.provider} 与 {@code omni.file.oss.providers.<id>.*} 即可切换，无需改动引擎核心。
 *
 * @see com.omni.scaffolding.infra.file.OssObjectStorage
 */
public interface OssProviderPlugin {

    /**
     * 插件 id，与配置 {@code omni.file.oss.provider} 对应。
     *
     * @return 如 {@code aliyun}、{@code qiniu}
     */
    String providerId();

    /**
     * 上传对象到该云厂商。
     *
     * @param request 写入请求
     * @return 已存储对象（{@code storageType=OSS}，{@code ossProvider=providerId}）
     */
    StoredObject store(StoreRequest request);

    /**
     * 打开对象流；调用方负责关闭。
     *
     * @param ref 对象引用
     * @return 内容流
     */
    InputStream open(StoredObjectRef ref);

    /**
     * 物理删除对象。
     *
     * @param ref 对象引用
     */
    void delete(StoredObjectRef ref);
}

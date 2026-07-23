package com.omni.scaffolding.common.file;

import java.io.InputStream;

/**
 * 存储写入请求。
 *
 * @param objectKey    对象键（相对路径，如 {@code avatar/xxx.png}）
 * @param contentType  MIME 类型，可空
 * @param sizeBytes    内容字节数
 * @param content      内容流（由调用方提供，存储实现读取后不负责关闭业务侧流语义以外的资源）
 * @param originalName 原始文件名（可选，部分云厂商元数据需要）
 */
public record StoreRequest(
        String objectKey,
        String contentType,
        long sizeBytes,
        InputStream content,
        String originalName
) {
}

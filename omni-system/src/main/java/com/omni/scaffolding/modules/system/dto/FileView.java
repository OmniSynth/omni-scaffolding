package com.omni.scaffolding.modules.system.dto;

import lombok.Data;

import java.time.Instant;

/**
 * 文件元数据读模型（列表 / 详情 / 上传响应）。
 */
@Data
public class FileView {

    /**
     * 文件主键。
     */
    private Long id;

    /**
     * 原始文件名。
     */
    private String originalName;

    /**
     * MIME 类型。
     */
    private String contentType;

    /**
     * 字节大小。
     */
    private Long sizeBytes;

    /**
     * 存储类型：LOCAL / MINIO / OSS。
     */
    private String storageType;

    /**
     * OSS 插件 id，可空。
     */
    private String ossProvider;

    /**
     * 对象键。
     */
    private String objectKey;

    /**
     * 业务类型。
     */
    private String bizType;

    /**
     * 可选 MD5。
     */
    private String md5;

    /**
     * 上传人用户 ID。
     */
    private Long createdBy;

    /**
     * 创建时间。
     */
    private Instant createdAt;

    /**
     * 短时预览 URL（服务端签发，可直接用于 {@code img}/{@code el-image}）。
     */
    private String previewUrl;
}

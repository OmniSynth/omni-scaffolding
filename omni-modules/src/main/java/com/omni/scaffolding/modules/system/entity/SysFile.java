package com.omni.scaffolding.modules.system.entity;

import com.omni.scaffolding.common.persistence.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 统一文件元数据（JPA 写模型）。
 *
 * <p>业务表只存文件 ID（如 {@code sys_user.avatar_file_id}）；二进制内容由
 * {@link com.omni.scaffolding.infra.file.FileStorageEngine} 按 {@link #storageType} 读写。
 */
@Getter
@Setter
@Entity
@Table(name = "sys_file")
public class SysFile extends BaseAuditableEntity {

    /**
     * 主键（应用侧发号）。
     */
    @Id
    private Long id;

    /**
     * 原始文件名。
     */
    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    /**
     * MIME 类型，如 {@code image/png}。
     */
    @Column(name = "content_type", length = 128)
    private String contentType;

    /**
     * 字节大小。
     */
    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes = 0L;

    /**
     * 存储类型：{@code LOCAL} / {@code MINIO} / {@code OSS}。
     */
    @Column(name = "storage_type", nullable = false, length = 16)
    private String storageType;

    /**
     * OSS 插件 id（如 {@code aliyun}）；非 OSS 时为空。
     */
    @Column(name = "oss_provider", length = 32)
    private String ossProvider;

    /**
     * 存储对象键 / 相对路径。
     */
    @Column(name = "object_key", nullable = false, length = 512)
    private String objectKey;

    /**
     * 业务类型，如 {@code avatar}、{@code common}，便于筛选。
     */
    @Column(name = "biz_type", nullable = false, length = 64)
    private String bizType = "common";

    /**
     * 可选内容 MD5。
     */
    @Column(length = 64)
    private String md5;

    /**
     * 上传人用户 ID。
     */
    @Column(name = "created_by")
    private Long createdBy;

    /**
     * 逻辑删除：0 正常，1 已删除。
     */
    @Column(nullable = false)
    private Integer deleted = 0;
}

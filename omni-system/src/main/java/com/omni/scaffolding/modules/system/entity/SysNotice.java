package com.omni.scaffolding.modules.system.entity;

import com.omni.scaffolding.common.persistence.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * 通知公告实体（JPA 写模型）。
 */
@Getter
@Setter
@Entity
@Table(name = "sys_notice")
public class SysNotice extends BaseAuditableEntity {

    /**
     * 主键。
     */
    @Id
    private Long id;

    /**
     * 标题。
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * 正文内容。
     */
    @Column(nullable = false, length = 4000)
    private String content;

    /**
     * 类型：NOTICE / ANNOUNCE。
     */
    @Column(nullable = false, length = 16)
    private String type = "NOTICE";

    /**
     * 是否启用（停用后用户端不可见）。
     */
    @Column(nullable = false)
    private Boolean status = true;

    /**
     * 发布人用户主键。
     */
    @Column(name = "publisher_id")
    private Long publisherId;

    /**
     * 发布时间。
     */
    @Column(name = "publish_time")
    private Instant publishTime;

    /**
     * 逻辑删除：0 正常，1 已删除。
     */
    @Column(nullable = false)
    private Integer deleted = 0;
}


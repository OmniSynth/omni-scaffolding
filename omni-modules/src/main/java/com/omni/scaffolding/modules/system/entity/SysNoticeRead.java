package com.omni.scaffolding.modules.system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * 公告已读记录。
 */
@Getter
@Setter
@Entity
@Table(name = "sys_notice_read")
public class SysNoticeRead {

    /**
     * 主键。
     */
    @Id
    private Long id;

    /**
     * 公告主键。
     */
    @Column(name = "notice_id", nullable = false)
    private Long noticeId;

    /**
     * 用户主键。
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 已读时间。
     */
    @Column(name = "read_time", nullable = false)
    private Instant readTime;
}


package com.omni.scaffolding.modules.system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * 登录日志实体（追加写，不走逻辑删除）。
 */
@Getter
@Setter
@Entity
@Table(name = "sys_login_log")
public class SysLoginLog {

    /**
     * 主键。
     */
    @Id
    private Long id;

    /**
     * 用户主键（登录成功时有值）。
     */
    @Column(name = "user_id")
    private Long userId;

    /**
     * 登录用户名。
     */
    @Column(nullable = false, length = 64)
    private String username;

    /**
     * 客户端 IP。
     */
    @Column(length = 64)
    private String ip;

    /**
     * 浏览器 User-Agent。
     */
    @Column(name = "user_agent", length = 512)
    private String userAgent;

    /**
     * 登录结果：SUCCESS / FAIL。
     */
    @Column(nullable = false, length = 16)
    private String status;

    /**
     * 结果说明（失败原因等）。
     */
    @Column(length = 255)
    private String message;

    /**
     * 链路追踪 ID。
     */
    @Column(name = "trace_id", length = 64)
    private String traceId;

    /**
     * 登录时间。
     */
    @Column(name = "login_time", nullable = false)
    private Instant loginTime;
}


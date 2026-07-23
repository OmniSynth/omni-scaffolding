package com.omni.scaffolding.modules.system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * 操作日志实体（追加写，不走逻辑删除）。
 */
@Getter
@Setter
@Entity
@Table(name = "sys_oper_log")
public class SysOperLog {

    /**
     * 主键。
     */
    @Id
    private Long id;

    /**
     * 操作用户主键。
     */
    @Column(name = "user_id")
    private Long userId;

    /**
     * 操作用户名。
     */
    @Column(length = 64)
    private String username;

    /**
     * 业务模块。
     */
    @Column(length = 64)
    private String module;

    /**
     * 操作动作。
     */
    @Column(length = 64)
    private String action;

    /**
     * 后端方法签名。
     */
    @Column(length = 200)
    private String method;

    /**
     * 请求 URI。
     */
    @Column(name = "request_uri", length = 255)
    private String requestUri;

    /**
     * HTTP 方法。
     */
    @Column(name = "request_method", length = 16)
    private String requestMethod;

    /**
     * 客户端 IP。
     */
    @Column(length = 64)
    private String ip;

    /**
     * 执行结果：SUCCESS / FAIL。
     */
    @Column(nullable = false, length = 16)
    private String status;

    /**
     * 失败时的错误信息。
     */
    @Column(name = "error_msg", length = 1000)
    private String errorMsg;

    /**
     * 耗时（毫秒）。
     */
    @Column(name = "cost_ms")
    private Integer costMs;

    /**
     * 请求参数摘要。
     */
    @Column(length = 2000)
    private String params;

    /**
     * 链路追踪 ID。
     */
    @Column(name = "trace_id", length = 64)
    private String traceId;

    /**
     * 操作时间。
     */
    @Column(name = "oper_time", nullable = false)
    private Instant operTime;
}


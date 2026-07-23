package com.omni.scaffolding.modules.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.Instant;

/**
 * 操作日志读模型。
 */
@Data
public class OperLogView {

    /**
     * 主键。
     */
    private Long id;

    /**
     * 操作用户主键。
     */
    private Long userId;

    /**
     * 操作用户名。
     */
    private String username;

    /**
     * 业务模块。
     */
    private String module;

    /**
     * 操作动作。
     */
    private String action;

    /**
     * 后端方法签名。
     */
    private String method;

    /**
     * 请求 URI。
     */
    private String requestUri;

    /**
     * HTTP 方法。
     */
    private String requestMethod;

    /**
     * 客户端 IP。
     */
    private String ip;

    /**
     * 执行结果：SUCCESS / FAIL。
     */
    private String status;

    /**
     * 失败时的错误信息。
     */
    private String errorMsg;

    /**
     * 耗时（毫秒）。
     */
    private Integer costMs;

    /**
     * 请求参数摘要。
     */
    private String params;

    /**
     * 链路追踪 ID。
     */
    private String traceId;

    /**
     * 操作时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Instant operTime;
}

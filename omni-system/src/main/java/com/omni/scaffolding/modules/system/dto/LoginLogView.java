package com.omni.scaffolding.modules.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.Instant;

/**
 * 登录日志读模型。
 */
@Data
public class LoginLogView {

    /** 主键。 */
    private Long id;

    /** 用户主键。 */
    private Long userId;

    /** 登录用户名。 */
    private String username;

    /** 客户端 IP。 */
    private String ip;

    /** 浏览器 User-Agent。 */
    private String userAgent;

    /** 登录结果：SUCCESS / FAIL。 */
    private String status;

    /** 结果说明。 */
    private String message;

    /** 链路追踪 ID。 */
    private String traceId;

    /** 登录时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Instant loginTime;
}

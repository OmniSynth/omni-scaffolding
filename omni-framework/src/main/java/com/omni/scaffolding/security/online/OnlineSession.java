package com.omni.scaffolding.security.online;

import lombok.Data;

/**
 * 在线会话快照（存 Redis）。
 */
@Data
public class OnlineSession {

    /**
     * JWT jti，会话主键。
     */
    private String jti;

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 登录用户名。
     */
    private String username;

    /**
     * 部门 ID。
     */
    private Long deptId;

    /**
     * 登录 IP。
     */
    private String ip;

    /**
     * User-Agent。
     */
    private String userAgent;

    /**
     * 登录时间（毫秒时间戳）。
     */
    private Long loginTime;

    /**
     * 令牌过期时间（毫秒时间戳）。
     */
    private Long expireAt;
}

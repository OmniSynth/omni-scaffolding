package com.omni.scaffolding.modules.system.dto.user;

import lombok.Data;

/**
 * 在线用户会话视图。
 */
@Data
public class OnlineUserView {

    /**
     * JWT jti，踢下线主键。
     */
    private String jti;

    /**
     * 用户主键。
     */
    private Long userId;

    /**
     * 用户名。
     */
    private String username;

    /**
     * 部门主键。
     */
    private Long deptId;

    /**
     * 部门名称。
     */
    private String deptName;

    /**
     * 客户端 IP。
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

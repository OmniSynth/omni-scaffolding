package com.omni.scaffolding.modules.system.dto;

import lombok.Data;

/**
 * 登录认证读模型（MyBatis），只承载鉴权必要字段。
 */
@Data
public class UserAuthView {

    /**
     * 用户 ID。
     */
    private Long id;

    /**
     * 登录用户名。
     */
    private String username;

    /**
     * 密码哈希，仅用于 passwordEncoder.matches。
     */
    private String passwordHash;

    /**
     * 昵称。
     */
    private String nickname;

    /**
     * 所属部门 ID，写入 JWT claim {@code deptId}。
     */
    private Long deptId;

    /**
     * 是否启用。
     */
    private Boolean enabled;
}

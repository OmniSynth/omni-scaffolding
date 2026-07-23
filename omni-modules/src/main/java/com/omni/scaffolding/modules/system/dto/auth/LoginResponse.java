package com.omni.scaffolding.modules.system.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 登录成功响应。
 */
@Data
@AllArgsConstructor
public class LoginResponse {

    /**
     * JWT 访问令牌。
     */
    private String accessToken;

    /**
     * Token 类型，固定 Bearer。
     */
    private String tokenType;

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 登录用户名。
     */
    private String username;

    /**
     * 所属部门 ID。
     */
    private Long deptId;

    /**
     * 有效数据范围（多角色合并后），如 ALL / SELF。
     */
    private String dataScope;

    /**
     * 角色编码列表。
     */
    private List<String> roles;

    /**
     * 权限编码列表（来自菜单 perms）。
     */
    private List<String> permissions;
}

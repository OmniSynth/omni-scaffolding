package com.omni.scaffolding.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 动态权限快照：用户身份已由 JWT 证明后，从库/缓存加载的有效权限。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DynamicPermissionSnapshot implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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
     * 有效数据范围（多角色合并后）。
     */
    private String dataScope;

    /**
     * 账号是否启用；禁用时不应建立鉴权上下文。
     */
    private boolean enabled;

    /**
     * 角色码列表。
     */
    private List<String> roles = new ArrayList<>();

    /**
     * 权限码列表。
     */
    private List<String> permissions = new ArrayList<>();

    /**
     * 构造已禁用用户的空权限快照。
     *
     * @param userId   用户 ID
     * @param username 用户名
     * @return {@code enabled=false}、数据范围为 {@code SELF} 的快照
     */
    public static DynamicPermissionSnapshot disabled(Long userId, String username) {
        return new DynamicPermissionSnapshot(
                userId, username, null, "SELF", false, List.of(), List.of());
    }
}

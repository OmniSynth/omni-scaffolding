package com.omni.scaffolding.security;

/**
 * 按用户 ID 加载实时权限（角色 / 权限码 / 数据范围 / 部门）。
 *
 * <p>由业务模块实现；仅在 {@code omni.security.dynamic-permission.enabled=true} 时被过滤器调用。
 */
public interface DynamicPermissionLoader {

    /**
     * 加载用户有效权限。
     *
     * @param userId 用户 ID
     * @return 快照；用户不存在时返回 {@code enabled=false} 的占位，勿返回 null（Redis 不缓存 null）
     */
    DynamicPermissionSnapshot load(Long userId);
}

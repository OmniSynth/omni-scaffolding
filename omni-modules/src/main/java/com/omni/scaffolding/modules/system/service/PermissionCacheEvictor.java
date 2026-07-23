package com.omni.scaffolding.modules.system.service;

import com.omni.scaffolding.common.cache.CacheNames;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;

/**
 * 动态权限缓存失效助手。
 *
 * <p>封装对 {@link CacheNames#USER_PERMISSIONS} 的驱逐，供角色 / 菜单 / 用户服务在写路径调用。
 * 方法体为空：实际失效由 Spring Cache AOP 在方法执行时完成。
 *
 * <ul>
 *   <li>角色、菜单变更 → {@link #evictAll()}（影响面广）</li>
 *   <li>单用户角色 / 启停 / 删除 → {@link #evictUser(Long)}</li>
 * </ul>
 */
@Component
public class PermissionCacheEvictor {

    /**
     * 清空全部用户权限缓存。
     *
     * <p>适用于角色菜单授权、角色启停、菜单 perms/状态变更等无法精确枚举受影响用户的场景。
     */
    @CacheEvict(cacheNames = CacheNames.USER_PERMISSIONS, allEntries = true)
    public void evictAll() {
        // Spring Cache 代理触发失效
    }

    /**
     * 按用户 ID 失效权限缓存。
     *
     * @param userId 受影响的用户主键
     */
    @CacheEvict(cacheNames = CacheNames.USER_PERMISSIONS, key = "#userId")
    public void evictUser(Long userId) {
        // Spring Cache 代理触发失效
    }
}

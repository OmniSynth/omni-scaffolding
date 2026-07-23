package com.omni.scaffolding.modules.system.service;

import com.omni.scaffolding.common.cache.CacheNames;
import com.omni.scaffolding.modules.system.dto.UserAuthView;
import com.omni.scaffolding.modules.system.mapper.SysUserQueryMapper;
import com.omni.scaffolding.security.DynamicPermissionLoader;
import com.omni.scaffolding.security.DynamicPermissionSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 动态权限加载器（数据库实现）。
 *
 * <p>仅在 {@code omni.security.dynamic-permission.enabled=true} 时由
 * {@link com.omni.scaffolding.security.JwtAuthenticationFilter} 调用。
 * 结果缓存于 {@link CacheNames#USER_PERMISSIONS}（默认 TTL 见 RedisCacheManager），
 * 角色 / 菜单 / 用户授权变更时由 {@link PermissionCacheEvictor} 失效。
 *
 * <p>用户不存在或已停用时返回 {@code enabled=false} 的占位快照（不返回 null，
 * 以便与 {@code disableCachingNullValues} 兼容），过滤器据此拒绝建立认证上下文。
 */
@Service
@RequiredArgsConstructor
public class DbDynamicPermissionLoader implements DynamicPermissionLoader {

    private final SysUserQueryMapper userQueryMapper;

    /**
     * 按用户 ID 加载有效角色、权限码、部门与合并后的数据范围。
     *
     * @param userId 用户主键
     * @return 权限快照；不可用时 {@link DynamicPermissionSnapshot#isEnabled()} 为 false
     */
    @Override
    @Cacheable(cacheNames = CacheNames.USER_PERMISSIONS, key = "#userId")
    public DynamicPermissionSnapshot load(Long userId) {
        UserAuthView user = userQueryMapper.findAuthViewById(userId);
        if (user == null) {
            return DynamicPermissionSnapshot.disabled(userId, null);
        }
        if (Boolean.FALSE.equals(user.getEnabled())) {
            return DynamicPermissionSnapshot.disabled(user.getId(), user.getUsername());
        }
        List<String> roles = new ArrayList<>(userQueryMapper.findRoleCodesByUserId(userId));
        List<String> permissions = new ArrayList<>(userQueryMapper.findPermissionCodesByUserId(userId));
        String dataScope = AuthService.mergeDataScope(userQueryMapper.findDataScopesByUserId(userId)).name();
        return new DynamicPermissionSnapshot(
                user.getId(),
                user.getUsername(),
                user.getDeptId(),
                dataScope,
                true,
                roles,
                permissions);
    }
}

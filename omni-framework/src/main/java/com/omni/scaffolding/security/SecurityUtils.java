package com.omni.scaffolding.security;

import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.exception.BusinessException;
import com.omni.scaffolding.security.datascope.DataScopeType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 当前登录用户工具：从 {@link SecurityContextHolder} 取 {@link AuthUser}。
 *
 * <p>未登录或 Principal 类型不对时抛 {@link BusinessException}（401），
 * 供业务服务与数据范围解析统一使用。
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * 获取当前认证用户；未登录则抛 401。
     */
    public static AuthUser requireAuthUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUser authUser)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "未登录或登录已失效");
        }
        return authUser;
    }

    /**
     * 尝试获取当前认证用户；未登录或 Principal 类型不对时返回 {@code null}。
     */
    public static AuthUser getAuthUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUser authUser)) {
            return null;
        }
        return authUser;
    }

    /**
     * 当前用户 ID。
     */
    public static Long requireUserId() {
        return requireAuthUser().getId();
    }

    /**
     * 当前登录用户名。
     */
    public static String requireUsername() {
        return requireAuthUser().getUsername();
    }

    /**
     * 当前用户所属部门 ID；缺失则视为无权限访问部门数据。
     */
    public static Long requireDeptId() {
        Long deptId = requireAuthUser().getDeptId();
        if (deptId == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "用户未分配部门");
        }
        return deptId;
    }

    /**
     * 当前用户部门 ID；未登录或未分配部门时返回 {@code null}（不抛异常）。
     */
    public static Long getDeptId() {
        AuthUser user = getAuthUser();
        return user == null ? null : user.getDeptId();
    }

    /**
     * 当前用户 ID；未登录时返回 {@code null}。
     */
    public static Long getUserId() {
        AuthUser user = getAuthUser();
        return user == null ? null : user.getId();
    }

    /**
     * 当前用户名；未登录时返回 {@code null}。
     */
    public static String getUsername() {
        AuthUser user = getAuthUser();
        return user == null ? null : user.getUsername();
    }

    /**
     * 当前有效数据范围枚举。
     */
    public static DataScopeType requireDataScope() {
        return DataScopeType.from(requireAuthUser().getDataScope());
    }

    /**
     * 当前数据范围字符串；未登录时返回 {@code null}。
     */
    public static String getDataScope() {
        AuthUser user = getAuthUser();
        return user == null ? null : user.getDataScope();
    }
}

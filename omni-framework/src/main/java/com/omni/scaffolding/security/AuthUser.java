package com.omni.scaffolding.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * 当前登录用户主体，扩展了业务用户 ID / 部门 / 数据范围 / 会话 jti，便于限流 Key、审计、数据权限与在线会话等直接取用。
 */
@Getter
public class AuthUser extends User {

    /**
     * 业务用户主键，来自 JWT claim {@code uid}。
     */
    private final Long id;

    /**
     * 所属部门 ID，来自 JWT claim {@code deptId}。
     */
    private final Long deptId;

    /**
     * 有效数据范围（多角色合并后的最宽松值），来自 JWT claim {@code dataScope}。
     *
     * <p>取值见 {@link com.omni.scaffolding.security.datascope.DataScopeType}：
     * {@code ALL} / {@code DEPT_AND_CHILD} / {@code DEPT} / {@code SELF}。
     */
    private final String dataScope;

    /**
     * 当前访问令牌 jti，来自 JWT {@code jti}；用于主动登出 / 踢下线。
     */
    private final String jti;

    public AuthUser(Long id,
                    Long deptId,
                    String dataScope,
                    String jti,
                    String username,
                    String password,
                    Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.id = id;
        this.deptId = deptId;
        this.dataScope = dataScope;
        this.jti = jti;
    }
}

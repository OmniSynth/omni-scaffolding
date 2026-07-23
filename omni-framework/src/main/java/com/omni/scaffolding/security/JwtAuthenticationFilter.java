package com.omni.scaffolding.security;

import com.omni.scaffolding.config.OmniSecurityProperties;
import com.omni.scaffolding.security.online.OnlineSessionService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 从 {@code Authorization: Bearer <token>} 解析 JWT，并写入 {@link SecurityContextHolder}。
 *
 * <p>无 Token 或非法 Token 时不强制拦截，交由后续 Security 规则决定是否放行（白名单仍可匿名访问）。
 *
 * <p>当 {@code omni.security.dynamic-permission.enabled=true} 时，JWT 仅作身份证明，
 * 角色 / 权限 / 数据范围从 {@link DynamicPermissionLoader}（库或缓存）重载。
 *
 * <p>若令牌带 {@code jti}，还会校验在线会话是否仍有效（未踢下线）。
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final OmniSecurityProperties securityProperties;
    private final ObjectProvider<DynamicPermissionLoader> dynamicPermissionLoader;
    private final OnlineSessionService onlineSessionService;

    /**
     * 解析 Bearer Token，校验会话并填充 {@link SecurityContextHolder}。
     *
     * @param request     当前请求
     * @param response    当前响应
     * @param filterChain 过滤链
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtService.isValid(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
                Claims claims = jwtService.parseClaims(token);
                String jti = claims.getId();
                if (onlineSessionService.isActive(jti)) {
                    AuthUser principal = buildPrincipal(claims);
                    if (principal != null) {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    /**
     * 由 JWT Claims 构建认证主体；动态权限开启时从库重载角色与权限。
     *
     * @param claims 已验签的 JWT Claims
     * @return 认证用户，动态权限加载失败或用户禁用时为 null
     */
    private AuthUser buildPrincipal(Claims claims) {
        Long userId = claims.get("uid", Long.class);
        String username = claims.getSubject();
        String jti = claims.getId();
        if (securityProperties.getDynamicPermission().isEnabled()) {
            DynamicPermissionLoader loader = dynamicPermissionLoader.getIfAvailable();
            if (loader != null && userId != null) {
                DynamicPermissionSnapshot snap = loader.load(userId);
                if (snap == null || !snap.isEnabled()) {
                    return null;
                }
                return new AuthUser(
                        snap.getUserId(),
                        snap.getDeptId(),
                        snap.getDataScope(),
                        jti,
                        snap.getUsername() != null ? snap.getUsername() : username,
                        "",
                        toAuthorities(snap.getRoles(), snap.getPermissions()));
            }
        }
        Collection<SimpleGrantedAuthority> authorities = extractAuthorities(claims);
        return new AuthUser(
                userId,
                claims.get("deptId", Long.class),
                claims.get("dataScope", String.class),
                jti,
                username,
                "",
                authorities);
    }

    /**
     * 将角色码与权限码转为 Spring Security Authority 集合。
     *
     * @param roles       角色码列表
     * @param permissions 权限码列表
     * @return GrantedAuthority 集合
     */
    private static Collection<SimpleGrantedAuthority> toAuthorities(List<String> roles, List<String> permissions) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (roles != null) {
            roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
        }
        if (permissions != null) {
            permissions.forEach(perm -> authorities.add(new SimpleGrantedAuthority(perm)));
        }
        return authorities;
    }

    /**
     * 角色转为 {@code ROLE_xxx}，权限码原样作为 authority，供 {@code hasRole/hasAuthority} 使用。
     */
    @SuppressWarnings("unchecked")
    private Collection<SimpleGrantedAuthority> extractAuthorities(Claims claims) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        Object roles = claims.get("roles");
        if (roles instanceof List<?> roleList) {
            roleList.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
        }
        Object perms = claims.get("perms");
        if (perms instanceof List<?> permList) {
            permList.forEach(perm -> authorities.add(new SimpleGrantedAuthority(String.valueOf(perm))));
        }
        return authorities;
    }
}

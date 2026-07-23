package com.omni.scaffolding.security;

import com.omni.scaffolding.config.OmniSecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * JWT 签发与校验。
 *
 * <p>Claims 约定：
 * <ul>
 *   <li>{@code jti}：令牌唯一 ID，用于在线会话与强制下线</li>
 *   <li>{@code sub}：用户名</li>
 *   <li>{@code uid}：用户 ID</li>
 *   <li>{@code deptId}：所属部门 ID</li>
 *   <li>{@code dataScope}：有效数据范围（多角色合并后）</li>
 *   <li>{@code roles}：角色码列表（过滤器会加 {@code ROLE_} 前缀；动态权限开启时仅作快照）</li>
 *   <li>{@code perms}：权限码列表，直接作为 GrantedAuthority（动态权限开启时由库/缓存重载）</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    private final OmniSecurityProperties securityProperties;

    /**
     * 签发访问令牌；过期时间见 {@code omni.security.jwt.expiration-ms}。
     *
     * @param username    登录用户名（写入 {@code sub}）
     * @param userId      用户 ID（写入 {@code uid}）
     * @param deptId      部门 ID
     * @param dataScope   有效数据范围
     * @param roles       角色码列表
     * @param permissions 权限码列表
     * @return 含 accessToken / jti / expireAt
     */
    public IssuedToken generateToken(String username,
                                     Long userId,
                                     Long deptId,
                                     String dataScope,
                                     List<String> roles,
                                     List<String> permissions) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + securityProperties.getJwt().getExpirationMs());
        String jti = UUID.randomUUID().toString().replace("-", "");
        String token = Jwts.builder()
                .id(jti)
                .subject(username)
                .claim("uid", userId)
                .claim("deptId", deptId)
                .claim("dataScope", dataScope)
                .claim("roles", roles)
                .claim("perms", permissions)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey())
                .compact();
        return new IssuedToken(token, jti, expiry.toInstant());
    }

    /**
     * 解析并验签；失败会抛出 jjwt 异常，由调用方决定是否吞掉。
     *
     * @param token JWT 字符串
     * @return 已验签的 Claims
     */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 轻量校验：签名合法且未过期。
     *
     * @param token JWT 字符串
     * @return {@code true} 合法且未过期
     */
    public boolean isValid(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * 计算令牌剩余有效毫秒；无效或已过期返回 0。
     *
     * @param token JWT 字符串
     * @return 剩余毫秒，无效时为 0
     */
    public long remainingTtlMs(String token) {
        try {
            Instant exp = parseClaims(token).getExpiration().toInstant();
            long ms = exp.toEpochMilli() - Instant.now().toEpochMilli();
            return Math.max(ms, 0L);
        } catch (Exception ex) {
            return 0L;
        }
    }

    /**
     * 从配置密钥字节构建 HMAC 签名密钥。
     *
     * @return JWT 验签 / 签发密钥
     */
    private SecretKey secretKey() {
        byte[] keyBytes = securityProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

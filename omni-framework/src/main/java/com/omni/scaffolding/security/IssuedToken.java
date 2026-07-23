package com.omni.scaffolding.security;

import java.time.Instant;

/**
 * JWT 签发结果：访问令牌 + 会话标识 + 过期时间。
 */
public record IssuedToken(String accessToken, String jti, Instant expireAt) {
}

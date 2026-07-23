package com.omni.scaffolding.security.sign;

import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.cache.RedisKeys;
import com.omni.scaffolding.common.exception.BusinessException;
import com.omni.scaffolding.common.util.SignUtils;
import com.omni.scaffolding.config.OmniSecurityProperties;
import com.omni.scaffolding.infra.ratelimit.RedisRateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * 登录请求加签校验：时间窗、nonce 防重放、HMAC 验签，以及按 IP 限流。
 */
@Service
@RequiredArgsConstructor
public class LoginSignService {

    private final OmniSecurityProperties securityProperties;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedisRateLimiter redisRateLimiter;

    /**
     * 校验登录加签与 IP 限流；未启用时直接放行。
     *
     * @param timestamp 客户端毫秒时间戳
     * @param nonce     一次性随机串
     * @param sign      HMAC 签名（小写 hex）
     * @param username  登录账号
     * @param password  登录密码
     * @param clientIp  客户端 IP（限流维度）
     */
    public void verify(String timestamp,
                       String nonce,
                       String sign,
                       String username,
                       String password,
                       String clientIp) {
        OmniSecurityProperties.Sign cfg = securityProperties.getSign();
        if (cfg == null || !cfg.isEnabled()) {
            return;
        }
        if (!StringUtils.hasText(cfg.getSecret())) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "登录签名密钥未配置");
        }

        String ip = StringUtils.hasText(clientIp) ? clientIp.trim() : "unknown";
        if (!redisRateLimiter.tryAcquire(RedisKeys.loginIpRate(ip), cfg.getIpLimit(), cfg.getIpWindowSeconds())) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS, "登录过于频繁，请稍后再试");
        }

        if (!StringUtils.hasText(timestamp) || !StringUtils.hasText(nonce) || !StringUtils.hasText(sign)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "缺少登录签名");
        }
        long ts;
        try {
            ts = Long.parseLong(timestamp.trim());
        } catch (NumberFormatException ex) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "登录时间戳无效");
        }
        long skewMs = Math.max(1L, cfg.getSkewSeconds()) * 1000L;
        long now = System.currentTimeMillis();
        if (Math.abs(now - ts) > skewMs) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "登录签名已过期");
        }

        String nonceVal = nonce.trim();
        if (nonceVal.length() < 8 || nonceVal.length() > 64) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "登录随机串无效");
        }
        Boolean firstSeen = stringRedisTemplate.opsForValue().setIfAbsent(
                RedisKeys.loginNonce(nonceVal),
                "1",
                Duration.ofMillis(skewMs * 2));
        if (!Boolean.TRUE.equals(firstSeen)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "登录随机串已被使用");
        }

        String expected = SignUtils.hmacSha256Hex(
                cfg.getSecret(),
                SignUtils.loginPayload(timestamp.trim(), nonceVal, username, password));
        if (!SignUtils.equalsHex(expected, sign.trim().toLowerCase())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "登录签名无效");
        }
    }
}

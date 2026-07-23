package com.omni.scaffolding.security.lock;

import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.cache.RedisKeys;
import com.omni.scaffolding.common.exception.BusinessException;
import com.omni.scaffolding.config.OmniSecurityProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 登录失败锁定：按用户名计数，超过阈值后在 TTL 内拒绝登录。
 */
@Service
@RequiredArgsConstructor
public class LoginLockService {

    private final OmniSecurityProperties securityProperties;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 若账号处于锁定中则抛出业务异常。
     *
     * @param username 登录用户名
     */
    public void assertNotLocked(String username) {
        OmniSecurityProperties.LoginLock cfg = securityProperties.getLoginLock();
        if (cfg == null || !cfg.isEnabled() || !StringUtils.hasText(username)) {
            return;
        }
        String key = RedisKeys.loginFailUser(normalize(username));
        String raw = stringRedisTemplate.opsForValue().get(key);
        if (!StringUtils.hasText(raw)) {
            return;
        }
        int failures;
        try {
            failures = Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            return;
        }
        if (failures < cfg.getMaxFailures()) {
            return;
        }
        Long ttl = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        long remain = ttl == null || ttl < 0 ? cfg.getLockSeconds() : ttl;
        throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS,
                "登录失败次数过多，请 " + Math.max(1, remain) + " 秒后再试");
    }

    /**
     * 记录一次密码错误；达到阈值后保持锁定至 TTL 结束。
     *
     * @param username 登录用户名
     */
    public void recordFailure(String username) {
        OmniSecurityProperties.LoginLock cfg = securityProperties.getLoginLock();
        if (cfg == null || !cfg.isEnabled() || !StringUtils.hasText(username)) {
            return;
        }
        String key = RedisKeys.loginFailUser(normalize(username));
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            stringRedisTemplate.expire(key, Duration.ofSeconds(Math.max(60, cfg.getLockSeconds())));
        }
    }

    /**
     * 登录成功清除失败计数。
     *
     * @param username 登录用户名
     */
    public void clearFailures(String username) {
        if (!StringUtils.hasText(username)) {
            return;
        }
        stringRedisTemplate.delete(RedisKeys.loginFailUser(normalize(username)));
    }

    private static String normalize(String username) {
        return username.trim().toLowerCase();
    }
}

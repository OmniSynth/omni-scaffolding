package com.omni.scaffolding.infra.ratelimit;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * 固定窗口 Redis 限流器，多实例共享计数，保证集群维度一致。
 *
 * <p>实现刻意保持简单，适合脚手架演示；生产高并发可升级为滑动窗口 / 令牌桶算法。
 */
@Component
@RequiredArgsConstructor
public class RedisRateLimiter {

    /**
     * KEYS[1]=计数 Key；ARGV[1]=阈值；ARGV[2]=窗口秒数。
     * 返回 1 表示放行，0 表示拒绝。
     */
    private static final DefaultRedisScript<Long> SCRIPT = new DefaultRedisScript<>("""
            local current = redis.call('incr', KEYS[1])
            if current == 1 then
                redis.call('expire', KEYS[1], ARGV[2])
            end
            if current > tonumber(ARGV[1]) then
                return 0
            end
            return 1
            """, Long.class);

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 尝试获取一次配额。
     *
     * @param key           业务维度 Key（如 user:1 / ip:x.x.x.x）
     * @param limit         窗口内最大请求数
     * @param windowSeconds 窗口长度（秒）
     * @return {@code true} 放行，{@code false} 拒绝
     */
    public boolean tryAcquire(String key, int limit, int windowSeconds) {
        Long allowed = stringRedisTemplate.execute(
                SCRIPT,
                Collections.singletonList("rl:" + key),
                String.valueOf(limit),
                String.valueOf(windowSeconds)
        );
        return allowed != null && allowed == 1L;
    }
}

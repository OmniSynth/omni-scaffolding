package com.omni.scaffolding.infra.lock;

import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 基于 Redis 的分布式锁（SET NX + TTL，Lua 校验后删除）。
 *
 * <h2>使用约束</h2>
 * <ul>
 *   <li>锁必须带 TTL，防止进程崩溃导致死锁</li>
 *   <li>持有时间尽量短，只包临界区</li>
 *   <li>解锁时校验 token，避免误删别人的锁</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DistributedLockService {

    /**
     * 仅当 value 匹配时删除，防止锁过期后误删新持有者的锁。
     */
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>("""
            if redis.call('get', KEYS[1]) == ARGV[1] then
                return redis.call('del', KEYS[1])
            else
                return 0
            end
            """, Long.class);

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 在锁保护下执行业务逻辑。
     *
     * @param key          锁 Key，建议带业务前缀，如 {@code lock:product:sku:xxx}
     * @param waitSeconds  最多等待多久去抢锁
     * @param leaseSeconds 锁租约（TTL）
     * @param unit         时间单位
     * @param action       临界区逻辑
     * @param <T>          返回值类型
     * @return action 的执行结果
     */
    public <T> T executeWithLock(String key, long waitSeconds, long leaseSeconds, TimeUnit unit, Supplier<T> action) {
        String token = UUID.randomUUID().toString();
        long deadline = System.nanoTime() + unit.toNanos(waitSeconds);
        boolean acquired = false;
        try {
            while (System.nanoTime() < deadline) {
                Boolean ok = stringRedisTemplate.opsForValue()
                        .setIfAbsent(key, token, Duration.ofSeconds(unit.toSeconds(leaseSeconds)));
                if (Boolean.TRUE.equals(ok)) {
                    acquired = true;
                    break;
                }
                try {
                    // 短间隔重试；虚拟线程上 sleep 成本可接受
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new BusinessException(ErrorCode.INTERNAL_ERROR, "获取锁等待被中断");
                }
            }
            if (!acquired) {
                throw new BusinessException(ErrorCode.CONFLICT, "资源正在被占用，请稍后再试");
            }
            return action.get();
        } finally {
            if (acquired) {
                unlock(key, token);
            }
        }
    }

    /**
     * 校验 token 后释放锁，失败仅告警（依赖 TTL 自动过期）。
     *
     * @param key   锁 Key
     * @param token 加锁时生成的持有者 token
     */
    private void unlock(String key, String token) {
        try {
            stringRedisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(key), token);
        } catch (Exception ex) {
            // 解锁失败最多等到 TTL 自动过期，这里只告警
            log.warn("Failed to release lock {}: {}", key, ex.getMessage());
        }
    }
}

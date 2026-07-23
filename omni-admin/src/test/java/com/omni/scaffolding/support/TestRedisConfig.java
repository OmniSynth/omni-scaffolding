package com.omni.scaffolding.support;

import com.omni.scaffolding.infra.lock.DistributedLockService;
import com.omni.scaffolding.infra.ratelimit.RedisRateLimiter;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 测试用 Redis：内存 Map 模拟 StringRedisTemplate，覆盖登录加签 nonce、分布式锁与在线会话。
 */
@TestConfiguration
public class TestRedisConfig {

    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        return Mockito.mock(RedisConnectionFactory.class);
    }

    @Bean
    @Primary
    @SuppressWarnings("unchecked")
    public StringRedisTemplate stringRedisTemplate() {
        Map<String, String> values = new ConcurrentHashMap<>();
        Map<String, Long> valueExpireAt = new ConcurrentHashMap<>();
        Map<String, Set<String>> sets = new ConcurrentHashMap<>();
        Map<String, Long> setExpireAt = new ConcurrentHashMap<>();

        StringRedisTemplate template = Mockito.mock(StringRedisTemplate.class);
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        SetOperations<String, String> setOps = mock(SetOperations.class);
        when(template.opsForValue()).thenReturn(ops);
        when(template.opsForSet()).thenReturn(setOps);

        when(ops.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            String value = invocation.getArgument(1);
            Duration ttl = invocation.getArgument(2);
            purgeExpired(values, valueExpireAt, key);
            if (values.containsKey(key)) {
                return false;
            }
            values.put(key, value);
            valueExpireAt.put(key, System.currentTimeMillis() + ttl.toMillis());
            return true;
        });

        doAnswer(invocation -> {
            String key = invocation.getArgument(0);
            String value = invocation.getArgument(1);
            Duration ttl = invocation.getArgument(2);
            values.put(key, value);
            valueExpireAt.put(key, System.currentTimeMillis() + ttl.toMillis());
            return null;
        }).when(ops).set(anyString(), anyString(), any(Duration.class));

        when(ops.get(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            purgeExpired(values, valueExpireAt, key);
            return values.get(key);
        });

        when(ops.increment(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            purgeExpired(values, valueExpireAt, key);
            long next = Long.parseLong(values.getOrDefault(key, "0")) + 1L;
            values.put(key, Long.toString(next));
            return next;
        });

        when(template.hasKey(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            purgeExpired(values, valueExpireAt, key);
            purgeExpiredSet(sets, setExpireAt, key);
            return values.containsKey(key) || sets.containsKey(key);
        });

        when(template.delete(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            boolean removed = values.remove(key) != null;
            valueExpireAt.remove(key);
            return removed;
        });

        when(template.getExpire(anyString(), any(TimeUnit.class))).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            TimeUnit unit = invocation.getArgument(1);
            Long expireAt = valueExpireAt.get(key);
            if (expireAt == null) {
                return -1L;
            }
            long remainMs = expireAt - System.currentTimeMillis();
            return remainMs <= 0 ? -2L : unit.convert(remainMs, TimeUnit.MILLISECONDS);
        });

        when(template.expire(anyString(), any(Duration.class))).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            Duration ttl = invocation.getArgument(1);
            if (sets.containsKey(key)) {
                setExpireAt.put(key, System.currentTimeMillis() + ttl.toMillis());
                return true;
            }
            if (values.containsKey(key)) {
                valueExpireAt.put(key, System.currentTimeMillis() + ttl.toMillis());
                return true;
            }
            return false;
        });

        when(setOps.add(anyString(), Mockito.<String>any())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            Set<String> set = sets.computeIfAbsent(key, ignored -> ConcurrentHashMap.newKeySet());
            long added = 0;
            Object[] args = invocation.getArguments();
            for (int i = 1; i < args.length; i++) {
                if (set.add(String.valueOf(args[i]))) {
                    added++;
                }
            }
            return added;
        });

        when(setOps.members(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            purgeExpiredSet(sets, setExpireAt, key);
            Set<String> set = sets.get(key);
            return set == null ? Collections.emptySet() : new HashSet<>(set);
        });

        when(setOps.remove(anyString(), Mockito.any())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            Set<String> set = sets.get(key);
            if (set == null) {
                return 0L;
            }
            long removed = 0;
            Object[] args = invocation.getArguments();
            for (int i = 1; i < args.length; i++) {
                Object arg = args[i];
                if (arg instanceof Object[] arr) {
                    for (Object item : arr) {
                        if (set.remove(String.valueOf(item))) {
                            removed++;
                        }
                    }
                } else if (set.remove(String.valueOf(arg))) {
                    removed++;
                }
            }
            if (set.isEmpty()) {
                sets.remove(key);
                setExpireAt.remove(key);
            }
            return removed;
        });

        return template;
    }

    private static void purgeExpired(Map<String, String> values, Map<String, Long> expireAt, String key) {
        Long at = expireAt.get(key);
        if (at != null && at <= System.currentTimeMillis()) {
            values.remove(key);
            expireAt.remove(key);
        }
    }

    private static void purgeExpiredSet(Map<String, Set<String>> sets, Map<String, Long> expireAt, String key) {
        Long at = expireAt.get(key);
        if (at != null && at <= System.currentTimeMillis()) {
            sets.remove(key);
            expireAt.remove(key);
        }
    }

    @Bean
    @Primary
    public DistributedLockService distributedLockService() {
        DistributedLockService lockService = Mockito.mock(DistributedLockService.class);
        when(lockService.executeWithLock(anyString(), anyLong(), anyLong(), any(TimeUnit.class), any()))
                .thenAnswer(invocation -> {
                    Supplier<?> supplier = invocation.getArgument(4);
                    return supplier.get();
                });
        return lockService;
    }

    @Bean
    @Primary
    public RedisRateLimiter redisRateLimiter() {
        RedisRateLimiter limiter = Mockito.mock(RedisRateLimiter.class);
        when(limiter.tryAcquire(anyString(), anyInt(), anyInt())).thenReturn(true);
        return limiter;
    }
}

package com.omni.scaffolding.infra.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 统一访问封装（基于 {@link StringRedisTemplate}）。
 *
 * <p>业务代码优先通过本类读写，避免散落 {@code opsForValue()/opsForSet()}。
 * 运维控制台、复杂 SCAN 等仍可通过 {@link #template()} 取底层模板。
 */
@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 底层字符串模板（逃生口：SCAN / INFO / 复杂结构运维等）。
     *
     * @return {@link StringRedisTemplate}
     */
    public StringRedisTemplate template() {
        return stringRedisTemplate;
    }

    // -------------------------------------------------------------------------
    // String / 通用 Key
    // -------------------------------------------------------------------------

    /**
     * 读取字符串值。
     *
     * @param key Redis Key
     * @return 值，不存在为 null
     */
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 写入字符串（永不过期，除非后续单独 expire）。
     *
     * @param key   Redis Key
     * @param value 值
     */
    public void set(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    /**
     * 写入字符串并设置 TTL。
     *
     * @param key   Redis Key
     * @param value 值
     * @param ttl   过期时间
     */
    public void set(String key, String value, Duration ttl) {
        stringRedisTemplate.opsForValue().set(key, value, ttl);
    }

    /**
     * 写入字符串并设置 TTL（秒）。
     *
     * @param key         Redis Key
     * @param value       值
     * @param ttlSeconds  过期秒数；≤0 则永不过期
     */
    public void set(String key, String value, long ttlSeconds) {
        if (ttlSeconds > 0) {
            stringRedisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);
        } else {
            stringRedisTemplate.opsForValue().set(key, value);
        }
    }

    /**
     * 仅当 Key 不存在时写入（SET NX），并设置 TTL。
     *
     * @param key   Redis Key
     * @param value 值
     * @param ttl   过期时间
     * @return {@code true} 表示写入成功
     */
    public boolean setIfAbsent(String key, String value, Duration ttl) {
        Boolean ok = stringRedisTemplate.opsForValue().setIfAbsent(key, value, ttl);
        return Boolean.TRUE.equals(ok);
    }

    /**
     * 删除 Key。
     *
     * @param key Redis Key
     * @return 是否删除成功
     */
    public boolean delete(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.delete(key));
    }

    /**
     * 批量删除。
     *
     * @param keys Key 集合
     * @return 删除条数
     */
    public long delete(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return 0L;
        }
        Long n = stringRedisTemplate.delete(keys);
        return n == null ? 0L : n;
    }

    /**
     * Key 是否存在。
     *
     * @param key Redis Key
     * @return 存在则为 {@code true}
     */
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    /**
     * 设置过期时间。
     *
     * @param key Redis Key
     * @param ttl TTL
     * @return 是否设置成功
     */
    public boolean expire(String key, Duration ttl) {
        return Boolean.TRUE.equals(stringRedisTemplate.expire(key, ttl));
    }

    /**
     * 设置过期时间（秒）。
     *
     * @param key        Redis Key
     * @param ttlSeconds 秒
     * @return 是否设置成功
     */
    public boolean expire(String key, long ttlSeconds) {
        return Boolean.TRUE.equals(stringRedisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS));
    }

    /**
     * 移除过期时间（持久化）。
     *
     * @param key Redis Key
     * @return 是否成功
     */
    public boolean persist(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.persist(key));
    }

    /**
     * 剩余 TTL。
     *
     * @param key  Redis Key
     * @param unit 时间单位
     * @return 剩余时间；不存在 / 无 TTL 时语义同 Redis（常为 -2 / -1）
     */
    public Long getExpire(String key, TimeUnit unit) {
        return stringRedisTemplate.getExpire(key, unit);
    }

    /**
     * 剩余 TTL（秒）。
     *
     * @param key Redis Key
     * @return 秒
     */
    public Long getExpireSeconds(String key) {
        return stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    // -------------------------------------------------------------------------
    // 计数
    // -------------------------------------------------------------------------

    /**
     * 自增 1。
     *
     * @param key Redis Key
     * @return 自增后的值
     */
    public long increment(String key) {
        Long v = stringRedisTemplate.opsForValue().increment(key);
        return v == null ? 0L : v;
    }

    /**
     * 按步长自增。
     *
     * @param key   Redis Key
     * @param delta 步长（可为负）
     * @return 自增后的值
     */
    public long increment(String key, long delta) {
        Long v = stringRedisTemplate.opsForValue().increment(key, delta);
        return v == null ? 0L : v;
    }

    /**
     * 自减 1。
     *
     * @param key Redis Key
     * @return 自减后的值
     */
    public long decrement(String key) {
        Long v = stringRedisTemplate.opsForValue().decrement(key);
        return v == null ? 0L : v;
    }

    /**
     * 自增并在首次创建时设置 TTL（常用于日计数）。
     *
     * @param key Redis Key
     * @param ttl 首次创建时的 TTL
     * @return 自增后的值
     */
    public long incrementAndExpireOnCreate(String key, Duration ttl) {
        long count = increment(key);
        if (count == 1L && ttl != null && !ttl.isNegative() && !ttl.isZero()) {
            expire(key, ttl);
        }
        return count;
    }

    // -------------------------------------------------------------------------
    // Set
    // -------------------------------------------------------------------------

    /**
     * Set 添加成员。
     *
     * @param key     Redis Key
     * @param members 成员
     * @return 新增成员数
     */
    public long sAdd(String key, String... members) {
        if (members == null || members.length == 0) {
            return 0L;
        }
        Long n = stringRedisTemplate.opsForSet().add(key, members);
        return n == null ? 0L : n;
    }

    /**
     * 读取 Set 全部成员。
     *
     * @param key Redis Key
     * @return 成员集合，不存在时为空集
     */
    public Set<String> sMembers(String key) {
        Set<String> members = stringRedisTemplate.opsForSet().members(key);
        return members == null ? Collections.emptySet() : members;
    }

    /**
     * Set 移除成员。
     *
     * @param key     Redis Key
     * @param members 成员
     * @return 移除数量
     */
    public long sRemove(String key, Object... members) {
        if (members == null || members.length == 0) {
            return 0L;
        }
        Long n = stringRedisTemplate.opsForSet().remove(key, members);
        return n == null ? 0L : n;
    }

    /**
     * Set 元素个数。
     *
     * @param key Redis Key
     * @return 大小
     */
    public long sCard(String key) {
        Long n = stringRedisTemplate.opsForSet().size(key);
        return n == null ? 0L : n;
    }

    // -------------------------------------------------------------------------
    // Hash
    // -------------------------------------------------------------------------

    /**
     * Hash 读字段。
     *
     * @param key   Redis Key
     * @param field 字段
     * @return 值
     */
    public Object hGet(String key, String field) {
        return stringRedisTemplate.opsForHash().get(key, field);
    }

    /**
     * Hash 写字段。
     *
     * @param key   Redis Key
     * @param field 字段
     * @param value 值
     */
    public void hSet(String key, String field, String value) {
        stringRedisTemplate.opsForHash().put(key, field, value);
    }

    /**
     * Hash 全部字段。
     *
     * @param key Redis Key
     * @return 字段映射
     */
    public Map<Object, Object> hGetAll(String key) {
        return stringRedisTemplate.opsForHash().entries(key);
    }

    /**
     * Hash 删除字段。
     *
     * @param key    Redis Key
     * @param fields 字段
     * @return 删除数量
     */
    public long hDel(String key, Object... fields) {
        if (fields == null || fields.length == 0) {
            return 0L;
        }
        Long n = stringRedisTemplate.opsForHash().delete(key, fields);
        return n == null ? 0L : n;
    }

    // -------------------------------------------------------------------------
    // 脚本 / SCAN
    // -------------------------------------------------------------------------

    /**
     * 执行 Lua 脚本。
     *
     * @param script 脚本
     * @param keys   KEYS
     * @param args   ARGV
     * @param <T>    返回类型
     * @return 脚本结果
     */
    public <T> T execute(RedisScript<T> script, List<String> keys, Object... args) {
        return stringRedisTemplate.execute(script, keys, args);
    }

    /**
     * 执行仅含单 Key 的 Lua 脚本。
     *
     * @param script 脚本
     * @param key    KEYS[1]
     * @param args   ARGV
     * @param <T>    返回类型
     * @return 脚本结果
     */
    public <T> T execute(RedisScript<T> script, String key, Object... args) {
        return execute(script, Collections.singletonList(key), args);
    }

    /**
     * SCAN 游标（调用方负责关闭）。
     *
     * @param options SCAN 选项
     * @return 游标
     */
    public Cursor<String> scan(ScanOptions options) {
        return stringRedisTemplate.scan(options);
    }

    /**
     * 按 pattern 扫描 Key（小数据量场景；大数据请用游标自行处理）。
     *
     * @param pattern 匹配模式
     * @param count   每次提示数量
     * @return 命中 Key 列表
     */
    public List<String> scanKeys(String pattern, long count) {
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(count).build();
        try (Cursor<String> cursor = scan(options)) {
            return cursor.stream().toList();
        }
    }

    /**
     * 便于日志 / 调试：可变参数拼成数组。
     */
    public static String[] args(String... values) {
        return values == null ? new String[0] : Arrays.copyOf(values, values.length);
    }
}

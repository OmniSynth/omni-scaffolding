package com.omni.scaffolding.modules.ops.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.exception.BusinessException;
import com.omni.scaffolding.modules.ops.dto.RedisExpireRequest;
import com.omni.scaffolding.modules.ops.dto.RedisInfoView;
import com.omni.scaffolding.modules.ops.dto.RedisKeyDetailView;
import com.omni.scaffolding.modules.ops.dto.RedisKeyView;
import com.omni.scaffolding.modules.ops.dto.RedisSetStringRequest;
import com.omni.scaffolding.infra.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Redis 运维：概览、SCAN 浏览、读写 String、TTL、删除。
 *
 * <p>不提供 FLUSHDB / FLUSHALL；大 value 截断返回，避免拖垮管理端。
 */
@Service
@Profile("!test")
@RequiredArgsConstructor
public class RedisOpsService {

    private static final int MAX_SCAN = 200;
    private static final int MAX_VALUE_CHARS = 4_000;
    private static final int MAX_COLLECTION_ITEMS = 100;

    private final RedisService redisService;
    private final ObjectMapper objectMapper;

    /**
     * Redis 运行概览。
     *
     * @return 版本、内存、连接数等
     */
    public RedisInfoView info() {
        StringRedisTemplate template = redisService.template();
        Properties props = template.execute((RedisConnection connection) -> connection.serverCommands().info());
        Long dbSize = template.execute(RedisConnection::dbSize);
        RedisInfoView view = new RedisInfoView();
        view.setRedisVersion(prop(props, "redis_version"));
        view.setMode(prop(props, "redis_mode"));
        view.setOs(prop(props, "os"));
        view.setUptimeInSeconds(longProp(props, "uptime_in_seconds"));
        view.setConnectedClients(longProp(props, "connected_clients"));
        view.setUsedMemory(longProp(props, "used_memory"));
        view.setUsedMemoryHuman(prop(props, "used_memory_human"));
        view.setMaxMemory(longProp(props, "maxmemory"));
        view.setMaxMemoryHuman(prop(props, "maxmemory_human"));
        view.setInstantaneousOpsPerSec(longProp(props, "instantaneous_ops_per_sec"));
        view.setRole(prop(props, "role"));
        view.setTotalKeys(dbSize == null ? 0L : dbSize);
        Map<String, String> extras = new LinkedHashMap<>();
        putExtra(extras, props, "used_memory_peak_human");
        putExtra(extras, props, "mem_fragmentation_ratio");
        putExtra(extras, props, "total_connections_received");
        putExtra(extras, props, "total_commands_processed");
        putExtra(extras, props, "expired_keys");
        putExtra(extras, props, "evicted_keys");
        putExtra(extras, props, "keyspace_hits");
        putExtra(extras, props, "keyspace_misses");
        view.setExtras(extras);
        return view;
    }

    /**
     * SCAN 浏览 Key。
     *
     * @param pattern 可选，匹配模式，默认 {@code *}
     * @param limit   可选，返回条数上限
     * @return Key 列表
     */
    public List<RedisKeyView> scanKeys(String pattern, Integer limit) {
        int max = limit == null ? 50 : Math.min(Math.max(limit, 1), MAX_SCAN);
        String pat = StringUtils.hasText(pattern) ? pattern.trim() : "*";
        List<RedisKeyView> rows = new ArrayList<>();
        ScanOptions options = ScanOptions.scanOptions().match(pat).count(Math.min(max, 50)).build();
        try (Cursor<String> cursor = redisService.scan(options)) {
            while (cursor.hasNext() && rows.size() < max) {
                rows.add(toKeyView(cursor.next()));
            }
        }
        return rows;
    }

    /**
     * Key 详情。
     *
     * @param key Key 名称
     * @return 类型、TTL、值（大 value 截断）
     */
    public RedisKeyDetailView detail(String key) {
        String k = requireKey(key);
        StringRedisTemplate template = redisService.template();
        DataType type = template.type(k);
        if (type == null || type == DataType.NONE) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Key 不存在");
        }
        RedisKeyDetailView view = new RedisKeyDetailView();
        view.setKey(k);
        view.setType(type.code());
        view.setTtlSeconds(redisService.getExpireSeconds(k));
        fillValue(view, k, type);
        return view;
    }

    /**
     * 写入 / 覆盖 String 类型 Key。
     *
     * @param request 写入请求
     * @return 更新后的 Key 详情
     */
    public RedisKeyDetailView setString(RedisSetStringRequest request) {
        String key = requireKey(request.getKey());
        String value = request.getValue();
        Long ttl = request.getTtlSeconds();
        if (ttl != null && ttl > 0) {
            redisService.set(key, value, ttl);
        } else {
            redisService.set(key, value);
        }
        return detail(key);
    }

    /**
     * 设置 / 清除 TTL。
     *
     * @param request 过期请求
     * @return Key 摘要
     */
    public RedisKeyView expire(RedisExpireRequest request) {
        String key = requireKey(request.getKey());
        if (!redisService.hasKey(key)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Key 不存在");
        }
        Long ttl = request.getTtlSeconds();
        if (ttl == null || ttl <= 0) {
            redisService.persist(key);
        } else if (!redisService.expire(key, ttl)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "设置过期时间失败");
        }
        return toKeyView(key);
    }

    /**
     * 批量删除 Key。
     *
     * @param keys 待删除 Key 列表
     * @return 实际删除数量
     */
    public long deleteKeys(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return 0L;
        }
        List<String> normalized = keys.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .limit(100)
                .toList();
        if (normalized.isEmpty()) {
            return 0L;
        }
        return redisService.delete(normalized);
    }

    /**
     * 按 Redis 数据类型读取键值并写入详情视图（含长度限制与截断标记）。
     *
     * @param view 键详情视图
     * @param key  Redis 键名
     * @param type 数据类型
     */
    private void fillValue(RedisKeyDetailView view, String key, DataType type) {
        StringRedisTemplate template = redisService.template();
        switch (type) {
            case STRING -> {
                String value = redisService.get(key);
                view.setSize(value == null ? 0L : (long) value.length());
                view.setValue(truncate(value));
                view.setTruncated(value != null && value.length() > MAX_VALUE_CHARS);
            }
            case HASH -> {
                Map<Object, Object> entries = redisService.hGetAll(key);
                view.setSize((long) entries.size());
                Map<String, String> limited = new LinkedHashMap<>();
                int i = 0;
                for (Map.Entry<Object, Object> e : entries.entrySet()) {
                    if (i++ >= MAX_COLLECTION_ITEMS) {
                        break;
                    }
                    limited.put(String.valueOf(e.getKey()), truncatePlain(String.valueOf(e.getValue()), 500));
                }
                view.setTruncated(entries.size() > MAX_COLLECTION_ITEMS);
                view.setValue(toJson(limited));
            }
            case LIST -> {
                Long size = template.opsForList().size(key);
                view.setSize(size == null ? 0L : size);
                List<String> range = template.opsForList().range(key, 0, MAX_COLLECTION_ITEMS - 1L);
                view.setTruncated(size != null && size > MAX_COLLECTION_ITEMS);
                view.setValue(toJson(range == null ? List.of() : range.stream()
                        .map(v -> truncatePlain(v, 500))
                        .toList()));
            }
            case SET -> {
                long size = redisService.sCard(key);
                view.setSize(size);
                Set<String> members = redisService.sMembers(key);
                List<String> limited = new ArrayList<>();
                int i = 0;
                for (String m : members) {
                    if (i++ >= MAX_COLLECTION_ITEMS) {
                        break;
                    }
                    limited.add(truncatePlain(m, 500));
                }
                view.setTruncated(members.size() > MAX_COLLECTION_ITEMS);
                view.setValue(toJson(limited));
            }
            case ZSET -> {
                Long size = template.opsForZSet().zCard(key);
                view.setSize(size == null ? 0L : size);
                Set<ZSetOperations.TypedTuple<String>> tuples =
                        template.opsForZSet().rangeWithScores(key, 0, MAX_COLLECTION_ITEMS - 1L);
                List<Map<String, Object>> limited = new ArrayList<>();
                if (tuples != null) {
                    for (ZSetOperations.TypedTuple<String> t : tuples) {
                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put("value", truncatePlain(t.getValue(), 500));
                        row.put("score", t.getScore());
                        limited.add(row);
                    }
                    view.setTruncated(size != null && size > MAX_COLLECTION_ITEMS);
                }
                view.setValue(toJson(limited));
            }
            default -> {
                view.setSize(0L);
                view.setValue("");
                view.setTruncated(false);
            }
        }
    }

    /**
     * 将 Redis 键名转为列表项视图（类型、TTL）。
     *
     * @param key Redis 键名
     * @return 键摘要视图
     */
    private RedisKeyView toKeyView(String key) {
        RedisKeyView view = new RedisKeyView();
        view.setKey(key);
        DataType type = redisService.template().type(key);
        view.setType(type == null ? "none" : type.code());
        view.setTtlSeconds(redisService.getExpireSeconds(key));
        return view;
    }

    /**
     * 校验 Redis 键非空且长度合法。
     *
     * @param key 原始键名
     * @return trim 后的键名
     */
    private static String requireKey(String key) {
        if (!StringUtils.hasText(key)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Key 不能为空");
        }
        String trimmed = key.trim();
        if (trimmed.length() > 512) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Key 长度不能超过 512");
        }
        return trimmed;
    }

    /**
     * 截断字符串值至 {@link #MAX_VALUE_CHARS}。
     *
     * @param value 原始值
     * @return 截断后的值，{@code null} 输入返回 {@code null}
     */
    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() <= MAX_VALUE_CHARS ? value : value.substring(0, MAX_VALUE_CHARS);
    }

    /**
     * 截断字符串并在超出时追加省略号。
     *
     * @param value 原始值
     * @param max   最大长度
     * @return 截断后的值，{@code null} 输入返回 {@code null}
     */
    private static String truncatePlain(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max) + "...";
    }

    /**
     * 将对象序列化为 JSON；失败时退回 {@code String.valueOf}。
     *
     * @param value 待序列化对象
     * @return JSON 字符串
     */
    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return String.valueOf(value);
        }
    }

    /**
     * 从 {@link Properties} 读取字符串属性。
     *
     * @param props 属性集
     * @param key   属性键
     * @return 字符串值，不存在时 {@code null}
     */
    private static String prop(Properties props, String key) {
        if (props == null) {
            return null;
        }
        Object value = props.get(key);
        return value == null ? null : String.valueOf(value);
    }

    /**
     * 从 {@link Properties} 读取长整型属性。
     *
     * @param props 属性集
     * @param key   属性键
     * @return 解析结果，缺失或非法时 {@code null}
     */
    private static Long longProp(Properties props, String key) {
        String value = prop(props, key);
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * 将非空属性写入扩展信息映射。
     *
     * @param extras 扩展信息映射
     * @param props  属性集
     * @param key    属性键
     */
    private static void putExtra(Map<String, String> extras, Properties props, String key) {
        String value = prop(props, key);
        if (StringUtils.hasText(value)) {
            extras.put(key, value);
        }
    }
}

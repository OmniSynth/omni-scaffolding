package com.omni.scaffolding.security.online;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.cache.RedisKeys;
import com.omni.scaffolding.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 在线会话管理：登录登记、列表、主动登出与强制踢下线。
 *
 * <p>Redis Key 定义见 {@link RedisKeys}（online*）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OnlineSessionService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 登录成功后登记在线会话。
     *
     * @param jti       JWT ID，会话主键
     * @param userId    用户 ID
     * @param username  登录用户名
     * @param deptId    部门 ID
     * @param ip        登录 IP
     * @param userAgent 客户端 User-Agent
     * @param expireAt  令牌过期时间
     */
    public void register(String jti,
                         Long userId,
                         String username,
                         Long deptId,
                         String ip,
                         String userAgent,
                         Instant expireAt) {
        if (!StringUtils.hasText(jti) || userId == null || expireAt == null) {
            return;
        }
        long ttlMs = expireAt.toEpochMilli() - Instant.now().toEpochMilli();
        if (ttlMs <= 0) {
            return;
        }
        OnlineSession session = new OnlineSession();
        session.setJti(jti);
        session.setUserId(userId);
        session.setUsername(username);
        session.setDeptId(deptId);
        session.setIp(ip);
        session.setUserAgent(truncate(userAgent, 512));
        session.setLoginTime(Instant.now().toEpochMilli());
        session.setExpireAt(expireAt.toEpochMilli());

        String json = toJson(session);
        Duration ttl = Duration.ofMillis(ttlMs);
        stringRedisTemplate.opsForValue().set(RedisKeys.onlineSession(jti), json, ttl);
        stringRedisTemplate.opsForSet().add(RedisKeys.onlineUser(userId), jti);
        stringRedisTemplate.expire(RedisKeys.onlineUser(userId), ttl);
        stringRedisTemplate.opsForSet().add(RedisKeys.ONLINE_INDEX, jti);
    }

    /**
     * 会话是否仍有效：未进入踢下线 / 主动登出黑名单。
     *
     * <p>{@code jti} 为空时视为旧令牌，放行（兼容升级过渡）。
     * Redis 会话 Key 仅用于在线列表展示，撤销以黑名单为准。
     *
     * @param jti JWT ID
     * @return {@code true} 未在黑名单中
     */
    public boolean isActive(String jti) {
        if (!StringUtils.hasText(jti)) {
            return true;
        }
        return !Boolean.TRUE.equals(stringRedisTemplate.hasKey(RedisKeys.onlineBlacklist(jti)));
    }

    /**
     * 列出全部在线会话，按登录时间倒序。
     *
     * @return 在线会话列表
     */
    public List<OnlineSession> listOnline() {
        Set<String> jtIs = stringRedisTemplate.opsForSet().members(RedisKeys.ONLINE_INDEX);
        if (jtIs == null || jtIs.isEmpty()) {
            return List.of();
        }
        List<OnlineSession> sessions = new ArrayList<>();
        List<String> stale = new ArrayList<>();
        for (String jti : jtIs) {
            String json = stringRedisTemplate.opsForValue().get(RedisKeys.onlineSession(jti));
            if (!StringUtils.hasText(json)) {
                stale.add(jti);
                continue;
            }
            OnlineSession session = fromJson(json);
            if (session != null) {
                sessions.add(session);
            } else {
                stale.add(jti);
            }
        }
        if (!stale.isEmpty()) {
            stringRedisTemplate.opsForSet().remove(RedisKeys.ONLINE_INDEX, stale.toArray());
        }
        sessions.sort(Comparator.comparing(OnlineSession::getLoginTime, Comparator.nullsLast(Long::compareTo)).reversed());
        return sessions;
    }

    /**
     * 按 jti 强制下线（管理员踢人 / 主动登出共用）。
     *
     * @param jti JWT ID
     */
    public void kickByJti(String jti) {
        if (!StringUtils.hasText(jti)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "会话标识不能为空");
        }
        String json = stringRedisTemplate.opsForValue().get(RedisKeys.onlineSession(jti));
        OnlineSession session = fromJson(json);
        long ttlMs = resolveTtlMs(session, jti);
        blacklist(jti, ttlMs);
        removeSessionKeys(jti, session == null ? null : session.getUserId());
    }

    /**
     * 踢掉某用户全部在线会话。
     *
     * @return 实际踢下线的会话数
     */
    public int kickByUserId(Long userId) {
        if (userId == null) {
            return 0;
        }
        Set<String> jtIs = stringRedisTemplate.opsForSet().members(RedisKeys.onlineUser(userId));
        if (jtIs == null || jtIs.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (String jti : jtIs) {
            kickByJti(jti);
            count++;
        }
        return count;
    }

    /**
     * 当前令牌主动登出。
     *
     * @param jti 当前 JWT ID
     */
    public void logout(String jti) {
        if (!StringUtils.hasText(jti)) {
            return;
        }
        kickByJti(jti);
    }

    /**
     * 将 jti 写入踢下线黑名单，TTL 与令牌剩余有效期对齐。
     *
     * @param jti   JWT ID
     * @param ttlMs 黑名单存活毫秒，≤0 时默认 1 小时
     */
    private void blacklist(String jti, long ttlMs) {
        if (ttlMs <= 0) {
            ttlMs = TimeUnit.HOURS.toMillis(1);
        }
        stringRedisTemplate.opsForValue().set(RedisKeys.onlineBlacklist(jti), "1", Duration.ofMillis(ttlMs));
    }

    /**
     * 清理在线会话相关 Redis Key（会话详情、全局索引、用户索引）。
     *
     * @param jti    JWT ID
     * @param userId 用户 ID，可为 null
     */
    private void removeSessionKeys(String jti, Long userId) {
        stringRedisTemplate.delete(RedisKeys.onlineSession(jti));
        stringRedisTemplate.opsForSet().remove(RedisKeys.ONLINE_INDEX, jti);
        if (userId != null) {
            stringRedisTemplate.opsForSet().remove(RedisKeys.onlineUser(userId), jti);
        }
    }

    /**
     * 解析黑名单应使用的 TTL：优先会话过期时间，否则读 Redis Key 剩余 TTL。
     *
     * @param session 在线会话，可为 null
     * @param jti     JWT ID
     * @return 剩余毫秒，无法解析时为 0
     */
    private long resolveTtlMs(OnlineSession session, String jti) {
        if (session != null && session.getExpireAt() != null) {
            return Math.max(session.getExpireAt() - Instant.now().toEpochMilli(), 0L);
        }
        Long ttl = stringRedisTemplate.getExpire(RedisKeys.onlineSession(jti), TimeUnit.MILLISECONDS);
        return ttl == null || ttl < 0 ? 0L : ttl;
    }

    /**
     * 在线会话序列化为 JSON 字符串。
     *
     * @param session 在线会话
     * @return JSON 文本
     */
    private String toJson(OnlineSession session) {
        try {
            return objectMapper.writeValueAsString(session);
        } catch (JsonProcessingException ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "在线会话序列化失败");
        }
    }

    /**
     * 从 JSON 反序列化在线会话；解析失败返回 null 并记录告警。
     *
     * @param json Redis 中存储的 JSON
     * @return 在线会话，无效时为 null
     */
    private OnlineSession fromJson(String json) {
        if (!StringUtils.hasText(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, OnlineSession.class);
        } catch (JsonProcessingException ex) {
            log.warn("parse online session failed: {}", ex.getMessage());
            return null;
        }
    }

    /**
     * 截断字符串至指定最大长度。
     *
     * @param value 原始字符串
     * @param max   最大字符数
     * @return 截断后的字符串；{@code value == null} 时返回 null
     */
    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}

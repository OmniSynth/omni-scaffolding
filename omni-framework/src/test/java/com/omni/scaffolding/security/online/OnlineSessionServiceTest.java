package com.omni.scaffolding.security.online;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omni.scaffolding.common.cache.RedisKeys;
import com.omni.scaffolding.infra.redis.RedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OnlineSessionServiceTest {

    @Mock
    private RedisService redisService;

    private OnlineSessionService service;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        service = new OnlineSessionService(redisService, objectMapper);
    }

    @Test
    void trimToMaxSessions_skipsWhenMaxDevicesNonPositive() {
        assertThat(service.trimToMaxSessions(1L, 0)).isZero();
        assertThat(service.trimToMaxSessions(1L, -1)).isZero();
        verify(redisService, never()).sMembers(anyString());
    }

    @Test
    void trimToMaxSessions_kicksOldestWhenOverLimit() throws Exception {
        Long userId = 10L;
        String oldest = "jti-old";
        String mid = "jti-mid";
        String newest = "jti-new";
        Set<String> jtIs = new LinkedHashSet<>();
        jtIs.add(newest);
        jtIs.add(oldest);
        jtIs.add(mid);

        when(redisService.sMembers(RedisKeys.onlineUser(userId))).thenReturn(jtIs);
        when(redisService.hasKey(anyString())).thenReturn(false);
        when(redisService.get(RedisKeys.onlineSession(oldest))).thenReturn(sessionJson(oldest, userId, 1000L));
        when(redisService.get(RedisKeys.onlineSession(mid))).thenReturn(sessionJson(mid, userId, 2000L));
        when(redisService.get(RedisKeys.onlineSession(newest))).thenReturn(sessionJson(newest, userId, 3000L));

        int kicked = service.trimToMaxSessions(userId, 2);

        assertThat(kicked).isEqualTo(1);
        ArgumentCaptor<String> blacklistKey = ArgumentCaptor.forClass(String.class);
        verify(redisService, atLeastOnce()).set(blacklistKey.capture(), eq("1"), any(Duration.class));
        assertThat(blacklistKey.getAllValues()).contains(RedisKeys.onlineBlacklist(oldest));
        assertThat(blacklistKey.getAllValues()).doesNotContain(RedisKeys.onlineBlacklist(newest));
        assertThat(blacklistKey.getAllValues()).doesNotContain(RedisKeys.onlineBlacklist(mid));
        verify(redisService).delete(RedisKeys.onlineSession(oldest));
        verify(redisService, never()).delete(RedisKeys.onlineSession(newest));
        verify(redisService, never()).delete(RedisKeys.onlineSession(mid));
    }

    @Test
    void trimToMaxSessions_noopWhenWithinLimit() throws Exception {
        Long userId = 11L;
        Set<String> jtIs = Set.of("a", "b");
        when(redisService.sMembers(RedisKeys.onlineUser(userId))).thenReturn(jtIs);

        assertThat(service.trimToMaxSessions(userId, 2)).isZero();
        verify(redisService, never()).hasKey(anyString());
        verify(redisService, never()).set(anyString(), anyString(), any(Duration.class));
    }

    private String sessionJson(String jti, Long userId, long loginTime) throws Exception {
        OnlineSession session = new OnlineSession();
        session.setJti(jti);
        session.setUserId(userId);
        session.setUsername("u");
        session.setLoginTime(loginTime);
        session.setExpireAt(System.currentTimeMillis() + 3_600_000L);
        return objectMapper.writeValueAsString(session);
    }
}

package com.omni.scaffolding.modules.open.security;

import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.cache.RedisKeys;
import com.omni.scaffolding.common.exception.BusinessException;
import com.omni.scaffolding.common.util.DigestUtils;
import com.omni.scaffolding.infra.ratelimit.RedisRateLimiter;
import com.omni.scaffolding.modules.open.dto.endpoint.OpenEndpointView;
import com.omni.scaffolding.modules.open.entity.OpenApiClient;
import com.omni.scaffolding.modules.open.mapper.OpenApiClientQueryMapper;
import com.omni.scaffolding.modules.open.mapper.OpenApiEndpointQueryMapper;
import com.omni.scaffolding.modules.open.repository.OpenApiClientRepository;
import com.omni.scaffolding.security.open.OpenApiAccessGuard;
import com.omni.scaffolding.security.open.OpenApiClientPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 开放 API 访问守卫实现。
 *
 * <p>校验顺序：API Key → 启停/过期 → IP 白名单（空不限）→ 接口绑定 → QPS → 日限额。
 * 失败抛 {@link BusinessException}，由过滤器写成统一 JSON。
 */
@Service
@RequiredArgsConstructor
public class OpenApiAccessGuardImpl implements OpenApiAccessGuard {

    private static final DateTimeFormatter DAY = DateTimeFormatter.BASIC_ISO_DATE;
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final OpenApiClientRepository clientRepository;
    private final OpenApiClientQueryMapper clientQueryMapper;
    private final OpenApiEndpointQueryMapper endpointQueryMapper;
    private final RedisRateLimiter redisRateLimiter;

    /**
     * {@inheritDoc}
     */
    @Override
    public OpenApiClientPrincipal authenticate(String apiKey, String clientIp, String method, String path) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "缺少 X-Api-Key");
        }
        String hash = DigestUtils.sha256Hex(apiKey.trim());
        OpenApiClient client = clientRepository.findByApiKeyHashAndDeleted(hash, 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "无效的 API Key"));
        if (!Boolean.TRUE.equals(client.getStatus())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "客户端已停用");
        }
        if (client.getExpireAt() != null && client.getExpireAt().isBefore(java.time.Instant.now())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "客户端已过期");
        }

        List<String> ips = clientQueryMapper.listIpsByClientId(client.getId());
        if (!ips.isEmpty()) {
            String ip = clientIp == null ? "" : clientIp.trim();
            boolean allowed = ips.stream().anyMatch(allowedIp -> allowedIp.equalsIgnoreCase(ip));
            if (!allowed) {
                throw new BusinessException(ErrorCode.FORBIDDEN, "IP 不在白名单内");
            }
        }

        List<OpenEndpointView> endpoints = endpointQueryMapper.listEnabledByClientId(client.getId());
        if (!isPathAuthorized(endpoints, method, path)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "未授权访问该接口");
        }

        Integer qps = client.getQpsLimit();
        if (qps != null && qps > 0) {
            if (!redisRateLimiter.tryAcquire(RedisKeys.openApiQps(client.getId()), qps, 1)) {
                throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS, "超过 QPS 限制");
            }
        }

        Integer daily = client.getDailyLimit();
        if (daily != null && daily > 0) {
            String day = LocalDate.now(ZONE).format(DAY);
            int windowSeconds = secondsUntilTomorrow();
            if (!redisRateLimiter.tryAcquire(RedisKeys.openApiDay(client.getId(), day), daily, windowSeconds)) {
                throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS, "超过当日调用次数限制");
            }
        }

        return new OpenApiClientPrincipal(client.getId(), client.getName(), client.getAccessKey(), Set.of());
    }

    /**
     * 判断 method + path 是否命中任一已绑定且启用的接口模式。
     */
    private static boolean isPathAuthorized(List<OpenEndpointView> endpoints, String method, String path) {
        if (endpoints == null || endpoints.isEmpty()) {
            return false;
        }
        String httpMethod = method == null ? "" : method.trim().toUpperCase(Locale.ROOT);
        for (OpenEndpointView ep : endpoints) {
            String epMethod = ep.getHttpMethod() == null ? "*" : ep.getHttpMethod().trim().toUpperCase(Locale.ROOT);
            boolean methodOk = "*".equals(epMethod) || epMethod.equals(httpMethod);
            if (methodOk && PATH_MATCHER.match(ep.getPathPattern(), path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 日限额窗口：到次日 0 点的秒数（至少 60，避免临近零点过短）。
     */
    private static int secondsUntilTomorrow() {
        LocalDateTime now = LocalDateTime.now(ZONE);
        LocalDateTime tomorrow = now.toLocalDate().plusDays(1).atStartOfDay();
        long seconds = Duration.between(now, tomorrow).getSeconds();
        return (int) Math.max(60, Math.min(seconds + 60, Integer.MAX_VALUE));
    }
}

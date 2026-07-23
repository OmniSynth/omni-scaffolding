package com.omni.scaffolding.infra.ratelimit;

import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.exception.BusinessException;
import com.omni.scaffolding.security.AuthUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * API 入口分布式限流拦截器。
 *
 * <p>已登录按 userId 计数，未登录按 IP 计数；阈值见 {@code omni.rate-limit.*}。
 * 与方法上的 Resilience4j {@code @RateLimiter} 互补：前者管集群入口，后者管单实例方法级。
 */
@Component
@RequiredArgsConstructor
public class ApiRateLimitInterceptor implements HandlerInterceptor {

    private final RedisRateLimiter redisRateLimiter;

    @Value("${omni.rate-limit.enabled:true}")
    private boolean enabled;

    @Value("${omni.rate-limit.limit-for-period:100}")
    private int limitForPeriod;

    /**
     * 请求前限流检查；超限则抛出 {@link BusinessException}。
     *
     * @param request  当前请求
     * @param response 当前响应
     * @param handler  目标处理器
     * @return {@code true} 放行
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!enabled) {
            return true;
        }
        String key = resolveKey(request);
        if (!redisRateLimiter.tryAcquire(key, limitForPeriod, 1)) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS, "请求过于频繁，请稍后再试");
        }
        return true;
    }

    /**
     * 解析限流维度：已登录按 userId，未登录按客户端 IP。
     *
     * @param request 当前请求
     * @return Redis 限流 Key 后缀
     */
    private String resolveKey(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthUser user) {
            return "user:" + user.getId();
        }
        return "ip:" + request.getRemoteAddr();
    }
}

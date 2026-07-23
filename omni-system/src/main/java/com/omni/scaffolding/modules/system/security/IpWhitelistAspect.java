package com.omni.scaffolding.modules.system.security;

import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.exception.BusinessException;
import com.omni.scaffolding.common.security.IpWhitelist;
import com.omni.scaffolding.common.util.IpUtils;
import com.omni.scaffolding.modules.system.service.IpWhitelistService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * IP 白名单切面。
 *
 * <p>拦截标注了 {@link IpWhitelist} 的 Controller 类或方法：
 * 方法注解优先于类注解；不在白名单则拒绝；放行后记录今日访问次数。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class IpWhitelistAspect {

    private final IpWhitelistService ipWhitelistService;

    /**
     * 环绕通知：解析注解 → 取客户端 IP → 校验 → 记次 → 放行。
     *
     * @param pjp 连接点
     * @return 原方法返回值
     * @throws Throwable 业务拒绝或原方法异常
     */
    @Around("@within(com.omni.scaffolding.common.security.IpWhitelist) || "
            + "@annotation(com.omni.scaffolding.common.security.IpWhitelist)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        IpWhitelist ann = resolveAnnotation(pjp);
        if (ann == null) {
            return pjp.proceed();
        }

        HttpServletRequest request = currentRequest();
        if (request == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "IP 不在白名单内");
        }
        String clientIp = IpUtils.resolveClientIp(
                request.getHeader("X-Forwarded-For"),
                request.getHeader("X-Real-IP"),
                request.getRemoteAddr());

        if (!ipWhitelistService.isAllowed(clientIp, ann.value())) {
            log.warn("IP whitelist denied: ip={}, uri={}", clientIp, request.getRequestURI());
            throw new BusinessException(ErrorCode.FORBIDDEN, "IP 不在白名单内");
        }

        long todayCount = ipWhitelistService.recordVisit(clientIp);
        log.debug("IP whitelist allowed: ip={}, todayCount={}, uri={}", clientIp, todayCount, request.getRequestURI());
        return pjp.proceed();
    }

    /**
     * 解析生效注解：方法级优先，否则取声明类上的注解。
     *
     * @param pjp 连接点
     * @return 生效的 {@link IpWhitelist}，均无则 {@code null}
     */
    private static IpWhitelist resolveAnnotation(ProceedingJoinPoint pjp) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        IpWhitelist onMethod = AnnotationUtils.findAnnotation(method, IpWhitelist.class);
        if (onMethod != null) {
            return onMethod;
        }
        return AnnotationUtils.findAnnotation(signature.getDeclaringType(), IpWhitelist.class);
    }

    /**
     * 从 Spring 请求上下文获取当前 HTTP 请求。
     *
     * @return 当前请求，无 Web 上下文时 {@code null}
     */
    private static HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs == null ? null : attrs.getRequest();
    }
}

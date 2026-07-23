package com.omni.scaffolding.modules.system.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omni.scaffolding.common.audit.OperLog;
import com.omni.scaffolding.common.util.IpUtils;
import com.omni.scaffolding.modules.system.entity.SysOperLog;
import com.omni.scaffolding.modules.system.service.OperLogService;
import com.omni.scaffolding.security.AuthUser;
import com.omni.scaffolding.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 操作日志切面：拦截标注 {@link OperLog} 的方法，记录请求摘要与耗时。
 *
 * <p>异步落库由 {@link OperLogService} 完成；敏感字段（密码等）自动脱敏。
 */
@Aspect
@Component
@RequiredArgsConstructor
public class OperLogAspect {

    private static final int MAX_PARAMS = 2000;
    private static final int MAX_ERROR = 1000;
    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "password", "oldpassword", "newpassword", "confirmpassword", "passwordhash");

    private final OperLogService operLogService;
    private final ObjectMapper objectMapper;

    /**
     * 环绕通知：执行业务方法并在 finally 中持久化操作日志。
     *
     * @param pjp 连接点
     * @return 原方法返回值
     * @throws Throwable 原方法抛出的异常
     */
    @Around("@annotation(com.omni.scaffolding.common.audit.OperLog)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        OperLog ann = method.getAnnotation(OperLog.class);

        Throwable error = null;
        Object result;
        try {
            result = pjp.proceed();
            return result;
        } catch (Throwable ex) {
            error = ex;
            throw ex;
        } finally {
            persist(pjp, signature, ann, System.currentTimeMillis() - start, error);
        }
    }

    /**
     * 组装操作日志实体并异步落库。
     *
     * @param pjp       连接点
     * @param signature 方法签名
     * @param ann       操作日志注解
     * @param costMs    执行耗时（毫秒）
     * @param error     业务异常，成功时为 {@code null}
     */
    private void persist(ProceedingJoinPoint pjp,
                         MethodSignature signature,
                         OperLog ann,
                         long costMs,
                         Throwable error) {
        if (ann == null) {
            return;
        }
        SysOperLog row = new SysOperLog();
        row.setModule(ann.module());
        row.setAction(ann.action());
        row.setMethod(signature.getDeclaringType().getSimpleName() + "#" + signature.getName());
        row.setCostMs((int) Math.min(costMs, Integer.MAX_VALUE));
        row.setStatus(error == null ? "SUCCESS" : "FAIL");
        if (error != null) {
            row.setErrorMsg(truncate(error.getMessage(), MAX_ERROR));
        }

        try {
            AuthUser user = SecurityUtils.requireAuthUser();
            row.setUserId(user.getId());
            row.setUsername(user.getUsername());
        } catch (Exception ignored) {
            // 匿名或未登录场景
        }

        HttpServletRequest request = currentRequest();
        if (request != null) {
            row.setRequestUri(truncate(request.getRequestURI(), 255));
            row.setRequestMethod(request.getMethod());
            row.setIp(IpUtils.resolveClientIp(
                    request.getHeader("X-Forwarded-For"),
                    request.getHeader("X-Real-IP"),
                    request.getRemoteAddr()));
        }

        if (ann.saveParams()) {
            row.setParams(buildParams(signature.getParameterNames(), pjp.getArgs()));
        }

        operLogService.save(row);
    }

    /**
     * 将方法参数序列化为 JSON 并脱敏敏感字段。
     *
     * @param names 参数名数组
     * @param args  参数值数组
     * @return JSON 字符串，无参数时 {@code null}
     */
    private String buildParams(String[] names, Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        try {
            Map<String, Object> map = new LinkedHashMap<>();
            for (int i = 0; i < args.length; i++) {
                String key = names != null && i < names.length ? names[i] : ("arg" + i);
                Object val = args[i];
                if (val instanceof HttpServletRequest || val instanceof MultipartFile) {
                    continue;
                }
                if (SENSITIVE_KEYS.contains(key.toLowerCase())) {
                    map.put(key, "******");
                } else {
                    map.put(key, val);
                }
            }
            return truncate(maskSensitiveJson(objectMapper.writeValueAsString(map)), MAX_PARAMS);
        } catch (Exception ex) {
            return truncate(String.valueOf(args.length) + " args", MAX_PARAMS);
        }
    }

    /**
     * 脱敏 JSON 中常见密码字段（含嵌套对象）。
     */
    private static String maskSensitiveJson(String json) {
        if (json == null || json.isBlank()) {
            return json;
        }
        return json.replaceAll(
                "(?i)(\"(?:password|oldPassword|newPassword|confirmPassword|passwordHash)\"\\s*:\\s*)\"[^\"]*\"",
                "$1\"******\"");
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

    /**
     * 截断字符串至指定最大长度。
     *
     * @param value 原始字符串
     * @param max   最大长度
     * @return 截断后的字符串，{@code null} 输入返回 {@code null}
     */
    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() > max ? value.substring(0, max) : value;
    }
}

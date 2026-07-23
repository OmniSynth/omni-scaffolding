package com.omni.scaffolding.config;

import com.omni.scaffolding.common.desensitize.DesensitizeContext;
import com.omni.scaffolding.common.desensitize.WithoutDesensitize;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 识别 {@link WithoutDesensitize}，在响应序列化阶段跳过字段脱敏。
 *
 * <p>在 {@code preHandle} 开启、{@code afterCompletion} 清理 ThreadLocal，
 * 覆盖 Controller 返回后的 Jackson 写出窗口。
 */
@Component
public class DesensitizeInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (handler instanceof HandlerMethod method && shouldSkip(method)) {
            DesensitizeContext.ignore();
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        DesensitizeContext.clear();
    }

    /**
     * 判断 Controller 方法或类是否标注 {@link WithoutDesensitize}。
     *
     * @param method 处理器方法
     * @return {@code true} 应跳过脱敏
     */
    private static boolean shouldSkip(HandlerMethod method) {
        return method.hasMethodAnnotation(WithoutDesensitize.class)
                || method.getBeanType().isAnnotationPresent(WithoutDesensitize.class);
    }
}

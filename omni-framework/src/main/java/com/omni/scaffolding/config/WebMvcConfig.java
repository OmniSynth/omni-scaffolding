package com.omni.scaffolding.config;

import com.omni.scaffolding.infra.ratelimit.ApiRateLimitInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 扩展：注册全局限流拦截器等。
 *
 * <p>登录接口排除在外，避免暴力破解场景与正常登录互相干扰；登录侧另有 Resilience4j 限流注解。
 * 文件内容不再走匿名静态映射，统一经 {@code /api/system/files/{id}/content}（JWT 或短时签名）。
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final ApiRateLimitInterceptor apiRateLimitInterceptor;
    private final DesensitizeInterceptor desensitizeInterceptor;

    /**
     * 注册全局限流与脱敏拦截器。
     *
     * @param registry 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiRateLimitInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login", "/api/open/**");
        registry.addInterceptor(desensitizeInterceptor).addPathPatterns("/api/**");
    }
}

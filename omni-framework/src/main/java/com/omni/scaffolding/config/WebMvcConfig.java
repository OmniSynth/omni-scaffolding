package com.omni.scaffolding.config;

import com.omni.scaffolding.infra.ratelimit.ApiRateLimitInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

/**
 * Web MVC 扩展：注册全局限流拦截器、本地上传静态资源映射等。
 *
 * <p>登录接口排除在外，避免暴力破解场景与正常登录互相干扰；登录侧另有 Resilience4j 限流注解。
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final ApiRateLimitInterceptor apiRateLimitInterceptor;
    private final DesensitizeInterceptor desensitizeInterceptor;
    private final OmniUploadProperties uploadProperties;

    /**
     * 注册全局限流与脱敏拦截器。
     *
     * @param registry 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiRateLimitInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login");
        registry.addInterceptor(desensitizeInterceptor).addPathPatterns("/api/**");
    }

    /**
     * 将 {@code /uploads/**} 映射到本地上传目录，供头像等静态访问。
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Path.of(uploadProperties.getBaseDir()).toAbsolutePath().normalize().toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        String pattern = uploadProperties.getUrlPrefix().replaceAll("/$", "") + "/**";
        registry.addResourceHandler(pattern).addResourceLocations(location);
    }
}

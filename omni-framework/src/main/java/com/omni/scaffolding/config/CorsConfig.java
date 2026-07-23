package com.omni.scaffolding.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * 浏览器跨域：供本地 / 局域网 Vue 开发与生产站点访问后端。
 *
 * <p>与 {@code SecurityConfig} 中 {@code http.cors(Customizer.withDefaults())} 配合；
 * Origin 白名单见 {@code omni.security.cors.allowed-origin-patterns}。
 */
@Configuration
@RequiredArgsConstructor
public class CorsConfig {

    private final OmniSecurityProperties securityProperties;

    /**
     * CORS 配置源，供 Spring Security {@code http.cors()} 使用。
     *
     * @return 按配置放行 Origin 模式的 CORS 配置
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        List<String> patterns = securityProperties.getCors().getAllowedOriginPatterns().stream()
                .flatMap(p -> java.util.Arrays.stream(p.split(",")))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        if (CollectionUtils.isEmpty(patterns)) {
            throw new IllegalStateException("omni.security.cors.allowed-origin-patterns 不能为空");
        }
        config.setAllowedOriginPatterns(patterns);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("X-Trace-Id"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}

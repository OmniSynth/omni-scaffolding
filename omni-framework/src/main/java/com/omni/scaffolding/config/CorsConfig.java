package com.omni.scaffolding.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * 浏览器跨域：供本地 / 局域网 Vue 开发访问后端。
 *
 * <p>与 {@code SecurityConfig} 中 {@code http.cors(Customizer.withDefaults())} 配合；
 * 走 Vite proxy 时浏览器仍可能带 {@code Origin: http://192.168.x.x:5173}，需放行。
 */
@Configuration
public class CorsConfig {

    /**
     * CORS 配置源，供 Spring Security {@code http.cors()} 使用。
     *
     * @return 放行本机 / 局域网 Vite 开发端口的 CORS 配置
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // 开发：本机 + 局域网 IP 的 Vite 端口；生产建议收紧为具体域名
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "http://192.168.*.*:*",
                "http://10.*.*.*:*",
                "http://172.*.*.*:*"
        ));
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

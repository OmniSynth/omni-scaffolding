package com.omni.scaffolding.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 浏览器跨域：供本地 / 局域网 Vue 开发与生产站点访问后端。
 *
 * <p>与 {@code SecurityConfig} 中 {@code http.cors(Customizer.withDefaults())} 配合；
 * Origin 白名单见 {@code omni.security.cors.allowed-origin-patterns}。
 *
 * <p>配置了 {@code http://host} 时会自动补上 {@code https://host}（反之亦然），
 * 避免 Nginx 仅配 80、外层已上 HTTPS 时出现 {@code Invalid CORS request}。
 */
@Slf4j
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
        List<String> patterns = expandOriginPatterns(securityProperties.getCors().getAllowedOriginPatterns());
        if (CollectionUtils.isEmpty(patterns)) {
            throw new IllegalStateException("omni.security.cors.allowed-origin-patterns 不能为空");
        }
        log.info("CORS allowedOriginPatterns={}", patterns);

        CorsConfiguration config = new CorsConfiguration();
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

    /**
     * 展开逗号分隔项，并为仅含一种协议的 Origin 自动补上另一种协议。
     */
    static List<String> expandOriginPatterns(List<String> raw) {
        Set<String> out = new LinkedHashSet<>();
        if (raw == null) {
            return List.of();
        }
        for (String entry : raw) {
            if (entry == null || entry.isBlank()) {
                continue;
            }
            for (String part : entry.split(",")) {
                String s = part.trim();
                if (s.isEmpty()) {
                    continue;
                }
                if (s.startsWith("https://")) {
                    String host = s.substring("https://".length());
                    out.add("https://" + host);
                    out.add("http://" + host);
                } else if (s.startsWith("http://")) {
                    String host = s.substring("http://".length());
                    out.add("http://" + host);
                    out.add("https://" + host);
                } else {
                    // 裸域名：同时放行 http / https
                    out.add("http://" + s);
                    out.add("https://" + s);
                }
            }
        }
        return new ArrayList<>(out);
    }
}

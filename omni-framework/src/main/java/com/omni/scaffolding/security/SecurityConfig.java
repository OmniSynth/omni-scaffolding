package com.omni.scaffolding.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omni.scaffolding.common.api.ApiResponse;
import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.trace.TraceContext;
import com.omni.scaffolding.config.OmniSecurityProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

/**
 * Spring Security 主配置：无状态 JWT + 方法级鉴权 + 安全响应头。
 *
 * <ul>
 *   <li>关闭 Session / CSRF（纯 API + JWT 场景）</li>
 *   <li>白名单路径来自 {@link OmniSecurityProperties#getPermitAll()}</li>
 *   <li>未认证 / 无权限统一返回 {@link ApiResponse}，并带上 traceId</li>
 *   <li>XSS 输入清洗由 {@link com.omni.scaffolding.security.xss.XssFilter} 在 Security 链外更早执行</li>
 *   <li>响应侧补充 CSP / nosniff / frame deny 等浏览器防护头</li>
 * </ul>
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OmniSecurityProperties securityProperties;
    private final ObjectMapper objectMapper;

    /**
     * 主 Security 过滤链：无状态 JWT、白名单、统一 JSON 鉴权失败响应。
     *
     * @param http {@link HttpSecurity} 构建器
     * @return 已组装的过滤链
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                // 无状态：不创建 HttpSession，便于多实例扩缩
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 浏览器侧防护头（与 XssFilter 输入清洗互补）
                .headers(headers -> headers
                        .contentTypeOptions(Customizer.withDefaults())
                        // sameOrigin：允许本站 iframe 内嵌 Druid 监控页
                        .frameOptions(frame -> frame.sameOrigin())
                        .referrerPolicy(referrer -> referrer.policy(
                                ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        // 兼顾本机 Swagger UI（需 self + 少量 inline）；纯 API 网关前可再收紧
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; "
                                        + "script-src 'self' 'unsafe-inline'; "
                                        + "style-src 'self' 'unsafe-inline'; "
                                        + "img-src 'self' data:; "
                                        + "font-src 'self' data:; "
                                        + "connect-src 'self'; "
                                        + "object-src 'none'; "
                                        + "base-uri 'self'; "
                                        + "form-action 'self'; "
                                        + "frame-ancestors 'self'; "
                                        + "frame-src 'self'")))
                .authorizeHttpRequests(auth -> {
                    securityProperties.getPermitAll().forEach(path ->
                            auth.requestMatchers(path).permitAll());
                    auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                    auth.anyRequest().authenticated();
                })
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            objectMapper.writeValue(response.getOutputStream(),
                                    ApiResponse.fail(ErrorCode.UNAUTHORIZED).withTraceId(TraceContext.getTraceId()));
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(403);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            objectMapper.writeValue(response.getOutputStream(),
                                    ApiResponse.fail(ErrorCode.FORBIDDEN).withTraceId(TraceContext.getTraceId()));
                        }))
                // JWT 过滤器放在用户名密码过滤器之前，完成鉴权上下文填充
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * BCrypt 密码编码器。
     *
     * @return {@link PasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 暴露 {@link AuthenticationManager} 供登录等场景注入。
     *
     * @param configuration Spring Security 认证配置
     * @return 认证管理器
     * @throws Exception 配置异常
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}

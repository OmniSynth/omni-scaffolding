package com.omni.scaffolding.security.xss;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omni.scaffolding.config.OmniSecurityProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * XSS 防护过滤器：包装请求以清洗参数 / Header / JSON·文本 Body。
 *
 * <p>顺序紧随 {@code TraceIdFilter}，早于 Spring Security / JWT，保证后续链路读到的已是清洗后数据。
 * 可通过 {@code omni.security.xss.enabled=false} 关闭。
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
@RequiredArgsConstructor
public class XssFilter extends OncePerRequestFilter {

    private final OmniSecurityProperties securityProperties;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * XSS 关闭或路径命中排除列表时跳过。
     *
     * @param request 当前请求
     * @return {@code true} 不执行 XSS 清洗
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        OmniSecurityProperties.Xss xss = securityProperties.getXss();
        if (xss == null || !xss.isEnabled()) {
            return true;
        }
        String path = request.getRequestURI();
        List<String> excludes = xss.getExcludePathPatterns();
        if (excludes == null || excludes.isEmpty()) {
            return false;
        }
        return excludes.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    /**
     * 包装请求并执行 XSS 清洗。
     *
     * @param request     原始请求
     * @param response    当前响应
     * @param filterChain 过滤链
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        XssMode mode = securityProperties.getXss().getMode();
        XssHttpServletRequestWrapper wrapped = new XssHttpServletRequestWrapper(request, mode, objectMapper);
        filterChain.doFilter(wrapped, response);
    }
}

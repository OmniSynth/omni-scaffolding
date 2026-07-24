package com.omni.scaffolding.security.open;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omni.scaffolding.common.api.ApiResponse;
import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.exception.BusinessException;
import com.omni.scaffolding.common.security.OpenApiHeaders;
import com.omni.scaffolding.common.trace.TraceContext;
import com.omni.scaffolding.common.util.IpUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 开放 API 鉴权过滤器。
 *
 * <p>仅处理 {@code /api/open/**}，排除管理端 {@code /api/open/admin/**}（后者走 JWT）。
 * 校验委托 {@link OpenApiAccessGuard}；成功后写入 {@link OpenApiClientPrincipal}。
 */
@Component
@RequiredArgsConstructor
public class OpenApiAuthFilter extends OncePerRequestFilter {

    private final ObjectProvider<OpenApiAccessGuard> openApiAccessGuard;
    private final ObjectMapper objectMapper;

    /**
     * 非开放业务路径或管理端路径时跳过本过滤器。
     *
     * @param request 当前请求
     * @return {@code true} 表示不进入鉴权逻辑
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = pathWithinApplication(request);
        if (!path.startsWith(OpenApiHeaders.OPEN_PATH_PREFIX)) {
            return true;
        }
        return path.startsWith(OpenApiHeaders.ADMIN_PATH_PREFIX);
    }

    /**
     * 读取 {@code X-Api-Key}，完成守卫校验并填充 SecurityContext。
     *
     * @param request     当前请求
     * @param response    当前响应
     * @param filterChain 过滤链
     */
    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain)
            throws ServletException, IOException {
        OpenApiAccessGuard guard = openApiAccessGuard.getIfAvailable();
        String path = pathWithinApplication(request);
        String apiKey = request.getHeader(OpenApiHeaders.API_KEY);
        String clientIp = IpUtils.resolveClientIp(
                request.getHeader("X-Forwarded-For"),
                request.getHeader("X-Real-IP"),
                request.getRemoteAddr());
        try {
            OpenApiClientPrincipal principal = guard.authenticate(
                    apiKey, clientIp, request.getMethod(), path);
            List<SimpleGrantedAuthority> authorities = new ArrayList<>(
                    principal.getAuthorities().stream().map(SimpleGrantedAuthority::new).toList());
            if (authorities.isEmpty()) {
                authorities.add(new SimpleGrantedAuthority("OPEN_API"));
            }
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (BusinessException ex) {
            writeError(response, ex.getErrorCode(), ex.getMessage());
        } catch (Exception ex) {
            writeError(response, ErrorCode.INTERNAL_ERROR, ErrorCode.INTERNAL_ERROR.getMessage());
        }
    }

    /**
     * 去掉 context-path 后的应用内路径。
     */
    private static String pathWithinApplication(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String context = request.getContextPath();
        if (context != null && !context.isEmpty() && uri.startsWith(context)) {
            return uri.substring(context.length());
        }
        return uri;
    }

    /**
     * 写出统一 {@link ApiResponse} 错误体（过滤器阶段无法走全局异常处理器）。
     */
    private void writeError(HttpServletResponse response, ErrorCode code, String message) throws IOException {
        int status = code.getCode();
        if (status < 400 || status > 599) {
            status = 500;
        }
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(),
                ApiResponse.fail(code, message).withTraceId(TraceContext.getTraceId()));
    }
}

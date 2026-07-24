package com.omni.scaffolding.modules.open.controller;

import com.omni.scaffolding.common.api.ApiResponse;
import com.omni.scaffolding.common.trace.TraceContext;
import com.omni.scaffolding.security.open.OpenApiClientPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 开放演示接口：由 {@code OpenApiAuthFilter} 按 {@code X-Api-Key} 鉴权，不走 JWT。
 *
 * <p>种子路径 {@code GET /api/open/demo/ping}，用于验证 Key / 白名单 / 限额整条链路。
 */
@Tag(name = "Open API Demo")
@RestController
@RequestMapping("/api/open/demo")
public class OpenApiDemoController {

    /**
     * 演示 Ping：返回客户端信息与 traceId。
     *
     * @return 简单业务载荷
     */
    @Operation(summary = "开放演示 Ping")
    @GetMapping("/ping")
    public ApiResponse<Map<String, Object>> ping() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "pong");
        body.put("traceId", TraceContext.getTraceId());
        if (auth != null && auth.getPrincipal() instanceof OpenApiClientPrincipal client) {
            body.put("clientId", client.getClientId());
            body.put("clientName", client.getName());
            body.put("accessKey", client.getAccessKey());
        }
        return ApiResponse.ok(body);
    }
}

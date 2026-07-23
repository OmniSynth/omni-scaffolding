package com.omni.scaffolding.modules.demo.controller;

import com.omni.scaffolding.common.api.ApiResponse;
import com.omni.scaffolding.common.security.IpWhitelist;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * IP 白名单注解演示。
 *
 * <p>仅白名单内 IP 可访问；放行后计入今日访问量。
 */
@Tag(name = "Demo IP Whitelist")
@RestController
@RequestMapping("/api/demo/secure")
public class DemoIpWhitelistController {

    /**
     * 白名单保护的 ping 接口；仅放行 IP 可访问，放行后计入今日访问量。
     *
     * @return 固定成功消息
     */
    @IpWhitelist
    @Operation(summary = "白名单保护的 ping（全局白名单）")
    @GetMapping("/ping")
    @PreAuthorize("isAuthenticated()")
    @RateLimiter(name = "api")
    public ApiResponse<Map<String, String>> ping() {
        return ApiResponse.ok(Map.of("message", "ip whitelist ok"));
    }
}

package com.omni.scaffolding.modules.ops.controller;

import com.omni.scaffolding.common.api.ApiResponse;
import com.omni.scaffolding.modules.ops.dto.ServerRuntimeView;
import com.omni.scaffolding.modules.ops.service.ServerInfoService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统运行环境详情。
 *
 * <p>权限：{@code ops:server:query}。
 */
@Tag(name = "Ops Server")
@RestController
@RequestMapping("/api/ops/server")
@RequiredArgsConstructor
public class ServerInfoController {

    private final ServerInfoService serverInfoService;

    /**
     * 获取当前运行环境全量信息。
     *
     * @return 应用、JVM、内存、OS、磁盘、数据源与 Redis 快照
     */
    @Operation(summary = "获取当前运行环境全量信息")
    @GetMapping("/runtime")
    @PreAuthorize("hasAuthority('ops:server:query')")
    @RateLimiter(name = "api")
    public ApiResponse<ServerRuntimeView> runtime() {
        return ApiResponse.ok(serverInfoService.collect());
    }
}

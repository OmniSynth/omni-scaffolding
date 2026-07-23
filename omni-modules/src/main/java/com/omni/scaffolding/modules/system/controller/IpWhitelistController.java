package com.omni.scaffolding.modules.system.controller;

import com.omni.scaffolding.common.api.ApiResponse;
import com.omni.scaffolding.common.api.PageResult;
import com.omni.scaffolding.common.audit.OperLog;
import com.omni.scaffolding.modules.system.dto.ip.IpVisitTodayView;
import com.omni.scaffolding.modules.system.dto.ip.IpWhitelistSaveRequest;
import com.omni.scaffolding.modules.system.dto.ip.IpWhitelistStatusRequest;
import com.omni.scaffolding.modules.system.dto.ip.IpWhitelistView;
import com.omni.scaffolding.modules.system.service.IpWhitelistService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * IP 白名单管理接口。
 *
 * <p>权限码：{@code system:ipWhitelist:query/add/edit/remove/refresh}。
 * 运行时校验由 {@link com.omni.scaffolding.common.security.IpWhitelist} 注解 + 切面完成。
 */
@Tag(name = "IP Whitelist")
@RestController
@RequestMapping("/api/system/ip-whitelist")
@RequiredArgsConstructor
public class IpWhitelistController {

    private final IpWhitelistService ipWhitelistService;

    /**
     * 分页列表。
     *
     * @param keyword 可选，匹配 IP / 备注
     * @param status  可选，启停状态
     * @param page    页码
     * @param size    每页条数
     * @return 分页结果
     */
    @Operation(summary = "IP 白名单分页列表")
    @GetMapping
    @PreAuthorize("hasAuthority('system:ipWhitelist:query')")
    @RateLimiter(name = "api")
    public ApiResponse<PageResult<IpWhitelistView>> list(@RequestParam(required = false) String keyword,
                                                         @RequestParam(required = false) Boolean status,
                                                         @RequestParam(required = false) Long page,
                                                         @RequestParam(required = false) Long size) {
        return ApiResponse.ok(ipWhitelistService.list(keyword, status, page, size));
    }

    /**
     * 今日访问统计（Redis 计数）。
     *
     * @return 合计与按 IP 明细
     */
    @Operation(summary = "今日白名单接口访问统计")
    @GetMapping("/visits/today")
    @PreAuthorize("hasAuthority('system:ipWhitelist:query')")
    @RateLimiter(name = "api")
    public ApiResponse<IpVisitTodayView> todayVisits() {
        return ApiResponse.ok(ipWhitelistService.todayStats());
    }

    /**
     * 清空启用 IP 列表缓存，下次校验重新加载。
     *
     * @return 空成功响应
     */
    @Operation(summary = "刷新 IP 白名单缓存")
    @PostMapping("/cache/refresh")
    @PreAuthorize("hasAuthority('system:ipWhitelist:refresh')")
    @RateLimiter(name = "api")
    @OperLog(module = "IP白名单", action = "刷新缓存")
    public ApiResponse<Void> refreshCache() {
        ipWhitelistService.refreshCache();
        return ApiResponse.ok();
    }

    /**
     * 详情。
     *
     * @param id 主键
     * @return 读模型
     */
    @Operation(summary = "IP 白名单详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:ipWhitelist:query')")
    @RateLimiter(name = "api")
    public ApiResponse<IpWhitelistView> detail(@PathVariable Long id) {
        return ApiResponse.ok(ipWhitelistService.detail(id));
    }

    /**
     * 新增。
     *
     * @param request 保存请求
     * @return 新建读模型
     */
    @Operation(summary = "新增 IP 白名单")
    @PostMapping
    @PreAuthorize("hasAuthority('system:ipWhitelist:add')")
    @RateLimiter(name = "api")
    @OperLog(module = "IP白名单", action = "新增")
    public ApiResponse<IpWhitelistView> create(@Valid @RequestBody IpWhitelistSaveRequest request) {
        return ApiResponse.ok(ipWhitelistService.create(request));
    }

    /**
     * 修改。
     *
     * @param id      主键
     * @param request 保存请求
     * @return 更新后读模型
     */
    @Operation(summary = "修改 IP 白名单")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:ipWhitelist:edit')")
    @RateLimiter(name = "api")
    @OperLog(module = "IP白名单", action = "修改")
    public ApiResponse<IpWhitelistView> update(@PathVariable Long id, @Valid @RequestBody IpWhitelistSaveRequest request) {
        return ApiResponse.ok(ipWhitelistService.update(id, request));
    }

    /**
     * 启停。
     *
     * @param id      主键
     * @param request 状态请求
     * @return 更新后读模型
     */
    @Operation(summary = "启用/停用 IP 白名单")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('system:ipWhitelist:edit')")
    @RateLimiter(name = "api")
    @OperLog(module = "IP白名单", action = "变更状态")
    public ApiResponse<IpWhitelistView> changeStatus(@PathVariable Long id,
                                                     @Valid @RequestBody IpWhitelistStatusRequest request) {
        return ApiResponse.ok(ipWhitelistService.changeStatus(id, Boolean.TRUE.equals(request.getStatus())));
    }

    /**
     * 逻辑删除。
     *
     * @param id 主键
     * @return 空成功响应
     */
    @Operation(summary = "删除 IP 白名单")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:ipWhitelist:remove')")
    @RateLimiter(name = "api")
    @OperLog(module = "IP白名单", action = "删除")
    public ApiResponse<Void> remove(@PathVariable Long id) {
        ipWhitelistService.remove(id);
        return ApiResponse.ok();
    }
}


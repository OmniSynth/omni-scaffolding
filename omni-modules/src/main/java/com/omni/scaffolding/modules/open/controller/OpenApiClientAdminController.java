package com.omni.scaffolding.modules.open.controller;

import com.omni.scaffolding.common.api.ApiResponse;
import com.omni.scaffolding.common.api.PageResult;
import com.omni.scaffolding.common.audit.OperLog;
import com.omni.scaffolding.modules.open.dto.client.OpenClientCredentialsView;
import com.omni.scaffolding.modules.open.dto.client.OpenClientSaveRequest;
import com.omni.scaffolding.modules.open.dto.client.OpenClientView;
import com.omni.scaffolding.modules.open.service.OpenApiClientService;
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
 * 开放 API 客户端管理接口（JWT）。
 *
 * <p>权限码：{@code open:client:query/add/edit/remove/resetKey}。
 * 创建与重置密钥会一次性返回明文 {@code apiKey}/{@code accessSecret}。
 */
@Tag(name = "Open API Clients")
@RestController
@RequestMapping("/api/open/admin/clients")
@RequiredArgsConstructor
public class OpenApiClientAdminController {

    private final OpenApiClientService clientService;

    /**
     * 客户端分页列表（含当日已用次数）。
     *
     * @param keyword 可选，匹配名称 / AccessKey
     * @param status  可选，启停过滤
     * @param page    页码
     * @param size    每页条数
     * @return 分页结果
     */
    @Operation(summary = "客户端分页列表")
    @GetMapping
    @PreAuthorize("hasAuthority('open:client:query')")
    @RateLimiter(name = "api")
    public ApiResponse<PageResult<OpenClientView>> list(@RequestParam(required = false) String keyword,
                                                        @RequestParam(required = false) Boolean status,
                                                        @RequestParam(required = false) Long page,
                                                        @RequestParam(required = false) Long size) {
        return ApiResponse.ok(clientService.list(keyword, status, page, size));
    }

    /**
     * 客户端详情（含 IP 列表与绑定接口）。
     *
     * @param id 客户端主键
     * @return 读模型
     */
    @Operation(summary = "客户端详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('open:client:query')")
    @RateLimiter(name = "api")
    public ApiResponse<OpenClientView> detail(@PathVariable Long id) {
        return ApiResponse.ok(clientService.detail(id));
    }

    /**
     * 新增客户端并签发密钥；响应含一次性明文凭证。
     *
     * @param request 保存请求
     * @return 明文凭证
     */
    @Operation(summary = "新增客户端（返回一次性明文密钥）")
    @PostMapping
    @PreAuthorize("hasAuthority('open:client:add')")
    @RateLimiter(name = "api")
    @OperLog(module = "开放客户端", action = "新增")
    public ApiResponse<OpenClientCredentialsView> create(@Valid @RequestBody OpenClientSaveRequest request) {
        return ApiResponse.ok(clientService.create(request));
    }

    /**
     * 修改客户端元数据、IP 白名单与接口绑定（不轮换密钥）。
     *
     * @param id      客户端主键
     * @param request 保存请求
     * @return 更新后读模型
     */
    @Operation(summary = "修改客户端")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('open:client:edit')")
    @RateLimiter(name = "api")
    @OperLog(module = "开放客户端", action = "修改")
    public ApiResponse<OpenClientView> update(@PathVariable Long id,
                                              @Valid @RequestBody OpenClientSaveRequest request) {
        return ApiResponse.ok(clientService.update(id, request));
    }

    /**
     * 重置 API Key / AccessSecret；旧 Key 立即失效。
     *
     * @param id 客户端主键
     * @return 新明文凭证
     */
    @Operation(summary = "重置 API Key / Secret（返回一次性明文）")
    @PostMapping("/{id}/reset-keys")
    @PreAuthorize("hasAuthority('open:client:resetKey')")
    @RateLimiter(name = "api")
    @OperLog(module = "开放客户端", action = "重置密钥")
    public ApiResponse<OpenClientCredentialsView> resetKeys(@PathVariable Long id) {
        return ApiResponse.ok(clientService.resetKeys(id));
    }

    /**
     * 逻辑删除客户端，并清理 IP / 接口关联。
     *
     * @param id 客户端主键
     * @return 空成功响应
     */
    @Operation(summary = "删除客户端")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('open:client:remove')")
    @RateLimiter(name = "api")
    @OperLog(module = "开放客户端", action = "删除")
    public ApiResponse<Void> remove(@PathVariable Long id) {
        clientService.remove(id);
        return ApiResponse.ok();
    }
}

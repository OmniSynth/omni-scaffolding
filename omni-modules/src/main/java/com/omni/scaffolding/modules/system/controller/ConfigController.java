package com.omni.scaffolding.modules.system.controller;

import com.omni.scaffolding.common.api.ApiResponse;
import com.omni.scaffolding.common.api.PageResult;
import com.omni.scaffolding.common.audit.OperLog;
import com.omni.scaffolding.common.excel.ExcelExportHelper;
import com.omni.scaffolding.modules.system.dto.config.ConfigSaveRequest;
import com.omni.scaffolding.modules.system.dto.config.ConfigStatusRequest;
import com.omni.scaffolding.modules.system.dto.config.ConfigView;
import com.omni.scaffolding.modules.system.dto.excel.ConfigExportRow;
import com.omni.scaffolding.modules.system.service.ConfigService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
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
 * 系统参数接口。
 *
 * <p>权限码：{@code system:config:query/add/edit/remove/export/refresh}。
 * 按键取值 {@code /value/{configKey}} 仅需登录。
 */
@Tag(name = "Configs")
@RestController
@RequestMapping("/api/system/configs")
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigService configService;

    /**
     * 按键读取启用中的参数值。
     *
     * @param configKey 参数键
     * @return 参数值，不存在或停用则为 {@code null}
     */
    @Operation(summary = "按键读取启用中的参数值")
    @GetMapping("/value/{configKey:.+}")
    @PreAuthorize("isAuthenticated()")
    @RateLimiter(name = "api")
    public ApiResponse<String> getValue(@PathVariable("configKey") String configKey) {
        return ApiResponse.ok(configService.getValue(configKey));
    }

    /**
     * 清空系统参数缓存（{@code sysConfig}），下次 {@code getValue} 重新加载。
     *
     * @return 空成功响应
     */
    @Operation(summary = "刷新系统参数缓存")
    @PostMapping("/cache/refresh")
    @PreAuthorize("hasAuthority('system:config:refresh')")
    @RateLimiter(name = "api")
    @OperLog(module = "系统参数", action = "刷新缓存")
    public ApiResponse<Void> refreshCache() {
        configService.refreshCache();
        return ApiResponse.ok();
    }

    /**
     * 分页列表。
     *
     * @param keyword 可选，匹配参数键 / 名称 / 值
     * @param page    页码
     * @param size    每页条数
     * @return 分页结果
     */
    @Operation(summary = "系统参数分页列表")
    @GetMapping
    @PreAuthorize("hasAuthority('system:config:query')")
    @RateLimiter(name = "api")
    public ApiResponse<PageResult<ConfigView>> list(@RequestParam(required = false) String keyword,
                                                    @RequestParam(required = false) Long page,
                                                    @RequestParam(required = false) Long size) {
        return ApiResponse.ok(configService.list(keyword, page, size));
    }

    /**
     * 导出 Excel（过滤条件与列表一致）。
     *
     * @param keyword  可选，匹配参数键 / 名称 / 值
     * @param response HTTP 响应，直接写入文件流
     */
    @Operation(summary = "导出系统参数 Excel")
    @GetMapping("/export")
    @PreAuthorize("hasAuthority('system:config:export')")
    @RateLimiter(name = "api")
    @OperLog(module = "系统参数", action = "导出")
    public void export(@RequestParam(required = false) String keyword, HttpServletResponse response) {
        ExcelExportHelper.write(response, "系统参数.xlsx", ConfigExportRow.class, configService.export(keyword));
    }

    /**
     * 参数详情。
     *
     * @param id 参数主键
     * @return 读模型
     */
    @Operation(summary = "系统参数详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:config:query')")
    @RateLimiter(name = "api")
    public ApiResponse<ConfigView> detail(@PathVariable Long id) {
        return ApiResponse.ok(configService.detail(id));
    }

    /**
     * 新增参数。
     *
     * @param request 创建请求
     * @return 新建参数读模型
     */
    @Operation(summary = "新增系统参数")
    @PostMapping
    @PreAuthorize("hasAuthority('system:config:add')")
    @RateLimiter(name = "api")
    @OperLog(module = "系统参数", action = "新增")
    public ApiResponse<ConfigView> create(@Valid @RequestBody ConfigSaveRequest request) {
        return ApiResponse.ok(configService.create(request));
    }

    /**
     * 修改参数。
     *
     * @param id      参数主键
     * @param request 修改请求
     * @return 更新后的读模型
     */
    @Operation(summary = "修改系统参数")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:config:edit')")
    @RateLimiter(name = "api")
    @OperLog(module = "系统参数", action = "修改")
    public ApiResponse<ConfigView> update(@PathVariable Long id, @Valid @RequestBody ConfigSaveRequest request) {
        return ApiResponse.ok(configService.update(id, request));
    }

    /**
     * 启用 / 停用参数。
     *
     * @param id      参数主键
     * @param request 启停请求
     * @return 更新后的读模型
     */
    @Operation(summary = "启用/停用系统参数")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('system:config:edit')")
    @RateLimiter(name = "api")
    @OperLog(module = "系统参数", action = "变更状态")
    public ApiResponse<ConfigView> changeStatus(@PathVariable Long id, @Valid @RequestBody ConfigStatusRequest request) {
        return ApiResponse.ok(configService.changeStatus(id, Boolean.TRUE.equals(request.getStatus())));
    }

    /**
     * 逻辑删除参数（内置参数不可删）。
     *
     * @param id 参数主键
     * @return 空成功响应
     */
    @Operation(summary = "删除系统参数")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:config:remove')")
    @RateLimiter(name = "api")
    @OperLog(module = "系统参数", action = "删除")
    public ApiResponse<Void> remove(@PathVariable Long id) {
        configService.remove(id);
        return ApiResponse.ok();
    }
}

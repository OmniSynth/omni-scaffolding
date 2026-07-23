package com.omni.scaffolding.modules.system.controller;

import com.omni.scaffolding.common.api.ApiResponse;
import com.omni.scaffolding.common.api.PageResult;
import com.omni.scaffolding.common.audit.OperLog;
import com.omni.scaffolding.common.excel.ExcelExportHelper;
import com.omni.scaffolding.modules.system.dto.PostSaveRequest;
import com.omni.scaffolding.modules.system.dto.PostView;
import com.omni.scaffolding.modules.system.dto.excel.PostExportRow;
import com.omni.scaffolding.modules.system.service.PostService;
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
 * 岗位管理接口。
 *
 * <p>权限码：{@code system:post:query/add/edit/remove/export}。
 * 列表接口额外允许用户新增/编辑场景下拉选型。
 */
@Tag(name = "Posts")
@RestController
@RequestMapping("/api/system/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * 岗位分页列表。
     *
     * @param keyword 可选，匹配编码 / 名称
     * @param page    页码
     * @param size    每页条数
     * @return 分页结果
     */
    @Operation(summary = "岗位分页列表")
    @GetMapping
    @PreAuthorize("hasAnyAuthority('system:post:query','system:user:add','system:user:edit')")
    @RateLimiter(name = "api")
    public ApiResponse<PageResult<PostView>> list(@RequestParam(required = false) String keyword,
                                                  @RequestParam(required = false) Long page,
                                                  @RequestParam(required = false) Long size) {
        return ApiResponse.ok(postService.list(keyword, page, size));
    }

    /**
     * 导出岗位 Excel。
     *
     * @param keyword  可选，匹配编码 / 名称
     * @param response HTTP 响应，直接写入文件流
     */
    @Operation(summary = "导出岗位 Excel")
    @GetMapping("/export")
    @PreAuthorize("hasAuthority('system:post:export')")
    @RateLimiter(name = "api")
    @OperLog(module = "岗位管理", action = "导出")
    public void export(@RequestParam(required = false) String keyword, HttpServletResponse response) {
        ExcelExportHelper.write(response, "岗位数据.xlsx", PostExportRow.class, postService.exportPosts(keyword));
    }

    /**
     * 岗位详情。
     *
     * @param id 岗位主键
     * @return 读模型
     */
    @Operation(summary = "岗位详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:post:query')")
    @RateLimiter(name = "api")
    public ApiResponse<PostView> detail(@PathVariable Long id) {
        return ApiResponse.ok(postService.detail(id));
    }

    /**
     * 创建岗位。
     *
     * @param request 保存请求
     * @return 新建读模型
     */
    @Operation(summary = "创建岗位")
    @PostMapping
    @PreAuthorize("hasAuthority('system:post:add')")
    @RateLimiter(name = "api")
    @OperLog(module = "岗位管理", action = "新增")
    public ApiResponse<PostView> create(@Valid @RequestBody PostSaveRequest request) {
        return ApiResponse.ok(postService.create(request));
    }

    /**
     * 修改岗位。
     *
     * @param id      岗位主键
     * @param request 保存请求
     * @return 更新后读模型
     */
    @Operation(summary = "修改岗位")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:post:edit')")
    @RateLimiter(name = "api")
    @OperLog(module = "岗位管理", action = "修改")
    public ApiResponse<PostView> update(@PathVariable Long id, @Valid @RequestBody PostSaveRequest request) {
        return ApiResponse.ok(postService.update(id, request));
    }

    /**
     * 删除岗位。
     *
     * @param id 岗位主键
     * @return 空成功响应
     */
    @Operation(summary = "删除岗位")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:post:remove')")
    @RateLimiter(name = "api")
    @OperLog(module = "岗位管理", action = "删除")
    public ApiResponse<Void> remove(@PathVariable Long id) {
        postService.remove(id);
        return ApiResponse.ok();
    }
}

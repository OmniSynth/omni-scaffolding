package com.omni.scaffolding.modules.system.controller;

import com.omni.scaffolding.common.api.ApiResponse;
import com.omni.scaffolding.common.api.PageResult;
import com.omni.scaffolding.common.audit.OperLog;
import com.omni.scaffolding.common.desensitize.WithoutDesensitize;
import com.omni.scaffolding.common.excel.ExcelExportHelper;
import com.omni.scaffolding.modules.system.dto.CreateUserRequest;
import com.omni.scaffolding.modules.system.dto.ResetPasswordRequest;
import com.omni.scaffolding.modules.system.dto.UpdateUserRequest;
import com.omni.scaffolding.modules.system.dto.UserDetailView;
import com.omni.scaffolding.modules.system.dto.UserEnabledRequest;
import com.omni.scaffolding.modules.system.dto.excel.UserExportRow;
import com.omni.scaffolding.modules.system.service.UserService;
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
 * 用户管理接口。
 *
 * <p>权限码精确到按钮：{@code system:user:query/add/edit/remove/resetPwd/export}。
 * 创建走 JPA，详情与搜索走 MyBatis，并按角色数据范围过滤；头像存 {@code avatarFileId}，上传走统一文件 API。
 */
@Tag(name = "Users")
@RestController
@RequestMapping("/api/system/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 分页搜索用户（数据范围过滤）。
     *
     * @param keyword 可选，匹配用户名 / 昵称 / 姓名 / 手机 / 邮箱
     * @param page    页码
     * @param size    每页条数
     * @return 分页结果
     */
    @Operation(summary = "分页搜索用户（数据范围过滤）")
    @GetMapping
    @PreAuthorize("hasAuthority('system:user:query')")
    @RateLimiter(name = "api")
    public ApiResponse<PageResult<UserDetailView>> search(@RequestParam(required = false) String keyword,
                                                          @RequestParam(required = false) Long page,
                                                          @RequestParam(required = false) Long size) {
        return ApiResponse.ok(userService.searchUsers(keyword, page, size));
    }

    /**
     * 导出用户 Excel。
     *
     * @param keyword  可选，匹配关键字
     * @param response HTTP 响应，直接写入文件流
     */
    @Operation(summary = "导出用户 Excel")
    @GetMapping("/export")
    @PreAuthorize("hasAuthority('system:user:export')")
    @RateLimiter(name = "api")
    @OperLog(module = "用户管理", action = "导出")
    public void export(@RequestParam(required = false) String keyword, HttpServletResponse response) {
        ExcelExportHelper.write(response, "用户数据.xlsx", UserExportRow.class, userService.exportUsers(keyword));
    }

    /**
     * 用户详情（含角色、权限、岗位）。
     *
     * @param id 用户主键
     * @return 详情读模型（明文，供编辑回填）
     */
    @Operation(summary = "用户详情含角色权限（MyBatis 联查，明文回填编辑）")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:query')")
    @RateLimiter(name = "api")
    @WithoutDesensitize
    public ApiResponse<UserDetailView> detail(@PathVariable Long id) {
        return ApiResponse.ok(userService.getUserDetail(id));
    }

    /**
     * 创建用户。
     *
     * @param request 创建请求
     * @return 新建用户详情
     */
    @Operation(summary = "创建用户（JPA 写）")
    @PostMapping
    @PreAuthorize("hasAuthority('system:user:add')")
    @RateLimiter(name = "api")
    @WithoutDesensitize
    @OperLog(module = "用户管理", action = "新增")
    public ApiResponse<UserDetailView> create(@Valid @RequestBody CreateUserRequest request) {
        return ApiResponse.ok(userService.createUser(request));
    }

    /**
     * 修改用户。
     *
     * @param id      用户主键
     * @param request 修改请求
     * @return 更新后详情
     */
    @Operation(summary = "修改用户")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:edit')")
    @RateLimiter(name = "api")
    @WithoutDesensitize
    @OperLog(module = "用户管理", action = "修改")
    public ApiResponse<UserDetailView> update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return ApiResponse.ok(userService.updateUser(id, request));
    }

    /**
     * 启用 / 停用用户。
     *
     * @param id      用户主键
     * @param request 启停请求
     * @return 更新后详情
     */
    @Operation(summary = "启用/停用用户")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('system:user:edit')")
    @RateLimiter(name = "api")
    @WithoutDesensitize
    @OperLog(module = "用户管理", action = "变更状态")
    public ApiResponse<UserDetailView> changeEnabled(@PathVariable Long id,
                                                     @Valid @RequestBody UserEnabledRequest request) {
        return ApiResponse.ok(userService.changeEnabled(id, Boolean.TRUE.equals(request.getEnabled())));
    }

    /**
     * 逻辑删除用户。
     *
     * @param id 用户主键
     * @return 空成功响应
     */
    @Operation(summary = "删除用户（逻辑删除）")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:remove')")
    @RateLimiter(name = "api")
    @OperLog(module = "用户管理", action = "删除")
    public ApiResponse<Void> remove(@PathVariable Long id) {
        userService.removeUser(id);
        return ApiResponse.ok();
    }

    /**
     * 重置密码。
     *
     * @param id      用户主键
     * @param request 新密码请求
     * @return 空成功响应
     */
    @Operation(summary = "重置密码")
    @PutMapping("/{id}/password")
    @PreAuthorize("hasAuthority('system:user:resetPwd')")
    @RateLimiter(name = "api")
    @OperLog(module = "用户管理", action = "重置密码")
    public ApiResponse<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(id, request);
        return ApiResponse.ok();
    }
}

package com.omni.scaffolding.modules.system.controller;

import com.omni.scaffolding.common.api.ApiResponse;
import com.omni.scaffolding.common.api.PageResult;
import com.omni.scaffolding.common.audit.OperLog;
import com.omni.scaffolding.modules.system.dto.NoticeSaveRequest;
import com.omni.scaffolding.modules.system.dto.NoticeStatusRequest;
import com.omni.scaffolding.modules.system.dto.NoticeView;
import com.omni.scaffolding.modules.system.service.NoticeService;
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

import java.util.List;
import java.util.Map;

/**
 * 通知公告接口。
 *
 * <p>管理权限：{@code system:notice:query/add/edit/remove}；
 * 用户端 unread/inbox/read 仅需登录。
 */
@Tag(name = "Notices")
@RestController
@RequestMapping("/api/system/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    /**
     * 当前用户未读公告列表。
     *
     * @return 读模型列表
     */
    @Operation(summary = "当前用户未读公告")
    @GetMapping("/unread")
    @PreAuthorize("isAuthenticated()")
    @RateLimiter(name = "api")
    public ApiResponse<List<NoticeView>> unread() {
        return ApiResponse.ok(noticeService.listUnread());
    }

    /**
     * 当前用户未读公告数量。
     *
     * @return {@code count} 字段
     */
    @Operation(summary = "当前用户未读数量")
    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    @RateLimiter(name = "api")
    public ApiResponse<Map<String, Long>> unreadCount() {
        return ApiResponse.ok(Map.of("count", noticeService.unreadCount()));
    }

    /**
     * 当前用户公告收件箱（含已读标记）。
     *
     * @return 读模型列表
     */
    @Operation(summary = "当前用户公告收件箱")
    @GetMapping("/inbox")
    @PreAuthorize("isAuthenticated()")
    @RateLimiter(name = "api")
    public ApiResponse<List<NoticeView>> inbox() {
        return ApiResponse.ok(noticeService.inbox());
    }

    /**
     * 标记全部未读公告为已读。
     *
     * @return 空成功响应
     */
    @Operation(summary = "标记全部已读")
    @PutMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    @RateLimiter(name = "api")
    public ApiResponse<Void> markAllRead() {
        noticeService.markAllRead();
        return ApiResponse.ok();
    }

    /**
     * 标记单条公告为已读。
     *
     * @param id 公告主键
     * @return 空成功响应
     */
    @Operation(summary = "标记单条已读")
    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    @RateLimiter(name = "api")
    public ApiResponse<Void> markRead(@PathVariable Long id) {
        noticeService.markRead(id);
        return ApiResponse.ok();
    }

    /**
     * 分页列表（管理端）。
     *
     * @param keyword 可选，匹配标题 / 内容
     * @param status  可选，启停状态
     * @param type    可选，公告类型
     * @param page    页码
     * @param size    每页条数
     * @return 分页结果
     */
    @Operation(summary = "公告分页列表")
    @GetMapping
    @PreAuthorize("hasAuthority('system:notice:query')")
    @RateLimiter(name = "api")
    public ApiResponse<PageResult<NoticeView>> list(@RequestParam(required = false) String keyword,
                                                    @RequestParam(required = false) Boolean status,
                                                    @RequestParam(required = false) String type,
                                                    @RequestParam(required = false) Long page,
                                                    @RequestParam(required = false) Long size) {
        return ApiResponse.ok(noticeService.list(keyword, status, type, page, size));
    }

    /**
     * 公告详情。
     *
     * @param id 公告主键
     * @return 读模型
     */
    @Operation(summary = "公告详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:notice:query')")
    @RateLimiter(name = "api")
    public ApiResponse<NoticeView> detail(@PathVariable Long id) {
        return ApiResponse.ok(noticeService.detail(id));
    }

    /**
     * 新增公告。
     *
     * @param request 创建请求
     * @return 新建公告读模型
     */
    @Operation(summary = "新增公告")
    @PostMapping
    @PreAuthorize("hasAuthority('system:notice:add')")
    @RateLimiter(name = "api")
    @OperLog(module = "通知公告", action = "新增")
    public ApiResponse<NoticeView> create(@Valid @RequestBody NoticeSaveRequest request) {
        return ApiResponse.ok(noticeService.create(request));
    }

    /**
     * 修改公告。
     *
     * @param id      公告主键
     * @param request 修改请求
     * @return 更新后的读模型
     */
    @Operation(summary = "修改公告")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:notice:edit')")
    @RateLimiter(name = "api")
    @OperLog(module = "通知公告", action = "修改")
    public ApiResponse<NoticeView> update(@PathVariable Long id, @Valid @RequestBody NoticeSaveRequest request) {
        return ApiResponse.ok(noticeService.update(id, request));
    }

    /**
     * 启用 / 停用公告。
     *
     * @param id      公告主键
     * @param request 启停请求
     * @return 更新后的读模型
     */
    @Operation(summary = "启用/停用公告")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('system:notice:edit')")
    @RateLimiter(name = "api")
    @OperLog(module = "通知公告", action = "变更状态")
    public ApiResponse<NoticeView> changeStatus(@PathVariable Long id, @Valid @RequestBody NoticeStatusRequest request) {
        return ApiResponse.ok(noticeService.changeStatus(id, Boolean.TRUE.equals(request.getStatus())));
    }

    /**
     * 逻辑删除公告。
     *
     * @param id 公告主键
     * @return 空成功响应
     */
    @Operation(summary = "删除公告")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:notice:remove')")
    @RateLimiter(name = "api")
    @OperLog(module = "通知公告", action = "删除")
    public ApiResponse<Void> remove(@PathVariable Long id) {
        noticeService.remove(id);
        return ApiResponse.ok();
    }
}


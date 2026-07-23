package com.omni.scaffolding.modules.system.controller;

import com.omni.scaffolding.common.api.ApiResponse;
import com.omni.scaffolding.common.api.PageResult;
import com.omni.scaffolding.common.audit.OperLog;
import com.omni.scaffolding.modules.system.dto.job.CronValidateRequest;
import com.omni.scaffolding.modules.system.dto.job.CronValidateView;
import com.omni.scaffolding.modules.system.dto.job.JobLogView;
import com.omni.scaffolding.modules.system.dto.job.JobSaveRequest;
import com.omni.scaffolding.modules.system.dto.job.JobStatusRequest;
import com.omni.scaffolding.modules.system.dto.job.JobView;
import com.omni.scaffolding.modules.system.service.JobService;
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
 * 定时任务管理接口。
 *
 * <p>权限：{@code system:job:query/add/edit/remove/run}。
 */
@Tag(name = "Jobs")
@RestController
@RequestMapping("/api/system/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    /**
     * 分页列表。
     *
     * @param keyword 可选，匹配任务名称 / 调用目标
     * @param status  可选，启停状态
     * @param page    页码
     * @param size    每页条数
     * @return 分页结果
     */
    @Operation(summary = "定时任务分页列表")
    @GetMapping
    @PreAuthorize("hasAuthority('system:job:query')")
    @RateLimiter(name = "api")
    public ApiResponse<PageResult<JobView>> list(@RequestParam(required = false) String keyword,
                                                 @RequestParam(required = false) Boolean status,
                                                 @RequestParam(required = false) Long page,
                                                 @RequestParam(required = false) Long size) {
        return ApiResponse.ok(jobService.list(keyword, status, page, size));
    }

    /**
     * 校验 Cron 表达式并预览下次触发时间。
     *
     * @param request Cron 校验请求
     * @return 校验结果
     */
    @Operation(summary = "校验 Cron 并预览下次触发")
    @PostMapping("/cron/validate")
    @PreAuthorize("hasAuthority('system:job:query')")
    @RateLimiter(name = "api")
    public ApiResponse<CronValidateView> validateCron(@Valid @RequestBody CronValidateRequest request) {
        return ApiResponse.ok(jobService.validateCron(request.getCronExpression()));
    }

    /**
     * 任务详情。
     *
     * @param id 任务主键
     * @return 读模型
     */
    @Operation(summary = "定时任务详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:job:query')")
    @RateLimiter(name = "api")
    public ApiResponse<JobView> detail(@PathVariable Long id) {
        return ApiResponse.ok(jobService.detail(id));
    }

    /**
     * 新增任务。
     *
     * @param request 创建请求
     * @return 新建任务读模型
     */
    @Operation(summary = "新增定时任务")
    @PostMapping
    @PreAuthorize("hasAuthority('system:job:add')")
    @RateLimiter(name = "api")
    @OperLog(module = "定时任务", action = "新增")
    public ApiResponse<JobView> create(@Valid @RequestBody JobSaveRequest request) {
        return ApiResponse.ok(jobService.create(request));
    }

    /**
     * 修改任务。
     *
     * @param id      任务主键
     * @param request 修改请求
     * @return 更新后的读模型
     */
    @Operation(summary = "修改定时任务")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:job:edit')")
    @RateLimiter(name = "api")
    @OperLog(module = "定时任务", action = "修改")
    public ApiResponse<JobView> update(@PathVariable Long id, @Valid @RequestBody JobSaveRequest request) {
        return ApiResponse.ok(jobService.update(id, request));
    }

    /**
     * 启用 / 停用任务。
     *
     * @param id      任务主键
     * @param request 启停请求
     * @return 更新后的读模型
     */
    @Operation(summary = "启用/停用定时任务")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('system:job:edit')")
    @RateLimiter(name = "api")
    @OperLog(module = "定时任务", action = "变更状态")
    public ApiResponse<JobView> changeStatus(@PathVariable Long id, @Valid @RequestBody JobStatusRequest request) {
        return ApiResponse.ok(jobService.changeStatus(id, Boolean.TRUE.equals(request.getStatus())));
    }

    /**
     * 逻辑删除任务。
     *
     * @param id 任务主键
     * @return 空成功响应
     */
    @Operation(summary = "删除定时任务")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:job:remove')")
    @RateLimiter(name = "api")
    @OperLog(module = "定时任务", action = "删除")
    public ApiResponse<Void> remove(@PathVariable Long id) {
        jobService.remove(id);
        return ApiResponse.ok();
    }

    /**
     * 立即触发一次执行。
     *
     * @param id 任务主键
     * @return 空成功响应
     */
    @Operation(summary = "立即执行一次")
    @PostMapping("/{id}/run")
    @PreAuthorize("hasAuthority('system:job:run')")
    @RateLimiter(name = "api")
    @OperLog(module = "定时任务", action = "立即执行")
    public ApiResponse<Void> runOnce(@PathVariable Long id) {
        jobService.runOnce(id);
        return ApiResponse.ok();
    }

    /**
     * 分页查询任务执行日志。
     *
     * @param id   任务主键
     * @param page 页码
     * @param size 每页条数
     * @return 分页结果
     */
    @Operation(summary = "任务执行日志")
    @GetMapping("/{id}/logs")
    @PreAuthorize("hasAuthority('system:job:query')")
    @RateLimiter(name = "api")
    public ApiResponse<PageResult<JobLogView>> logs(@PathVariable Long id,
                                                    @RequestParam(required = false) Long page,
                                                    @RequestParam(required = false) Long size) {
        return ApiResponse.ok(jobService.listLogs(id, page, size));
    }

    /**
     * 清空指定任务的全部执行日志。
     *
     * @param id 任务主键
     * @return 空成功响应
     */
    @Operation(summary = "清空任务执行日志")
    @DeleteMapping("/{id}/logs")
    @PreAuthorize("hasAuthority('system:job:remove')")
    @RateLimiter(name = "api")
    @OperLog(module = "定时任务", action = "清空日志")
    public ApiResponse<Void> clearLogs(@PathVariable Long id) {
        jobService.clearLogs(id);
        return ApiResponse.ok();
    }
}

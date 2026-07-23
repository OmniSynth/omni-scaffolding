package com.omni.scaffolding.modules.ops.controller;

import com.omni.scaffolding.common.api.ApiResponse;
import com.omni.scaffolding.common.audit.OperLog;
import com.omni.scaffolding.modules.ops.dto.MysqlCreateIndexRequest;
import com.omni.scaffolding.modules.ops.dto.MysqlDropIndexRequest;
import com.omni.scaffolding.modules.ops.dto.MysqlOverviewView;
import com.omni.scaffolding.modules.ops.dto.MysqlProcessView;
import com.omni.scaffolding.modules.ops.dto.MysqlTableDetailView;
import com.omni.scaffolding.modules.ops.dto.MysqlTableView;
import com.omni.scaffolding.modules.ops.service.MysqlOpsService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * MySQL 运维接口。
 *
 * <p>权限：{@code ops:mysql:query/edit/remove}。不含任意 SQL / DROP TABLE。
 */
@Tag(name = "Ops MySQL")
@RestController
@RequestMapping("/api/ops/mysql")
@RequiredArgsConstructor
@Profile("!test")
public class MysqlOpsController {

    private final MysqlOpsService mysqlOpsService;

    /**
     * 当前库概览。
     *
     * @return 版本、表数量、空间占用等
     */
    @Operation(summary = "当前库概览")
    @GetMapping("/overview")
    @PreAuthorize("hasAuthority('ops:mysql:query')")
    @RateLimiter(name = "api")
    public ApiResponse<MysqlOverviewView> overview() {
        return ApiResponse.ok(mysqlOpsService.overview());
    }

    /**
     * 表列表。
     *
     * @param keyword 可选，表名模糊匹配
     * @return 表列表
     */
    @Operation(summary = "表列表")
    @GetMapping("/tables")
    @PreAuthorize("hasAuthority('ops:mysql:query')")
    @RateLimiter(name = "api")
    public ApiResponse<List<MysqlTableView>> tables(@RequestParam(required = false) String keyword) {
        return ApiResponse.ok(mysqlOpsService.listTables(keyword));
    }

    /**
     * 表详情（列 / 索引 / DDL）。
     *
     * @param table 表名
     * @return 表详情
     */
    @Operation(summary = "表详情（列/索引/DDL）")
    @GetMapping("/tables/{table}")
    @PreAuthorize("hasAuthority('ops:mysql:query')")
    @RateLimiter(name = "api")
    public ApiResponse<MysqlTableDetailView> tableDetail(@PathVariable String table) {
        return ApiResponse.ok(mysqlOpsService.tableDetail(table));
    }

    /**
     * 创建索引。
     *
     * @param request 创建索引请求
     * @return 更新后的表详情
     */
    @Operation(summary = "创建索引")
    @PostMapping("/indexes")
    @PreAuthorize("hasAuthority('ops:mysql:edit')")
    @RateLimiter(name = "api")
    @OperLog(module = "MySQL运维", action = "创建索引")
    public ApiResponse<MysqlTableDetailView> createIndex(@Valid @RequestBody MysqlCreateIndexRequest request) {
        return ApiResponse.ok(mysqlOpsService.createIndex(request));
    }

    /**
     * 删除索引。
     *
     * @param request 删除索引请求
     * @return 更新后的表详情
     */
    @Operation(summary = "删除索引")
    @DeleteMapping("/indexes")
    @PreAuthorize("hasAuthority('ops:mysql:remove')")
    @RateLimiter(name = "api")
    @OperLog(module = "MySQL运维", action = "删除索引")
    public ApiResponse<MysqlTableDetailView> dropIndex(@Valid @RequestBody MysqlDropIndexRequest request) {
        return ApiResponse.ok(mysqlOpsService.dropIndex(request));
    }

    /**
     * ANALYZE TABLE。
     *
     * @param table 表名
     * @return 执行结果消息
     */
    @Operation(summary = "ANALYZE TABLE")
    @PostMapping("/tables/{table}/analyze")
    @PreAuthorize("hasAuthority('ops:mysql:edit')")
    @RateLimiter(name = "api")
    @OperLog(module = "MySQL运维", action = "ANALYZE表")
    public ApiResponse<Map<String, Object>> analyze(@PathVariable String table) {
        return ApiResponse.ok(mysqlOpsService.analyzeTable(table));
    }

    /**
     * 进程列表。
     *
     * @return 连接进程列表
     */
    @Operation(summary = "进程列表")
    @GetMapping("/processes")
    @PreAuthorize("hasAuthority('ops:mysql:query')")
    @RateLimiter(name = "api")
    public ApiResponse<List<MysqlProcessView>> processes() {
        return ApiResponse.ok(mysqlOpsService.listProcesses());
    }

    /**
     * Kill 连接。
     *
     * @param id 进程 ID
     * @return 执行结果
     */
    @Operation(summary = "Kill 连接")
    @DeleteMapping("/processes/{id}")
    @PreAuthorize("hasAuthority('ops:mysql:remove')")
    @RateLimiter(name = "api")
    @OperLog(module = "MySQL运维", action = "Kill连接")
    public ApiResponse<Map<String, Object>> kill(@PathVariable long id) {
        return ApiResponse.ok(mysqlOpsService.killProcess(id));
    }
}

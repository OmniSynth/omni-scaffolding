package com.omni.scaffolding.modules.tool.gen.controller;

import com.omni.scaffolding.common.api.ApiResponse;
import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.api.PageResult;
import com.omni.scaffolding.common.exception.BusinessException;
import com.omni.scaffolding.modules.ops.dto.MysqlTableView;
import com.omni.scaffolding.modules.system.dto.dict.DictTypeView;
import com.omni.scaffolding.modules.system.service.DictService;
import com.omni.scaffolding.modules.tool.gen.dto.GenFileView;
import com.omni.scaffolding.modules.tool.gen.dto.GenTableConfig;
import com.omni.scaffolding.modules.tool.gen.service.GenService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 在线 CRUD 代码生成接口。
 *
 * <p>权限：{@code tool:gen:query/preview/code}。仅预览/下载 ZIP，不写服务器磁盘。
 */
@Tag(name = "Tool Gen")
@RestController
@RequestMapping("/api/tool/gen")
@RequiredArgsConstructor
@Profile("!test")
public class GenController {

    private final GenService genService;
    private final DictService dictService;

    /**
     * 可生成的业务表列表。
     *
     * @param keyword 可选，表名模糊匹配
     * @param page    页码
     * @param size    每页条数
     * @return 分页表列表
     */
    @Operation(summary = "可生成表列表")
    @GetMapping("/tables")
    @PreAuthorize("hasAuthority('tool:gen:query')")
    @RateLimiter(name = "api")
    public ApiResponse<PageResult<MysqlTableView>> tables(@RequestParam(required = false) String keyword,
                                                          @RequestParam(required = false) Long page,
                                                          @RequestParam(required = false) Long size) {
        return ApiResponse.ok(genService.listTables(keyword, page, size));
    }

    /**
     * 代码生成字段可选的启用字典类型。
     *
     * @return 启用字典类型列表
     */
    @Operation(summary = "可选字典类型")
    @GetMapping("/dict-types")
    @PreAuthorize("hasAuthority('tool:gen:query')")
    @RateLimiter(name = "api")
    public ApiResponse<List<DictTypeView>> dictTypes() {
        List<DictTypeView> types = dictService.listTypes(null, 1L, 200L).getRecords().stream()
                .filter(type -> Boolean.TRUE.equals(type.getStatus()))
                .toList();
        return ApiResponse.ok(types);
    }

    /**
     * 按表名返回默认生成配置（含列推断）。
     *
     * @param table 表名
     * @return 默认配置
     */
    @Operation(summary = "表默认生成配置")
    @GetMapping("/tables/{table}/columns")
    @PreAuthorize("hasAuthority('tool:gen:query')")
    @RateLimiter(name = "api")
    public ApiResponse<GenTableConfig> columns(@PathVariable String table) {
        return ApiResponse.ok(genService.defaultConfig(table));
    }

    /**
     * 预览全部生成文件。
     *
     * @param config 表与列配置
     * @return 文件路径与内容
     */
    @Operation(summary = "预览生成代码")
    @PostMapping("/preview")
    @PreAuthorize("hasAuthority('tool:gen:preview')")
    @RateLimiter(name = "api")
    public ApiResponse<List<GenFileView>> preview(@Valid @RequestBody GenTableConfig config) {
        return ApiResponse.ok(genService.preview(config));
    }

    /**
     * 下载生成代码 ZIP。
     *
     * @param config   表与列配置
     * @param response HTTP 响应
     */
    @Operation(summary = "下载生成代码 ZIP")
    @PostMapping("/download")
    @PreAuthorize("hasAuthority('tool:gen:code')")
    @RateLimiter(name = "api")
    public void download(@Valid @RequestBody GenTableConfig config, HttpServletResponse response) {
        byte[] zip = genService.downloadZip(config);
        String filename = config.getClassName() + "-gen.zip";
        try {
            response.setContentType("application/zip");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);
            response.getOutputStream().write(zip);
            response.flushBuffer();
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "下载 ZIP 失败: " + ex.getMessage());
        }
    }
}

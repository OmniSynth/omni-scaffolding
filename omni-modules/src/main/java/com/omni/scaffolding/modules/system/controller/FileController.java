package com.omni.scaffolding.modules.system.controller;

import com.omni.scaffolding.common.api.ApiResponse;
import com.omni.scaffolding.common.api.PageQuery;
import com.omni.scaffolding.common.api.PageResult;
import com.omni.scaffolding.common.audit.OperLog;
import com.omni.scaffolding.modules.system.dto.file.FilePreviewUrlView;
import com.omni.scaffolding.modules.system.dto.file.FileView;
import com.omni.scaffolding.modules.system.service.FileService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 统一文件管理接口。
 *
 * <p>权限码：{@code system:file:list|query|upload|remove}；上传亦允许用户编辑权限以便头像场景。
 * {@code GET .../content} 在 security permit-all 中，由服务校验 JWT 或短时签名。
 */
@Tag(name = "Files")
@RestController
@RequestMapping("/api/system/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /**
     * 分页查询文件元数据。
     *
     * @param keyword           可选，文件名关键字
     * @param bizType           可选，业务类型
     * @param storageType       可选，存储类型
     * @param contentTypePrefix 可选，MIME 前缀
     * @param page              页码
     * @param size              每页条数
     * @return 分页结果（含 previewUrl）
     */
    @Operation(summary = "分页查询文件")
    @GetMapping
    @PreAuthorize("hasAuthority('system:file:list')")
    @RateLimiter(name = "api")
    public ApiResponse<PageResult<FileView>> page(@RequestParam(required = false) String keyword,
                                                  @RequestParam(required = false) String bizType,
                                                  @RequestParam(required = false) String storageType,
                                                  @RequestParam(required = false) String contentTypePrefix,
                                                  @RequestParam(required = false) Long page,
                                                  @RequestParam(required = false) Long size) {
        return ApiResponse.ok(fileService.page(keyword, bizType, storageType, contentTypePrefix,
                PageQuery.of(page, size)));
    }

    /**
     * 文件详情。
     *
     * @param id 文件主键
     * @return 元数据（含 previewUrl）
     */
    @Operation(summary = "文件详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:file:query')")
    @RateLimiter(name = "api")
    public ApiResponse<FileView> detail(@PathVariable Long id) {
        return ApiResponse.ok(fileService.get(id));
    }

    /**
     * 上传文件并落库元数据。
     *
     * @param file    multipart 文件，字段名 {@code file}
     * @param bizType 业务类型，默认 {@code common}；头像用 {@code avatar}
     * @return 含 id 与 previewUrl 的读模型
     */
    @Operation(summary = "上传文件")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('system:file:upload') or hasAnyAuthority('system:user:add','system:user:edit')")
    @OperLog(module = "文件管理", action = "上传")
    @RateLimiter(name = "api")
    public ApiResponse<FileView> upload(@RequestPart("file") MultipartFile file,
                                        @RequestParam(required = false, defaultValue = "common") String bizType) {
        return ApiResponse.ok(fileService.upload(file, bizType));
    }

    /**
     * 生成短时预览 URL（需登录）。
     *
     * @param id 文件主键
     * @return 预览地址与过期时间
     */
    @Operation(summary = "生成短时预览 URL")
    @GetMapping("/{id}/preview-url")
    @PreAuthorize("isAuthenticated()")
    @RateLimiter(name = "api")
    public ApiResponse<FilePreviewUrlView> previewUrl(@PathVariable Long id) {
        return ApiResponse.ok(fileService.previewUrl(id));
    }

    /**
     * 下载/内联预览文件内容。
     *
     * @param id     文件主键
     * @param expire 签名过期时间（Unix 秒），可与 sign 成对使用
     * @param sign   HMAC 签名；与 JWT 二选一
     * @return 流式响应
     */
    @Operation(summary = "下载/预览文件内容（JWT 或签名）")
    @GetMapping("/{id}/content")
    @RateLimiter(name = "api")
    public ResponseEntity<InputStreamResource> content(@PathVariable Long id,
                                                       @RequestParam(required = false) Long expire,
                                                       @RequestParam(required = false) String sign) {
        return fileService.content(id, expire, sign);
    }

    /**
     * 逻辑删除文件（可配置同步物理删除）。
     *
     * @param id 文件主键
     * @return 空成功响应
     */
    @Operation(summary = "删除文件")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:file:remove')")
    @OperLog(module = "文件管理", action = "删除")
    @RateLimiter(name = "api")
    public ApiResponse<Void> remove(@PathVariable Long id) {
        fileService.remove(id);
        return ApiResponse.ok();
    }
}

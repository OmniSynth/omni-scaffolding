package com.omni.scaffolding.modules.system.controller;

import com.omni.scaffolding.common.api.ApiResponse;
import com.omni.scaffolding.common.api.PageResult;
import com.omni.scaffolding.common.audit.OperLog;
import com.omni.scaffolding.common.excel.ExcelExportHelper;
import com.omni.scaffolding.modules.system.dto.DictDataSaveRequest;
import com.omni.scaffolding.modules.system.dto.DictDataView;
import com.omni.scaffolding.modules.system.dto.DictOptionView;
import com.omni.scaffolding.modules.system.dto.DictStatusRequest;
import com.omni.scaffolding.modules.system.dto.DictTypeSaveRequest;
import com.omni.scaffolding.modules.system.dto.DictTypeView;
import com.omni.scaffolding.modules.system.dto.excel.DictDataExportRow;
import com.omni.scaffolding.modules.system.dto.excel.DictTypeExportRow;
import com.omni.scaffolding.modules.system.service.DictService;
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

import java.util.List;

/**
 * 数据字典接口。
 *
 * <p>权限码：{@code system:dict:query/add/edit/remove/export}。
 * 业务下拉 {@code /options/{typeCode}} 仅需登录。
 */
@Tag(name = "Dicts")
@RestController
@RequestMapping("/api/system/dicts")
@RequiredArgsConstructor
public class DictController {

    private final DictService dictService;

    // -------------------------------------------------------------------------
    // 业务复用
    // -------------------------------------------------------------------------

    /**
     * 按类型编码获取启用中的下拉选项。
     *
     * @param typeCode 字典类型编码
     * @return 选项列表
     */
    @Operation(summary = "按类型编码获取启用中的下拉选项")
    @GetMapping("/options/{typeCode}")
    @PreAuthorize("isAuthenticated()")
    @RateLimiter(name = "api")
    public ApiResponse<List<DictOptionView>> options(@PathVariable String typeCode) {
        return ApiResponse.ok(dictService.listOptions(typeCode));
    }

    // -------------------------------------------------------------------------
    // 类型
    // -------------------------------------------------------------------------

    /**
     * 字典类型分页列表。
     *
     * @param keyword 可选，匹配编码 / 名称
     * @param page    页码
     * @param size    每页条数
     * @return 分页结果
     */
    @Operation(summary = "字典类型分页列表")
    @GetMapping("/types")
    @PreAuthorize("hasAuthority('system:dict:query')")
    @RateLimiter(name = "api")
    public ApiResponse<PageResult<DictTypeView>> listTypes(@RequestParam(required = false) String keyword,
                                                           @RequestParam(required = false) Long page,
                                                           @RequestParam(required = false) Long size) {
        return ApiResponse.ok(dictService.listTypes(keyword, page, size));
    }

    /**
     * 导出字典类型 Excel。
     *
     * @param keyword  可选，匹配编码 / 名称
     * @param response HTTP 响应，直接写入文件流
     */
    @Operation(summary = "导出字典类型 Excel")
    @GetMapping("/types/export")
    @PreAuthorize("hasAuthority('system:dict:export')")
    @RateLimiter(name = "api")
    @OperLog(module = "数据字典", action = "导出类型")
    public void exportTypes(@RequestParam(required = false) String keyword, HttpServletResponse response) {
        ExcelExportHelper.write(response, "字典类型.xlsx", DictTypeExportRow.class, dictService.exportTypes(keyword));
    }

    /**
     * 字典类型详情。
     *
     * @param id 类型主键
     * @return 读模型
     */
    @Operation(summary = "字典类型详情")
    @GetMapping("/types/{id}")
    @PreAuthorize("hasAuthority('system:dict:query')")
    @RateLimiter(name = "api")
    public ApiResponse<DictTypeView> typeDetail(@PathVariable Long id) {
        return ApiResponse.ok(dictService.getType(id));
    }

    /**
     * 新增字典类型。
     *
     * @param request 保存请求
     * @return 新建读模型
     */
    @Operation(summary = "新增字典类型")
    @PostMapping("/types")
    @PreAuthorize("hasAuthority('system:dict:add')")
    @RateLimiter(name = "api")
    @OperLog(module = "数据字典", action = "新增类型")
    public ApiResponse<DictTypeView> createType(@Valid @RequestBody DictTypeSaveRequest request) {
        return ApiResponse.ok(dictService.createType(request));
    }

    /**
     * 修改字典类型。
     *
     * @param id      类型主键
     * @param request 保存请求
     * @return 更新后读模型
     */
    @Operation(summary = "修改字典类型")
    @PutMapping("/types/{id}")
    @PreAuthorize("hasAuthority('system:dict:edit')")
    @RateLimiter(name = "api")
    @OperLog(module = "数据字典", action = "修改类型")
    public ApiResponse<DictTypeView> updateType(@PathVariable Long id, @Valid @RequestBody DictTypeSaveRequest request) {
        return ApiResponse.ok(dictService.updateType(id, request));
    }

    /**
     * 启用 / 停用字典类型。
     *
     * @param id      类型主键
     * @param request 状态请求
     * @return 更新后读模型
     */
    @Operation(summary = "启用/停用字典类型")
    @PutMapping("/types/{id}/status")
    @PreAuthorize("hasAuthority('system:dict:edit')")
    @RateLimiter(name = "api")
    @OperLog(module = "数据字典", action = "变更类型状态")
    public ApiResponse<DictTypeView> changeTypeStatus(@PathVariable Long id,
                                                      @Valid @RequestBody DictStatusRequest request) {
        return ApiResponse.ok(dictService.changeTypeStatus(id, Boolean.TRUE.equals(request.getStatus())));
    }

    /**
     * 删除字典类型。
     *
     * @param id 类型主键
     * @return 空成功响应
     */
    @Operation(summary = "删除字典类型")
    @DeleteMapping("/types/{id}")
    @PreAuthorize("hasAuthority('system:dict:remove')")
    @RateLimiter(name = "api")
    @OperLog(module = "数据字典", action = "删除类型")
    public ApiResponse<Void> removeType(@PathVariable Long id) {
        dictService.removeType(id);
        return ApiResponse.ok();
    }

    // -------------------------------------------------------------------------
    // 数据
    // -------------------------------------------------------------------------

    /**
     * 字典数据分页列表。
     *
     * @param typeCode 字典类型编码，必填
     * @param keyword  可选，匹配标签 / 键值
     * @param page     页码
     * @param size     每页条数
     * @return 分页结果
     */
    @Operation(summary = "字典数据分页列表")
    @GetMapping("/data")
    @PreAuthorize("hasAuthority('system:dict:query')")
    @RateLimiter(name = "api")
    public ApiResponse<PageResult<DictDataView>> listData(@RequestParam String typeCode,
                                                          @RequestParam(required = false) String keyword,
                                                          @RequestParam(required = false) Long page,
                                                          @RequestParam(required = false) Long size) {
        return ApiResponse.ok(dictService.listData(typeCode, keyword, page, size));
    }

    /**
     * 导出字典数据 Excel。
     *
     * @param typeCode 字典类型编码，必填
     * @param keyword  可选，匹配标签 / 键值
     * @param response HTTP 响应，直接写入文件流
     */
    @Operation(summary = "导出字典数据 Excel")
    @GetMapping("/data/export")
    @PreAuthorize("hasAuthority('system:dict:export')")
    @RateLimiter(name = "api")
    @OperLog(module = "数据字典", action = "导出数据")
    public void exportData(@RequestParam String typeCode,
                           @RequestParam(required = false) String keyword,
                           HttpServletResponse response) {
        ExcelExportHelper.write(response, "字典数据.xlsx", DictDataExportRow.class,
                dictService.exportData(typeCode, keyword));
    }

    /**
     * 字典数据详情。
     *
     * @param id 数据主键
     * @return 读模型
     */
    @Operation(summary = "字典数据详情")
    @GetMapping("/data/{id}")
    @PreAuthorize("hasAuthority('system:dict:query')")
    @RateLimiter(name = "api")
    public ApiResponse<DictDataView> dataDetail(@PathVariable Long id) {
        return ApiResponse.ok(dictService.getData(id));
    }

    /**
     * 新增字典数据。
     *
     * @param request 保存请求
     * @return 新建读模型
     */
    @Operation(summary = "新增字典数据")
    @PostMapping("/data")
    @PreAuthorize("hasAuthority('system:dict:add')")
    @RateLimiter(name = "api")
    @OperLog(module = "数据字典", action = "新增数据")
    public ApiResponse<DictDataView> createData(@Valid @RequestBody DictDataSaveRequest request) {
        return ApiResponse.ok(dictService.createData(request));
    }

    /**
     * 修改字典数据。
     *
     * @param id      数据主键
     * @param request 保存请求
     * @return 更新后读模型
     */
    @Operation(summary = "修改字典数据")
    @PutMapping("/data/{id}")
    @PreAuthorize("hasAuthority('system:dict:edit')")
    @RateLimiter(name = "api")
    @OperLog(module = "数据字典", action = "修改数据")
    public ApiResponse<DictDataView> updateData(@PathVariable Long id, @Valid @RequestBody DictDataSaveRequest request) {
        return ApiResponse.ok(dictService.updateData(id, request));
    }

    /**
     * 启用 / 停用字典数据。
     *
     * @param id      数据主键
     * @param request 状态请求
     * @return 更新后读模型
     */
    @Operation(summary = "启用/停用字典数据")
    @PutMapping("/data/{id}/status")
    @PreAuthorize("hasAuthority('system:dict:edit')")
    @RateLimiter(name = "api")
    @OperLog(module = "数据字典", action = "变更数据状态")
    public ApiResponse<DictDataView> changeDataStatus(@PathVariable Long id,
                                                      @Valid @RequestBody DictStatusRequest request) {
        return ApiResponse.ok(dictService.changeDataStatus(id, Boolean.TRUE.equals(request.getStatus())));
    }

    /**
     * 删除字典数据。
     *
     * @param id 数据主键
     * @return 空成功响应
     */
    @Operation(summary = "删除字典数据")
    @DeleteMapping("/data/{id}")
    @PreAuthorize("hasAuthority('system:dict:remove')")
    @RateLimiter(name = "api")
    @OperLog(module = "数据字典", action = "删除数据")
    public ApiResponse<Void> removeData(@PathVariable Long id) {
        dictService.removeData(id);
        return ApiResponse.ok();
    }
}

package ${cfg.packageName}.controller;

import com.omni.scaffolding.common.api.ApiResponse;
import com.omni.scaffolding.common.api.PageResult;
import com.omni.scaffolding.common.audit.OperLog;
import ${cfg.packageName}.dto.${functionCamel}SaveRequest;
import ${cfg.packageName}.dto.${functionCamel}View;
import ${cfg.packageName}.service.${functionCamel}Service;
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
<#if needInstant>
import java.time.Instant;
</#if>

/**
 * ${cfg.functionName}接口。
 *
 * <p>权限：{@code ${perm}:query/add/edit/remove}
 *
 * @author ${cfg.author}
 */
@Tag(name = "${cfg.functionName}")
@RestController
@RequestMapping("${apiPath}")
@RequiredArgsConstructor
public class ${functionCamel}Controller {

    private final ${functionCamel}Service ${functionCamelLower}Service;

    /**
     * 分页列表。
     */
    @Operation(summary = "${cfg.functionName}分页列表")
    @GetMapping
    @PreAuthorize("hasAuthority('${perm}:query')")
    @RateLimiter(name = "api")
    public ApiResponse<PageResult<${functionCamel}View>> list(<#list queryColumns as col><#if col.queryType == "BETWEEN">@RequestParam(required = false) ${col.javaType} ${col.javaField}From,
                                                              @RequestParam(required = false) ${col.javaType} ${col.javaField}To<#else>@RequestParam(required = false) ${col.javaType} ${col.javaField}</#if>,
                                                              </#list>@RequestParam(required = false) Long page,
                                                              @RequestParam(required = false) Long size) {
        return ApiResponse.ok(${functionCamelLower}Service.list(<#list queryColumns as col><#if col.queryType == "BETWEEN">${col.javaField}From, ${col.javaField}To<#else>${col.javaField}</#if>, </#list>page, size));
    }

    /**
     * 详情。
     */
    @Operation(summary = "${cfg.functionName}详情")
    @GetMapping("/{${cfg.pkField}}")
    @PreAuthorize("hasAuthority('${perm}:query')")
    @RateLimiter(name = "api")
    public ApiResponse<${functionCamel}View> detail(@PathVariable ${cfg.pkJavaType} ${cfg.pkField}) {
        return ApiResponse.ok(${functionCamelLower}Service.detail(${cfg.pkField}));
    }

    /**
     * 新增。
     */
    @Operation(summary = "新增${cfg.functionName}")
    @PostMapping
    @PreAuthorize("hasAuthority('${perm}:add')")
    @RateLimiter(name = "api")
    @OperLog(module = "${cfg.functionName}", action = "新增")
    public ApiResponse<${functionCamel}View> create(@Valid @RequestBody ${functionCamel}SaveRequest request) {
        return ApiResponse.ok(${functionCamelLower}Service.create(request));
    }

    /**
     * 修改。
     */
    @Operation(summary = "修改${cfg.functionName}")
    @PutMapping("/{${cfg.pkField}}")
    @PreAuthorize("hasAuthority('${perm}:edit')")
    @RateLimiter(name = "api")
    @OperLog(module = "${cfg.functionName}", action = "修改")
    public ApiResponse<${functionCamel}View> update(@PathVariable ${cfg.pkJavaType} ${cfg.pkField},
                                                    @Valid @RequestBody ${functionCamel}SaveRequest request) {
        return ApiResponse.ok(${functionCamelLower}Service.update(${cfg.pkField}, request));
    }

    /**
     * 删除。
     */
    @Operation(summary = "删除${cfg.functionName}")
    @DeleteMapping("/{${cfg.pkField}}")
    @PreAuthorize("hasAuthority('${perm}:remove')")
    @RateLimiter(name = "api")
    @OperLog(module = "${cfg.functionName}", action = "删除")
    public ApiResponse<Void> remove(@PathVariable ${cfg.pkJavaType} ${cfg.pkField}) {
        ${functionCamelLower}Service.remove(${cfg.pkField});
        return ApiResponse.ok();
    }
}

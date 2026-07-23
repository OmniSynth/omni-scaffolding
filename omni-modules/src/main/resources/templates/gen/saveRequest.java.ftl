package ${cfg.packageName}.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
<#if needBigDecimal>
import java.math.BigDecimal;
</#if>
<#if needInstant>
import java.time.Instant;
</#if>

/**
 * ${cfg.functionName}新增 / 修改请求体。
 *
 * @author ${cfg.author}
 */
@Data
public class ${functionCamel}SaveRequest {

<#list formColumns as col>
    /** ${col.columnComment}。 */
<#if col.required && col.javaType == "String">
    @NotBlank(message = "${col.columnComment}不能为空")
<#elseif col.required>
    @NotNull(message = "${col.columnComment}不能为空")
</#if>
    private ${col.javaType} ${col.javaField};

</#list>
}

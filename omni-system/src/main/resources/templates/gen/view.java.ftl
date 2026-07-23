package ${cfg.packageName}.dto;

import lombok.Data;
<#if needBigDecimal>
import java.math.BigDecimal;
</#if>
<#if needInstant>
import java.time.Instant;
</#if>

/**
 * ${cfg.functionName}读模型。
 *
 * @author ${cfg.author}
 */
@Data
public class ${functionCamel}View {

<#list cfg.columns as col>
<#if !col.logicDelete>
    /** ${col.columnComment}。 */
    private ${col.javaType} ${col.javaField};

</#if>
</#list>
}

package ${cfg.packageName}.dto;

<#if hasDictColumns>
import com.omni.scaffolding.common.dict.DictText;
</#if>
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

<#list viewColumns as col>
    /** ${col.columnComment}。 */
<#if col.dictType?? && col.dictType?has_content>
    @DictText("${col.dictType}")
</#if>
    private ${col.javaType} ${col.javaField};

</#list>
}

package ${cfg.packageName}.mapper;

import ${cfg.packageName}.dto.${functionCamel}View;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
<#if needInstant>
import java.time.Instant;
</#if>

/**
 * ${cfg.functionName}读模型 Mapper（MyBatis）。
 *
 * @author ${cfg.author}
 */
@Mapper
public interface ${cfg.className}QueryMapper {

    /**
     * 统计条数。
     */
    long count(<#list queryColumns as col><#if col.queryType == "BETWEEN">@Param("${col.javaField}From") ${col.javaType} ${col.javaField}From,
               @Param("${col.javaField}To") ${col.javaType} ${col.javaField}To<#else>@Param("${col.javaField}") ${col.javaType} ${col.javaField}</#if><#if col_has_next>,
               </#if></#list>);

    /**
     * 分页列表。
     */
    List<${functionCamel}View> list(<#list queryColumns as col><#if col.queryType == "BETWEEN">@Param("${col.javaField}From") ${col.javaType} ${col.javaField}From,
                                    @Param("${col.javaField}To") ${col.javaType} ${col.javaField}To<#else>@Param("${col.javaField}") ${col.javaType} ${col.javaField}</#if>,
                                    </#list>@Param("limit") long limit,
                                    @Param("offset") long offset);

    /**
     * 详情。
     */
    ${functionCamel}View findById(@Param("id") ${cfg.pkJavaType} id);
}

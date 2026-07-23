package ${cfg.packageName}.service;

import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.api.PageQuery;
import com.omni.scaffolding.common.api.PageResult;
import com.omni.scaffolding.common.exception.BusinessException;
import com.omni.scaffolding.common.util.IdGenerator;
import ${cfg.packageName}.dto.${functionCamel}SaveRequest;
import ${cfg.packageName}.dto.${functionCamel}View;
import ${cfg.packageName}.entity.${cfg.className};
import ${cfg.packageName}.mapper.${cfg.className}QueryMapper;
import ${cfg.packageName}.repository.${cfg.className}Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
<#if needInstant>
import java.time.Instant;
</#if>
import java.util.List;

/**
 * ${cfg.functionName}领域服务（JPA 写 / MyBatis 读）。
 *
 * @author ${cfg.author}
 */
@Service
@RequiredArgsConstructor
public class ${functionCamel}Service {

    private final ${cfg.className}Repository ${functionCamelLower}Repository;
    private final ${cfg.className}QueryMapper ${functionCamelLower}QueryMapper;

    /**
     * 分页查询。
     */
    @Transactional(readOnly = true)
    public PageResult<${functionCamel}View> list(<#list queryColumns as col><#if col.queryType == "BETWEEN">${col.javaType} ${col.javaField}From,
                                                 ${col.javaType} ${col.javaField}To<#else>${col.javaType} ${col.javaField}</#if>,
                                                 </#list>Long page,
                                                 Long size) {
        PageQuery pq = PageQuery.of(page, size);
        long total = ${functionCamelLower}QueryMapper.count(<#list queryColumns as col><#if col.queryType == "BETWEEN">${col.javaField}From, ${col.javaField}To<#else>${col.javaField}</#if><#if col_has_next>, </#if></#list>);
        if (total == 0) {
            return pq.toResult(0, List.of());
        }
        return pq.toResult(total, ${functionCamelLower}QueryMapper.list(<#list queryColumns as col><#if col.queryType == "BETWEEN">${col.javaField}From, ${col.javaField}To<#else>${col.javaField}</#if>, </#list>pq.getSize(), pq.getOffset()));
    }

    /**
     * 详情。
     */
    @Transactional(readOnly = true)
    public ${functionCamel}View detail(${cfg.pkJavaType} ${cfg.pkField}) {
        ${functionCamel}View view = ${functionCamelLower}QueryMapper.findById(${cfg.pkField});
        if (view == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "${cfg.functionName}不存在");
        }
        return view;
    }

    /**
     * 新增。
     */
    @Transactional
    public ${functionCamel}View create(${functionCamel}SaveRequest request) {
        ${cfg.className} entity = new ${cfg.className}();
        entity.set${cfg.pkField?cap_first}(IdGenerator.nextId());
<#if cfg.hasDeleted>
        entity.setDeleted(0);
</#if>
        applyMutable(entity, request);
        ${functionCamelLower}Repository.saveAndFlush(entity);
        return detail(entity.get${cfg.pkField?cap_first}());
    }

    /**
     * 修改。
     */
    @Transactional
    public ${functionCamel}View update(${cfg.pkJavaType} ${cfg.pkField}, ${functionCamel}SaveRequest request) {
        ${cfg.className} entity = requireEntity(${cfg.pkField});
        applyMutable(entity, request);
        ${functionCamelLower}Repository.saveAndFlush(entity);
        return detail(${cfg.pkField});
    }

    /**
     * 删除。
     */
    @Transactional
    public void remove(${cfg.pkJavaType} ${cfg.pkField}) {
        ${cfg.className} entity = requireEntity(${cfg.pkField});
<#if cfg.hasDeleted>
        entity.setDeleted(1);
        ${functionCamelLower}Repository.save(entity);
<#else>
        ${functionCamelLower}Repository.delete(entity);
</#if>
    }

    private ${cfg.className} requireEntity(${cfg.pkJavaType} ${cfg.pkField}) {
<#if cfg.hasDeleted>
        return ${functionCamelLower}Repository.findBy${cfg.pkField?cap_first}AndDeleted(${cfg.pkField}, 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "${cfg.functionName}不存在"));
<#else>
        return ${functionCamelLower}Repository.findById(${cfg.pkField})
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "${cfg.functionName}不存在"));
</#if>
    }

    private void applyMutable(${cfg.className} entity, ${functionCamel}SaveRequest request) {
<#list formColumns as col>
        entity.set${col.javaField?cap_first}(request.get${col.javaField?cap_first}());
</#list>
    }
}

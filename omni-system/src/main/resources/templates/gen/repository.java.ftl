package ${cfg.packageName}.repository;

import ${cfg.packageName}.entity.${cfg.className};
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * ${cfg.functionName} JPA 写仓储。
 *
 * @author ${cfg.author}
 */
public interface ${cfg.className}Repository extends JpaRepository<${cfg.className}, ${cfg.pkJavaType}> {

<#if cfg.hasDeleted>
    /**
     * 按主键查询未删除记录。
     *
     * @param ${cfg.pkField} 主键
     * @param deleted 删除标记
     * @return 实体
     */
    Optional<${cfg.className}> findBy${cfg.pkField?cap_first}AndDeleted(${cfg.pkJavaType} ${cfg.pkField}, Integer deleted);
</#if>
}

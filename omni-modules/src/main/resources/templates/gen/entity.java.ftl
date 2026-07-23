package ${cfg.packageName}.entity;

<#if cfg.extendsAudit>
import com.omni.scaffolding.common.persistence.BaseAuditableEntity;
</#if>
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
<#if needBigDecimal>
import java.math.BigDecimal;
</#if>
<#if needInstant>
import java.time.Instant;
</#if>

/**
 * ${cfg.functionName}实体（JPA 写模型）。
 *
 * <p>表：{@code ${cfg.tableName}}
 *
 * @author ${cfg.author}
 */
@Getter
@Setter
@Entity
@Table(name = "${cfg.tableName}")
public class ${cfg.className}<#if cfg.extendsAudit> extends BaseAuditableEntity</#if> {

<#list entityColumns as col>
    /**
     * ${col.columnComment}。
     */
<#if col.pk>
    @Id
</#if>
<#if col.columnName == col.javaField>
    @Column(<#if !col.nullable>nullable = false</#if>)
<#else>
    @Column(name = "${col.columnName}"<#if !col.nullable>, nullable = false</#if>)
</#if>
    private ${col.javaType} ${col.javaField}<#if col.logicDelete> = 0</#if>;

</#list>
}

package com.omni.scaffolding.modules.system.entity;

import com.omni.scaffolding.common.persistence.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 组织部门实体（JPA 写模型）。
 *
 * <p>树形结构：{@code parentId=0} 为根；{@code ancestors} 存祖先路径（如 {@code 0,1}），
 * 用于「本部门及以下」数据范围的子树查询（兼容 MySQL / H2）。
 */
@Getter
@Setter
@Entity
@Table(name = "sys_dept")
public class SysDept extends BaseAuditableEntity {

    /**
     * 主键 ID。
     */
    @Id
    private Long id;

    /**
     * 父部门 ID；根部门为 0。
     */
    @Column(name = "parent_id", nullable = false)
    private Long parentId = 0L;

    /**
     * 部门名称。
     */
    @Column(nullable = false, length = 64)
    private String name;

    /**
     * 同级排序，越小越靠前。
     */
    @Column(nullable = false)
    private Integer sort = 0;

    /**
     * 祖先路径，逗号分隔，如 {@code 0,1}；不含自身 ID。
     */
    @Column(nullable = false, length = 512)
    private String ancestors = "0";

    /**
     * 是否启用；停用后一般不再出现在可选部门树中。
     */
    @Column(nullable = false)
    private Boolean status = true;

    /**
     * 逻辑删除标记：0=正常，1=已删除。
     */
    @Column(nullable = false)
    private Integer deleted = 0;
}

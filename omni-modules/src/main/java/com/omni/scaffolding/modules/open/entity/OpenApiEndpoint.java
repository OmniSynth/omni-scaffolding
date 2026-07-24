package com.omni.scaffolding.modules.open.entity;

import com.omni.scaffolding.common.persistence.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 开放接口目录实体（JPA 写模型）。
 *
 * <p>对应表 {@code open_api_endpoint}；供第三方可访问接口登记与按客户端授权绑定。
 */
@Getter
@Setter
@Entity
@Table(name = "open_api_endpoint")
public class OpenApiEndpoint extends BaseAuditableEntity {

    /**
     * 主键。
     */
    @Id
    private Long id;

    /**
     * 接口编码，未删除范围内唯一，如 {@code open.demo.ping}。
     */
    @Column(nullable = false, length = 64)
    private String code;

    /**
     * 接口显示名称。
     */
    @Column(nullable = false, length = 128)
    private String name;

    /**
     * HTTP 方法：{@code GET}/{@code POST}/…，{@code *} 表示任意方法。
     */
    @Column(name = "http_method", nullable = false, length = 16)
    private String httpMethod;

    /**
     * Ant 风格路径，如 {@code /api/open/demo/ping} 或 {@code /api/open/demo/**}。
     */
    @Column(name = "path_pattern", nullable = false, length = 255)
    private String pathPattern;

    /**
     * 备注说明。
     */
    @Column(length = 255)
    private String remark;

    /**
     * 是否启用：停用后不可再被授权匹配。
     */
    @Column(nullable = false)
    private Boolean status = true;

    /**
     * 逻辑删除：0 正常，1 已删除。
     */
    @Column(nullable = false)
    private Integer deleted = 0;
}

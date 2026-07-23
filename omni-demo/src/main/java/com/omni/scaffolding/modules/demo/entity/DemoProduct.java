package com.omni.scaffolding.modules.demo.entity;

import com.omni.scaffolding.common.persistence.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 演示商品实体（JPA 写模型）。
 *
 * <p>动态搜索与类目聚合见 {@code DemoProductQueryMapper}，结果用独立 DTO，避免 Entity 泄露查询语义。
 */
@Getter
@Setter
@Entity
@Table(name = "demo_product")
public class DemoProduct extends BaseAuditableEntity {

    /**
     * 主键 ID。
     */
    @Id
    private Long id;

    /**
     * 商品 SKU，业务唯一键；创建时会按此加分布式锁。
     */
    @Column(nullable = false, unique = true, length = 64)
    private String sku;

    /**
     * 商品名称。
     */
    @Column(nullable = false, length = 128)
    private String name;

    /**
     * 类目编码，如 BOOK / GEAR，用于聚合统计。
     */
    @Column(nullable = false, length = 64)
    private String category;

    /**
     * 价格（分），避免浮点金额误差。
     */
    @Column(name = "price_cents", nullable = false)
    private Long priceCents;

    /**
     * 库存数量。
     */
    @Column(nullable = false)
    private Integer stock = 0;

    /**
     * 状态：ACTIVE=上架，INACTIVE=下架。
     */
    @Column(nullable = false, length = 32)
    private String status = "ACTIVE";

    /**
     * 逻辑删除标记：0=正常，1=已删除。
     */
    @Column(nullable = false)
    private Integer deleted = 0;
}

package com.omni.scaffolding.modules.demo.dto;

import lombok.Data;

import java.time.Instant;

/**
 * 商品读模型（MyBatis 查询结果），与写侧 {@code DemoProduct} Entity 分离。
 */
@Data
public class ProductView {

    /**
     * 商品 ID。
     */
    private Long id;

    /**
     * 商品 SKU。
     */
    private String sku;

    /**
     * 商品名称。
     */
    private String name;

    /**
     * 类目编码。
     */
    private String category;

    /**
     * 价格（分）。
     */
    private Long priceCents;

    /**
     * 库存数量。
     */
    private Integer stock;

    /**
     * 状态：ACTIVE / INACTIVE。
     */
    private String status;

    /**
     * 创建时间。
     */
    private Instant createdAt;
}

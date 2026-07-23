package com.omni.scaffolding.modules.demo.dto;

import lombok.Data;

/**
 * 商品类目聚合统计读模型（MyBatis）。
 */
@Data
public class CategoryStatsView {

    /**
     * 类目编码。
     */
    private String category;

    /**
     * 该类目下商品总数（未删除）。
     */
    private Long productCount;

    /**
     * 库存合计。
     */
    private Long totalStock;

    /**
     * 平均价格（分）。
     */
    private Long avgPriceCents;

    /**
     * 上架（ACTIVE）商品数。
     */
    private Long activeCount;
}

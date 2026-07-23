package com.omni.scaffolding.modules.demo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建商品请求体。
 */
@Data
public class ProductCreateRequest {

    /**
     * 商品 SKU，业务唯一。
     */
    @NotBlank(message = "SKU 不能为空")
    private String sku;

    /**
     * 商品名称。
     */
    @NotBlank(message = "商品名称不能为空")
    private String name;

    /**
     * 类目编码，如 BOOK / GEAR。
     */
    @NotBlank(message = "类目不能为空")
    private String category;

    /**
     * 价格（分），须 >= 0。
     */
    @NotNull(message = "价格不能为空")
    @Min(value = 0, message = "价格不能为负数")
    private Long priceCents;

    /**
     * 初始库存，须 >= 0。
     */
    @NotNull(message = "库存不能为空")
    @Min(value = 0, message = "库存不能为负数")
    private Integer stock;
}

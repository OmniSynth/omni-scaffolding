package com.omni.scaffolding.modules.demo.es;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;

/**
 * 演示商品 ES 文档（与 MySQL {@code demo_product} 对应的检索模型）。
 *
 * <p>索引名默认 {@code omni_demo_product}，与 {@code omni.elasticsearch.product-index} 保持一致。
 */
@Getter
@Setter
@Document(indexName = "omni_demo_product")
public class DemoProductDocument {

    /**
     * 文档 ID，与商品主键一致。
     */
    @Id
    private String id;

    /**
     * 商品 SKU。
     */
    @Field(type = FieldType.Keyword)
    private String sku;

    /**
     * 商品名称（全文检索）。
     */
    @Field(type = FieldType.Text)
    private String name;

    /**
     * 类目编码。
     */
    @Field(type = FieldType.Keyword)
    private String category;

    /**
     * 价格（分）。
     */
    @Field(type = FieldType.Long)
    private Long priceCents;

    /**
     * 库存数量。
     */
    @Field(type = FieldType.Integer)
    private Integer stock;

    /**
     * 状态：ACTIVE / INACTIVE。
     */
    @Field(type = FieldType.Keyword)
    private String status;

    /**
     * 创建时间。
     */
    @Field(type = FieldType.Date)
    private Instant createdAt;
}

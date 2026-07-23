package com.omni.scaffolding.modules.ops.dto;

import lombok.Data;

/**
 * MySQL 索引信息（按索引名聚合列）。
 */
@Data
public class MysqlIndexView {

    /**
     * 索引名。
     */
    private String name;

    /**
     * 是否唯一索引。
     */
    private boolean unique;

    /**
     * 索引类型（如 BTREE）。
     */
    private String indexType;

    /**
     * 索引列（逗号分隔）。
     */
    private String columns;

    /**
     * 基数估算。
     */
    private Long cardinality;
}

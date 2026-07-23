package com.omni.scaffolding.modules.tool.gen.dto;

import lombok.Data;

/**
 * 代码生成：单列配置。
 */
@Data
public class GenColumnConfig {

    /**
     * 数据库列名。
     */
    private String columnName;

    /**
     * 列类型（含长度，如 varchar(64)）。
     */
    private String columnType;

    /**
     * 列注释 / 显示名。
     */
    private String columnComment;

    /**
     * Java 字段名（驼峰）。
     */
    private String javaField;

    /**
     * Java 类型，如 Long / String / Instant。
     */
    private String javaType;

    /**
     * 是否主键。
     */
    private boolean pk;

    /**
     * 是否自增。
     */
    private boolean increment;

    /**
     * 是否可空。
     */
    private boolean nullable;

    /**
     * 列表是否展示。
     */
    private boolean list;

    /**
     * 表单是否编辑。
     */
    private boolean form;

    /**
     * 表单是否必填。
     */
    private boolean required;

    /**
     * 查询类型：NONE / EQ / LIKE / BETWEEN。
     */
    private String queryType = "NONE";

    /**
     * 可选字典类型编码；配置后为读模型生成 {@code @DictText}。
     */
    private String dictType;

    /**
     * 是否审计/系统字段（created_at 等，一般不进表单）。
     */
    private boolean audit;

    /**
     * 是否逻辑删除字段。
     */
    private boolean logicDelete;
}

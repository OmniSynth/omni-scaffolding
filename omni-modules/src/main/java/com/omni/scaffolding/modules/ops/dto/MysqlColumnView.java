package com.omni.scaffolding.modules.ops.dto;

import lombok.Data;

/**
 * MySQL 列信息。
 */
@Data
public class MysqlColumnView {

    /**
     * 列名。
     */
    private String name;

    /**
     * 列类型。
     */
    private String type;

    /**
     * 是否可空（YES / NO）。
     */
    private String nullable;

    /**
     * 键类型（PRI / UNI / MUL 等）。
     */
    private String columnKey;

    /**
     * 默认值。
     */
    private String defaultValue;

    /**
     * 额外属性（如 auto_increment）。
     */
    private String extra;

    /**
     * 列注释。
     */
    private String comment;

    /**
     * 列序号。
     */
    private Long ordinalPosition;
}

package com.omni.scaffolding.modules.ops.dto;

import lombok.Data;

/**
 * MySQL 表列表项。
 */
@Data
public class MysqlTableView {

    /**
     * 表名。
     */
    private String name;

    /**
     * 存储引擎。
     */
    private String engine;

    /**
     * 行数估算。
     */
    private Long tableRows;

    /**
     * 数据文件大小（字节）。
     */
    private Long dataLength;

    /**
     * 索引文件大小（字节）。
     */
    private Long indexLength;

    /**
     * 碎片空间（字节）。
     */
    private Long dataFree;

    /**
     * 排序规则。
     */
    private String collation;

    /**
     * 表注释。
     */
    private String comment;

    /**
     * 创建时间。
     */
    private String createTime;

    /**
     * 最后更新时间。
     */
    private String updateTime;
}

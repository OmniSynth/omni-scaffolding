package com.omni.scaffolding.modules.ops.dto;

import lombok.Data;

/**
 * MySQL 当前库概览。
 */
@Data
public class MysqlOverviewView {

    /** MySQL 版本。 */
    private String version;

    /** 当前库名。 */
    private String schema;

    /** 表数量。 */
    private Long tableCount;

    /** 总行数估算。 */
    private Long totalRows;

    /** 数据文件大小（字节）。 */
    private Long dataLength;

    /** 索引文件大小（字节）。 */
    private Long indexLength;

    /** 碎片空间（字节）。 */
    private Long dataFree;

    /** 默认字符集。 */
    private String characterSet;

    /** 默认排序规则。 */
    private String collation;
}

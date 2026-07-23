package com.omni.scaffolding.modules.ops.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * MySQL 表详情：列、索引、建表 DDL。
 */
@Data
public class MysqlTableDetailView {

    /** 表摘要。 */
    private MysqlTableView table;

    /** 列列表。 */
    private List<MysqlColumnView> columns = new ArrayList<>();

    /** 索引列表。 */
    private List<MysqlIndexView> indexes = new ArrayList<>();

    /** 建表 DDL。 */
    private String ddl;
}

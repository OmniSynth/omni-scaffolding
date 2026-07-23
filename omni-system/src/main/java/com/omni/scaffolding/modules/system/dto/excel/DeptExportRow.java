package com.omni.scaffolding.modules.system.dto.excel;

import com.omni.scaffolding.common.excel.ExcelColumn;
import com.omni.scaffolding.common.excel.ExcelSheet;
import lombok.Data;

/**
 * 部门导出行（树扁平化）。
 */
@Data
@ExcelSheet("部门数据")
public class DeptExportRow {

    /** 部门ID */
    @ExcelColumn(name = "部门ID", order = 1, width = 12)
    private Long id;

    /** 上级部门 */
    @ExcelColumn(name = "上级部门", order = 2, width = 16)
    private String parentName;

    /** 部门名称 */
    @ExcelColumn(name = "部门名称", order = 3, width = 18)
    private String name;

    /** 排序 */
    @ExcelColumn(name = "排序", order = 4, width = 8)
    private Integer sort;

    /** 用户数 */
    @ExcelColumn(name = "用户数", order = 5, width = 10)
    private Long userCount;

    /** 状态 */
    @ExcelColumn(name = "状态", order = 6, width = 8)
    private String status;
}

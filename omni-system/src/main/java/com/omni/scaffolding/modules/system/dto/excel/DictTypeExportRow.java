package com.omni.scaffolding.modules.system.dto.excel;

import com.omni.scaffolding.common.excel.ExcelColumn;
import com.omni.scaffolding.common.excel.ExcelSheet;
import lombok.Data;

/**
 * 字典类型导出行。
 */
@Data
@ExcelSheet("字典类型")
public class DictTypeExportRow {

    /**
     * 类型ID
     */
    @ExcelColumn(name = "类型ID", order = 1, width = 12)
    private Long id;

    /**
     * 编码
     */
    @ExcelColumn(name = "编码", order = 2, width = 18)
    private String code;

    /**
     * 名称
     */
    @ExcelColumn(name = "名称", order = 3, width = 16)
    private String name;

    /**
     * 排序
     */
    @ExcelColumn(name = "排序", order = 4, width = 8)
    private Integer sort;

    /**
     * 状态
     */
    @ExcelColumn(name = "状态", order = 5, width = 8)
    private String status;

    /**
     * 数据条数
     */
    @ExcelColumn(name = "数据条数", order = 6, width = 10)
    private Long dataCount;

    /**
     * 备注
     */
    @ExcelColumn(name = "备注", order = 7, width = 24)
    private String remark;
}

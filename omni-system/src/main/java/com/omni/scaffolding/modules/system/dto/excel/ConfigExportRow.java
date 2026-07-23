package com.omni.scaffolding.modules.system.dto.excel;

import com.omni.scaffolding.common.excel.ExcelColumn;
import com.omni.scaffolding.common.excel.ExcelSheet;
import lombok.Data;

/**
 * 系统参数导出行。
 */
@Data
@ExcelSheet("系统参数")
public class ConfigExportRow {

    /** 参数ID */
    @ExcelColumn(name = "参数ID", order = 1, width = 12)
    private Long id;

    /** 参数键 */
    @ExcelColumn(name = "参数键", order = 2, width = 28)
    private String configKey;

    /** 参数名称 */
    @ExcelColumn(name = "参数名称", order = 3, width = 18)
    private String configName;

    /** 参数值 */
    @ExcelColumn(name = "参数值", order = 4, width = 28)
    private String configValue;

    /** 排序 */
    @ExcelColumn(name = "排序", order = 5, width = 8)
    private Integer sort;

    /** 状态 */
    @ExcelColumn(name = "状态", order = 6, width = 8)
    private String status;

    /** 内置 */
    @ExcelColumn(name = "内置", order = 7, width = 8)
    private String builtin;

    /** 备注 */
    @ExcelColumn(name = "备注", order = 8, width = 24)
    private String remark;
}

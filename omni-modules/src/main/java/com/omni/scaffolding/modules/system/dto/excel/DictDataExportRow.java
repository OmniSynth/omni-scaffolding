package com.omni.scaffolding.modules.system.dto.excel;

import com.omni.scaffolding.common.excel.ExcelColumn;
import com.omni.scaffolding.common.excel.ExcelSheet;
import lombok.Data;

/**
 * 字典数据导出行。
 */
@Data
@ExcelSheet("字典数据")
public class DictDataExportRow {

    /**
     * 数据ID
     */
    @ExcelColumn(name = "数据ID", order = 1, width = 12)
    private Long id;

    /**
     * 类型编码
     */
    @ExcelColumn(name = "类型编码", order = 2, width = 18)
    private String typeCode;

    /**
     * 标签
     */
    @ExcelColumn(name = "标签", order = 3, width = 16)
    private String label;

    /**
     * 键值
     */
    @ExcelColumn(name = "键值", order = 4, width = 16)
    private String value;

    /**
     * 排序
     */
    @ExcelColumn(name = "排序", order = 5, width = 8)
    private Integer sort;

    /**
     * 样式类
     */
    @ExcelColumn(name = "样式类", order = 6, width = 12)
    private String cssClass;

    /**
     * 默认
     */
    @ExcelColumn(name = "默认", order = 7, width = 8)
    private String defaultFlag;

    /**
     * 状态
     */
    @ExcelColumn(name = "状态", order = 8, width = 8)
    private String status;

    /**
     * 备注
     */
    @ExcelColumn(name = "备注", order = 9, width = 24)
    private String remark;
}

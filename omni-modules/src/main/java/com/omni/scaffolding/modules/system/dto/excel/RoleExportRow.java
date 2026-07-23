package com.omni.scaffolding.modules.system.dto.excel;

import com.omni.scaffolding.common.excel.ExcelColumn;
import com.omni.scaffolding.common.excel.ExcelSheet;
import lombok.Data;

/**
 * 角色导出行。
 */
@Data
@ExcelSheet("角色数据")
public class RoleExportRow {

    /**
     * 角色ID
     */
    @ExcelColumn(name = "角色ID", order = 1, width = 12)
    private Long id;

    /**
     * 编码
     */
    @ExcelColumn(name = "编码", order = 2, width = 16)
    private String code;

    /**
     * 名称
     */
    @ExcelColumn(name = "名称", order = 3, width = 16)
    private String name;

    /**
     * 数据范围
     */
    @ExcelColumn(name = "数据范围", order = 4, width = 16)
    private String dataScope;

    /**
     * 状态
     */
    @ExcelColumn(name = "状态", order = 5, width = 8)
    private String status;

    /**
     * 用户数
     */
    @ExcelColumn(name = "用户数", order = 6, width = 10)
    private Long userCount;

    /**
     * 菜单数
     */
    @ExcelColumn(name = "菜单数", order = 7, width = 10)
    private Integer menuCount;
}

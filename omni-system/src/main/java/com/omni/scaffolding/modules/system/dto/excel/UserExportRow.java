package com.omni.scaffolding.modules.system.dto.excel;

import com.omni.scaffolding.common.excel.ExcelColumn;
import com.omni.scaffolding.common.excel.ExcelSheet;
import lombok.Data;

/**
 * 用户导出行。
 */
@Data
@ExcelSheet("用户数据")
public class UserExportRow {

    /** 用户ID */
    @ExcelColumn(name = "用户ID", order = 1, width = 14)
    private Long id;

    /** 用户名 */
    @ExcelColumn(name = "用户名", order = 2, width = 16)
    private String username;

    /** 姓名 */
    @ExcelColumn(name = "姓名", order = 3, width = 12)
    private String realName;

    /** 昵称 */
    @ExcelColumn(name = "昵称", order = 4, width = 12)
    private String nickname;

    /** 手机号 */
    @ExcelColumn(name = "手机号", order = 5, width = 14)
    private String mobile;

    /** 邮箱 */
    @ExcelColumn(name = "邮箱", order = 6, width = 22)
    private String email;

    /** 性别 */
    @ExcelColumn(name = "性别", order = 7, width = 8)
    private String gender;

    /** 部门 */
    @ExcelColumn(name = "部门", order = 8, width = 16)
    private String deptName;

    /** 岗位 */
    @ExcelColumn(name = "岗位", order = 9, width = 20)
    private String posts;

    /** 角色 */
    @ExcelColumn(name = "角色", order = 10, width = 20)
    private String roles;

    /** 状态 */
    @ExcelColumn(name = "状态", order = 11, width = 8)
    private String enabled;
}

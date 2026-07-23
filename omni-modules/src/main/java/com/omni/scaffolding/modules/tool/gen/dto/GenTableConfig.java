package com.omni.scaffolding.modules.tool.gen.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 代码生成：整表配置（预览 / 下载请求体）。
 */
@Data
public class GenTableConfig {

    /**
     * 表名。
     */
    @NotBlank
    private String tableName;

    /**
     * 表注释。
     */
    private String tableComment;

    /**
     * 模块名，如 demo / system。
     */
    @NotBlank
    private String moduleName;

    /**
     * 业务名（URL/文件名），如 order。
     */
    @NotBlank
    private String businessName;

    /**
     * 实体类名，如 BizOrder。
     */
    @NotBlank
    private String className;

    /**
     * 功能名 / 菜单名，如 订单。
     */
    @NotBlank
    private String functionName;

    /**
     * Java 包路径，如 com.omni.scaffolding.modules.demo。
     */
    @NotBlank
    private String packageName;

    /**
     * 权限前缀，如 demo:order。
     */
    @NotBlank
    private String permissionPrefix;

    /**
     * 作者（注释用）。
     */
    private String author = "omni";

    /**
     * 菜单父级 ID。
     */
    @NotNull
    private Long menuParentId = 1L;

    /**
     * 菜单起始 ID（连续占用 5 个：菜单+4 按钮）。
     */
    @NotNull
    private Long menuIdStart = 5000L;

    /**
     * 菜单排序。
     */
    private Integer menuSort = 99;

    /**
     * 是否包含逻辑删除。
     */
    private boolean hasDeleted;

    /**
     * 是否继承审计基类（存在 created_at/updated_at/version）。
     */
    private boolean extendsAudit;

    /**
     * 主键 Java 字段名。
     */
    private String pkField = "id";

    /**
     * 主键 Java 类型。
     */
    private String pkJavaType = "Long";

    /**
     * 主键列名。
     */
    private String pkColumn = "id";

    /**
     * 列配置。
     */
    @NotEmpty
    @Valid
    private List<GenColumnConfig> columns = new ArrayList<>();
}

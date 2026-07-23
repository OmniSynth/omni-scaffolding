package com.omni.scaffolding.common.excel;

/**
 * 枚举下拉展示文案约定。
 *
 * <p>当字段使用 {@link ExcelColumn#dropdownEnum()} 且枚举实现本接口时：
 * <ul>
 *   <li>导出 / 模板下拉：写入 {@link #getLabel()}，而非 {@link Enum#name()}</li>
 *   <li>导入：同时接受 label 与 {@code name()}（忽略大小写匹配 name）</li>
 * </ul>
 *
 * <h2>示例</h2>
 * <pre>{@code
 * public enum Status implements ExcelDropdownLabel {
 *     ACTIVE("启用"),
 *     INACTIVE("停用");
 *
 *     private final String label;
 *     Status(String label) { this.label = label; }
 *
 *     @Override
 *     public String getLabel() { return label; }
 * }
 * }</pre>
 *
 * @see ExcelColumn#dropdownEnum()
 */
public interface ExcelDropdownLabel {

    /**
     * 返回写入 Excel 下拉列表与单元格的展示值。
     *
     * @return 非空展示文案，建议面向业务用户（可为中文）
     */
    String getLabel();
}

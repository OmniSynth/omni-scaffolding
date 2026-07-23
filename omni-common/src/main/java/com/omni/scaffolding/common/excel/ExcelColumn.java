package com.omni.scaffolding.common.excel;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字段级 Excel 列元数据，供 {@link ExcelUtils} 生成表头、下拉与导入映射。
 *
 * <p>标注在导入/导出 DTO、VO 的字段上；未标注的字段不会参与 Excel 读写。
 *
 * <h2>示例</h2>
 * <pre>{@code
 * @ExcelColumn(name = "状态", order = 2, dropdown = {"启用", "停用"}, required = true)
 * private String status;
 *
 * @ExcelColumn(name = "类型", order = 3, dropdownEnum = ProductType.class)
 * private ProductType type;
 * }</pre>
 *
 * @see ExcelSheet
 * @see ExcelDropdownLabel
 * @see ExcelUtils
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelColumn {

    /**
     * 表头显示名称（导入时按此名称匹配列，忽略前导 {@code *}）。
     *
     * @return 列名，不可为空语义字符串
     */
    String name();

    /**
     * 列顺序，数值越小越靠左。
     *
     * <p>多个字段 {@code order} 相同时，按字段名字典序稳定排序（非源码声明顺序）。
     *
     * @return 排序权重，默认 {@code 0}
     */
    int order() default 0;

    /**
     * 列宽（Apache POI 字符宽度近似值，内部会乘以 256）。
     *
     * @return 列宽，默认 {@code 18}；实际生效时不小于 8
     */
    int width() default 18;

    /**
     * 显式下拉选项列表。
     *
     * <p>与 {@link #dropdownEnum()} 同时配置时，以本属性为准。
     * 选项总长度受 Excel「显式列表」限制，极端长列表需另行扩展隐藏 Sheet 方案。
     *
     * @return 下拉文案数组；空数组表示不使用显式列表
     */
    String[] dropdown() default {};

    /**
     * 由枚举常量生成下拉选项。
     *
     * <ul>
     *   <li>默认取 {@link Enum#name()}</li>
     *   <li>若枚举实现 {@link ExcelDropdownLabel}，则取 {@link ExcelDropdownLabel#getLabel()}</li>
     * </ul>
     *
     * @return 枚举类型；默认 {@link None} 表示未配置
     */
    Class<? extends Enum<?>> dropdownEnum() default None.class;

    /**
     * 是否必填。
     *
     * <p>为 {@code true} 时：导出表头前缀 {@code *}；导入时缺列或空值抛出业务异常。
     *
     * @return {@code true} 表示必填，默认 {@code false}
     */
    boolean required() default false;

    /**
     * 日期时间格式，导出写入与导入解析共用。
     *
     * <p>适用于字段类型为 {@link java.util.Date}、{@link java.time.Instant}、
     * {@link java.time.LocalDate}、{@link java.time.LocalDateTime}。
     * {@link java.time.LocalDate} 导出时若格式含空格，仅使用空格前的日期段。
     *
     * @return {@link java.time.format.DateTimeFormatter} 可识别的 pattern
     */
    String dateFormat() default "yyyy-MM-dd HH:mm:ss";

    /**
     * 表头单元格批注（Excel 中鼠标悬停可见）。
     *
     * <p>未配置时：若有下拉则自动生成「可选: …」；若仅必填则批注「必填」。
     *
     * @return 批注文案；空字符串表示走自动批注逻辑
     */
    String comment() default "";

    /**
     * {@link #dropdownEnum()} 的占位类型，表示「未配置枚举下拉」。
     *
     * <p>请勿在业务代码中使用该枚举。
     */
    enum None {
    }
}

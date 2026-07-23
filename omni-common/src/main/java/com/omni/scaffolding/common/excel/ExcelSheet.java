package com.omni.scaffolding.common.excel;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 类型级 Sheet 名称声明，供 {@link ExcelUtils} 导出时创建工作表。
 *
 * <p>标注在导入/导出模型类上；未标注时默认 Sheet 名为 {@code Sheet1}。
 * 导入始终读取工作簿第一个 Sheet，不受本注解影响。
 *
 * <h2>示例</h2>
 * <pre>{@code
 * @ExcelSheet("商品导入")
 * public class ProductImportRow {
 *     // ...
 * }
 * }</pre>
 *
 * @see ExcelColumn
 * @see ExcelUtils
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelSheet {

    /**
     * 导出时创建的工作表名称。
     *
     * <p>需符合 Excel 命名约束（勿含 {@code : \ / ? * [ ]} 等非法字符，长度不宜过长）。
     *
     * @return Sheet 名称，默认 {@code Sheet1}
     */
    String value() default "Sheet1";
}

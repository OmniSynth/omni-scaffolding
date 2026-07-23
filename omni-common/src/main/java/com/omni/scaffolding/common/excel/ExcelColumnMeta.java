package com.omni.scaffolding.common.excel;

import java.lang.reflect.Field;
import java.util.List;

/**
 * 已解析的单列元数据，供 {@link ExcelUtils} 内部读写使用。
 *
 * <p>由 {@link ExcelColumnResolver} 根据 {@link ExcelColumn} 生成；业务代码无需直接依赖。
 *
 * @param field          对应 Java 字段（已 {@code setAccessible(true)}）
 * @param annotation     字段上的 {@link ExcelColumn} 原文
 * @param index          写出时的物理列下标（从 0 起，按 order 排序后分配）
 * @param dropdownValues 解析后的下拉选项；无下拉时为空列表
 */
record ExcelColumnMeta(
        Field field,
        ExcelColumn annotation,
        int index,
        List<String> dropdownValues
) {

    /**
     * 生成写入表头的显示文本。
     *
     * <p>必填列会加 {@code *} 前缀，例如 {@code *SKU}；导入匹配时会剥掉该前缀。
     *
     * @return 表头字符串
     */
    String headerName() {
        return annotation.required() ? "*" + annotation.name() : annotation.name();
    }
}

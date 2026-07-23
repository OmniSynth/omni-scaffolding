package com.omni.scaffolding.common.excel;

import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.exception.BusinessException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * {@link ExcelColumn} / {@link ExcelSheet} 元数据解析器。
 *
 * <p>包内使用：扫描类型及其父类字段，过滤静态字段，按 {@link ExcelColumn#order()} 排序后
 * 生成 {@link ExcelColumnMeta} 列表。
 */
final class ExcelColumnResolver {

    private ExcelColumnResolver() {
    }

    /**
     * 解析类型上所有 {@link ExcelColumn} 字段。
     *
     * @param type 导入/导出模型类型，不可为 {@code null}
     * @return 按列顺序排列的元数据列表，至少包含一列
     * @throws BusinessException 当类型上不存在任何 {@link ExcelColumn} 字段时
     */
    static List<ExcelColumnMeta> resolve(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = type;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                if (field.getAnnotation(ExcelColumn.class) != null) {
                    fields.add(field);
                }
            }
            current = current.getSuperclass();
        }

        if (fields.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST,
                    "类型 " + type.getName() + " 未找到 @ExcelColumn 字段");
        }

        fields.sort(Comparator
                .comparingInt((Field f) -> f.getAnnotation(ExcelColumn.class).order())
                .thenComparing(Field::getName));

        List<ExcelColumnMeta> metas = new ArrayList<>(fields.size());
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            field.setAccessible(true);
            ExcelColumn column = field.getAnnotation(ExcelColumn.class);
            metas.add(new ExcelColumnMeta(field, column, i, resolveDropdown(column)));
        }
        return metas;
    }

    /**
     * 解析导出 Sheet 名称。
     *
     * @param type 模型类型
     * @return {@link ExcelSheet#value()}；未标注时返回 {@code Sheet1}
     */
    static String sheetName(Class<?> type) {
        ExcelSheet sheet = type.getAnnotation(ExcelSheet.class);
        return sheet == null ? "Sheet1" : sheet.value();
    }

    /**
     * 解析下拉选项：优先 {@link ExcelColumn#dropdown()}，否则取枚举常量标签。
     *
     * @param column 列注解
     * @return 不可变选项列表；无下拉时为空列表
     */
    private static List<String> resolveDropdown(ExcelColumn column) {
        if (column.dropdown().length > 0) {
            return List.of(column.dropdown());
        }
        Class<? extends Enum<?>> enumType = column.dropdownEnum();
        if (enumType == ExcelColumn.None.class) {
            return List.of();
        }
        Enum<?>[] constants = enumType.getEnumConstants();
        if (constants == null || constants.length == 0) {
            return List.of();
        }
        return Arrays.stream(constants)
                .map(ExcelColumnResolver::enumLabel)
                .toList();
    }

    /**
     * 将枚举常量转为下拉展示文案。
     *
     * @param value 枚举常量
     * @return {@link ExcelDropdownLabel#getLabel()} 或 {@link Enum#name()}
     */
    private static String enumLabel(Enum<?> value) {
        if (value instanceof ExcelDropdownLabel labeled) {
            return labeled.getLabel();
        }
        return value.name();
    }
}

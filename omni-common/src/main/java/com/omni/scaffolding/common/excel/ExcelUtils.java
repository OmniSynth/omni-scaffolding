package com.omni.scaffolding.common.excel;

import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.exception.BusinessException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 基于 {@link ExcelColumn} / {@link ExcelSheet} 的 Excel 读写工具。
 *
 * <h2>能力</h2>
 * <ul>
 *   <li>{@link #writeTemplate}：仅表头模板，带下拉与批注，适合「下载导入模板」</li>
 *   <li>{@link #write}：导出数据行，同样带表头与下拉</li>
 *   <li>{@link #read}：按表头列名映射导入，校验必填</li>
 * </ul>
 *
 * <h2>使用注意</h2>
 * <ul>
 *   <li>模型类需具备公共无参构造（导入反射实例化）</li>
 *   <li>输出格式为 {@code .xlsx}（XSSF）</li>
 *   <li>失败时抛出 {@link BusinessException}，由全局异常处理转换为 API 响应</li>
 *   <li>本类无状态、线程安全；调用方负责流的关闭</li>
 * </ul>
 *
 * @see ExcelColumn
 * @see ExcelSheet
 */
public final class ExcelUtils {

    /**
     * 数据校验（下拉）作用的最大行号（含），从第 2 行（下标 1）起算。
     */
    private static final int DEFAULT_DROPDOWN_ROWS = 2000;

    private ExcelUtils() {
    }

    /**
     * 写出仅含表头的导入模板（含列宽、下拉、批注）。
     *
     * @param out  目标输出流，不会在方法内关闭
     * @param type 标注了 {@link ExcelColumn} 的模型类型
     * @throws BusinessException 类型无列定义或写出失败时
     */
    public static void writeTemplate(OutputStream out, Class<?> type) {
        write(out, type, List.of(), true);
    }

    /**
     * 导出数据到 Excel。
     *
     * <p>{@code rows} 为 {@code null} 或空时仍写出表头与下拉，效果接近模板。
     *
     * @param out  目标输出流，不会在方法内关闭
     * @param type 行数据类型
     * @param rows 数据列表，可为 {@code null}
     * @param <T>  行类型
     * @throws BusinessException 类型无列定义或写出失败时
     */
    public static <T> void write(OutputStream out, Class<T> type, List<T> rows) {
        write(out, type, rows == null ? List.of() : rows, true);
    }

    /**
     * 从 Excel 导入为对象列表。
     *
     * <p>约定：读取工作簿<strong>第一个</strong> Sheet；第一行为表头；按列名（去 {@code *}）匹配字段；
     * 完全空白行跳过。
     *
     * @param in   Excel 输入流（{@code .xlsx} / {@code .xls}），不会在方法内关闭外层业务流之外的生命周期由调用方管理；
     *             方法内会通过 {@link WorkbookFactory} 消费该流
     * @param type 目标类型，需有无参构造
     * @param <T>  行类型
     * @return 解析结果，可能为空列表，不会返回 {@code null}
     * @throws BusinessException 缺少必填列、必填单元格为空、类型转换失败或文件损坏时
     */
    public static <T> List<T> read(InputStream in, Class<T> type) {
        List<ExcelColumnMeta> columns = ExcelColumnResolver.resolve(type);
        try (Workbook workbook = WorkbookFactory.create(in)) {
            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            if (sheet == null) {
                return List.of();
            }
            Row header = sheet.getRow(0);
            if (header == null) {
                return List.of();
            }
            int[] mapping = mapHeaderToColumns(header, columns);
            List<T> result = new ArrayList<>();
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null || isBlankRow(row, mapping)) {
                    continue;
                }
                T instance = newInstance(type);
                for (int c = 0; c < columns.size(); c++) {
                    int cellIndex = mapping[c];
                    if (cellIndex < 0) {
                        continue;
                    }
                    ExcelColumnMeta meta = columns.get(c);
                    Object value = readCell(row.getCell(cellIndex), meta);
                    if (meta.annotation().required() && isEmptyValue(value)) {
                        throw new BusinessException(ErrorCode.BAD_REQUEST,
                                "第 " + (r + 1) + " 行「" + meta.annotation().name() + "」不能为空");
                    }
                    if (value != null) {
                        setField(meta.field(), instance, value);
                    }
                }
                result.add(instance);
            }
            return result;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Excel 读取失败: " + ex.getMessage());
        }
    }

    /**
     * 内部统一写出实现。
     *
     * @param out            输出流
     * @param type           模型类型
     * @param rows           数据行
     * @param withValidation 是否写入下拉数据校验
     */
    private static <T> void write(OutputStream out, Class<T> type, List<T> rows, boolean withValidation) {
        List<ExcelColumnMeta> columns = ExcelColumnResolver.resolve(type);
        String sheetName = ExcelColumnResolver.sheetName(type);
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet(sheetName);
            CellStyle headerStyle = headerStyle(workbook);
            Row header = sheet.createRow(0);
            for (ExcelColumnMeta meta : columns) {
                Cell cell = header.createCell(meta.index());
                cell.setCellValue(meta.headerName());
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(meta.index(), Math.max(meta.annotation().width(), 8) * 256);
                writeComment(sheet, meta);
            }
            if (withValidation) {
                applyDropdowns(sheet, columns);
            }
            for (int i = 0; i < rows.size(); i++) {
                Row row = sheet.createRow(i + 1);
                T data = rows.get(i);
                for (ExcelColumnMeta meta : columns) {
                    Object value = getField(meta.field(), data);
                    writeCell(row.createCell(meta.index()), value, meta);
                }
            }
            workbook.write(out);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Excel 写出失败: " + ex.getMessage());
        }
    }

    /**
     * 为配置了下拉的列添加 Excel 数据校验（显式列表）。
     *
     * @param sheet   目标 Sheet
     * @param columns 列元数据
     */
    private static void applyDropdowns(Sheet sheet, List<ExcelColumnMeta> columns) {
        DataValidationHelper helper = sheet.getDataValidationHelper();
        for (ExcelColumnMeta meta : columns) {
            List<String> options = meta.dropdownValues();
            if (options.isEmpty()) {
                continue;
            }
            // 显式列表总长度受限；选项较多时仍可用，极端场景可再扩展隐藏 Sheet 方案
            String[] values = options.toArray(String[]::new);
            DataValidationConstraint constraint = helper.createExplicitListConstraint(values);
            CellRangeAddressList range = new CellRangeAddressList(1, DEFAULT_DROPDOWN_ROWS, meta.index(), meta.index());
            DataValidation validation = helper.createValidation(constraint, range);
            validation.setSuppressDropDownArrow(true);
            validation.setShowErrorBox(true);
            validation.createErrorBox("输入无效", "请从下拉列表中选择「" + meta.annotation().name() + "」");
            sheet.addValidationData(validation);
        }
    }

    /**
     * 写入表头批注：优先注解 {@link ExcelColumn#comment()}，否则按下拉/必填自动生成。
     *
     * @param sheet 目标 Sheet
     * @param meta  列元数据
     */
    private static void writeComment(XSSFSheet sheet, ExcelColumnMeta meta) {
        String text = meta.annotation().comment();
        if (text == null || text.isBlank()) {
            if (!meta.dropdownValues().isEmpty()) {
                text = "可选: " + String.join(" / ", meta.dropdownValues());
            } else if (meta.annotation().required()) {
                text = "必填";
            } else {
                return;
            }
        }
        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = new XSSFClientAnchor(0, 0, 0, 0,
                meta.index(), 0, meta.index() + 2, 3);
        var comment = drawing.createCellComment(anchor);
        comment.setString(new XSSFRichTextString(text));
        comment.setAuthor("omni");
        sheet.getRow(0).getCell(meta.index()).setCellComment(comment);
    }

    /**
     * 创建表头单元格样式（加粗、居中、浅灰底）。
     *
     * @param workbook 工作簿
     * @return 可复用的表头样式
     */
    private static CellStyle headerStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    /**
     * 将表头行映射为「列元数据下标 → Excel 物理列下标」。
     *
     * @param header  第一行表头
     * @param columns 模型列定义
     * @return 与 {@code columns} 等长的数组；未匹配到的列为 {@code -1}
     * @throws BusinessException 必填列在表头中不存在时
     */
    private static int[] mapHeaderToColumns(Row header, List<ExcelColumnMeta> columns) {
        int[] mapping = new int[columns.size()];
        for (int i = 0; i < mapping.length; i++) {
            mapping[i] = -1;
        }
        short last = header.getLastCellNum();
        for (int cellIndex = 0; cellIndex < last; cellIndex++) {
            Cell cell = header.getCell(cellIndex);
            String headerText = normalizeHeader(asString(cell));
            if (headerText.isEmpty()) {
                continue;
            }
            for (int c = 0; c < columns.size(); c++) {
                String expected = normalizeHeader(columns.get(c).annotation().name());
                if (expected.equals(headerText) && mapping[c] < 0) {
                    mapping[c] = cellIndex;
                    break;
                }
            }
        }
        for (int c = 0; c < columns.size(); c++) {
            if (mapping[c] < 0 && columns.get(c).annotation().required()) {
                throw new BusinessException(ErrorCode.BAD_REQUEST,
                        "缺少必填列: " + columns.get(c).annotation().name());
            }
        }
        return mapping;
    }

    /**
     * 规范化表头文本：去空白、去掉必填前缀 {@code *}。
     *
     * @param raw 原始表头
     * @return 规范化后的列名；{@code null} 视为空串
     */
    private static String normalizeHeader(String raw) {
        if (raw == null) {
            return "";
        }
        String text = raw.trim();
        if (text.startsWith("*")) {
            text = text.substring(1).trim();
        }
        return text;
    }

    /**
     * 判断映射列是否全部为空（用于跳过尾部空行）。
     *
     * @param row     数据行
     * @param mapping 列映射
     * @return 全部为空则为 {@code true}
     */
    private static boolean isBlankRow(Row row, int[] mapping) {
        for (int cellIndex : mapping) {
            if (cellIndex < 0) {
                continue;
            }
            String text = asString(row.getCell(cellIndex));
            if (text != null && !text.isBlank()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 读取单元格并转换为字段类型。
     *
     * @param cell 单元格，可为 {@code null}
     * @param meta 列元数据
     * @return 转换后的值；空单元格返回 {@code null}
     * @throws BusinessException 类型无法转换时
     */
    private static Object readCell(Cell cell, ExcelColumnMeta meta) {
        if (cell == null) {
            return null;
        }
        Class<?> fieldType = meta.field().getType();
        String text = asString(cell);
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            if (fieldType == String.class) {
                return text;
            }
            if (fieldType == Integer.class || fieldType == int.class) {
                return (int) Double.parseDouble(text);
            }
            if (fieldType == Long.class || fieldType == long.class) {
                return (long) Double.parseDouble(text);
            }
            if (fieldType == Double.class || fieldType == double.class) {
                return Double.parseDouble(text);
            }
            if (fieldType == BigDecimal.class) {
                return new BigDecimal(text);
            }
            if (fieldType == Boolean.class || fieldType == boolean.class) {
                return "true".equalsIgnoreCase(text) || "是".equals(text) || "1".equals(text);
            }
            if (fieldType == LocalDate.class) {
                return LocalDate.parse(text, formatter(meta));
            }
            if (fieldType == LocalDateTime.class) {
                return LocalDateTime.parse(text, formatter(meta));
            }
            if (fieldType == Instant.class) {
                return LocalDateTime.parse(text, formatter(meta)).atZone(ZoneId.systemDefault()).toInstant();
            }
            if (fieldType == Date.class) {
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                }
                LocalDateTime ldt = LocalDateTime.parse(text, formatter(meta));
                return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
            }
            if (fieldType.isEnum()) {
                return parseEnum(fieldType, text);
            }
            return text;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST,
                    "列「" + meta.annotation().name() + "」值无法转换: " + text);
        }
    }

    /**
     * 将单元格文本解析为枚举（支持 name 与 {@link ExcelDropdownLabel#getLabel()}）。
     *
     * @param enumType 枚举 Class
     * @param text     单元格文本
     * @return 枚举常量
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object parseEnum(Class<?> enumType, String text) {
        Object[] constants = enumType.getEnumConstants();
        for (Object constant : constants) {
            Enum<?> e = (Enum<?>) constant;
            if (e.name().equalsIgnoreCase(text)) {
                return e;
            }
            if (e instanceof ExcelDropdownLabel labeled && labeled.getLabel().equals(text)) {
                return e;
            }
        }
        throw new BusinessException(ErrorCode.BAD_REQUEST,
                "枚举值无效: " + text + "，类型=" + enumType.getSimpleName());
    }

    /**
     * 按字段运行时类型写入单元格。
     *
     * @param cell  目标单元格
     * @param value 字段值，可为 {@code null}
     * @param meta  列元数据（提供日期格式等）
     */
    private static void writeCell(Cell cell, Object value, ExcelColumnMeta meta) {
        if (value == null) {
            cell.setBlank();
            return;
        }
        if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
            return;
        }
        if (value instanceof Boolean bool) {
            cell.setCellValue(bool);
            return;
        }
        if (value instanceof Date date) {
            cell.setCellValue(DateTimeFormatter.ofPattern(meta.annotation().dateFormat())
                    .withZone(ZoneId.systemDefault())
                    .format(date.toInstant()));
            return;
        }
        if (value instanceof LocalDate localDate) {
            cell.setCellValue(localDate.format(DateTimeFormatter.ofPattern(trimDatePattern(meta))));
            return;
        }
        if (value instanceof LocalDateTime localDateTime) {
            cell.setCellValue(localDateTime.format(formatter(meta)));
            return;
        }
        if (value instanceof Instant instant) {
            cell.setCellValue(DateTimeFormatter.ofPattern(meta.annotation().dateFormat())
                    .withZone(ZoneId.systemDefault())
                    .format(instant));
            return;
        }
        if (value instanceof ExcelDropdownLabel labeled) {
            cell.setCellValue(labeled.getLabel());
            return;
        }
        if (value instanceof Enum<?> e) {
            cell.setCellValue(e.name());
            return;
        }
        cell.setCellValue(String.valueOf(value));
    }

    /**
     * 按列注解构建日期时间格式化器。
     *
     * @param meta 列元数据
     * @return formatter
     */
    private static DateTimeFormatter formatter(ExcelColumnMeta meta) {
        return DateTimeFormatter.ofPattern(meta.annotation().dateFormat());
    }

    /**
     * 为 {@link LocalDate} 截取日期段 pattern（去掉空格后的时间部分）。
     *
     * @param meta 列元数据
     * @return 仅日期的 pattern
     */
    private static String trimDatePattern(ExcelColumnMeta meta) {
        String pattern = meta.annotation().dateFormat();
        return pattern.contains(" ") ? pattern.substring(0, pattern.indexOf(' ')) : pattern;
    }

    /**
     * 将单元格统一转为修剪后的字符串，便于后续类型转换。
     *
     * @param cell 单元格
     * @return 文本；空白单元格返回 {@code null}
     */
    private static String asString(Cell cell) {
        if (cell == null) {
            return null;
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue() == null ? null : cell.getStringCellValue().trim();
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    Date date = cell.getDateCellValue();
                    yield DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                            .withZone(ZoneId.systemDefault())
                            .format(date.toInstant());
                }
                double number = cell.getNumericCellValue();
                if (Math.floor(number) == number) {
                    yield String.valueOf((long) number);
                }
                yield BigDecimal.valueOf(number).stripTrailingZeros().toPlainString();
            }
            case FORMULA -> cell.getCellFormula();
            case BLANK -> null;
            default -> null;
        };
    }

    /**
     * 判断导入值是否视为「空」（用于必填校验）。
     *
     * @param value 转换后的值
     * @return {@code null} 或空白字符串为 {@code true}
     */
    private static boolean isEmptyValue(Object value) {
        if (value == null) {
            return true;
        }
        return value instanceof String s && s.isBlank();
    }

    /**
     * 通过无参构造实例化导入对象。
     *
     * @param type 目标类型
     * @param <T>  类型参数
     * @return 新实例
     */
    private static <T> T newInstance(Class<T> type) {
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR,
                    "无法实例化 " + type.getName() + "，请提供无参构造");
        }
    }

    /**
     * 反射读取字段值。
     *
     * @param field  字段
     * @param target 对象实例
     * @return 字段值
     */
    private static Object getField(Field field, Object target) {
        try {
            return field.get(target);
        } catch (IllegalAccessException ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "读取字段失败: " + field.getName());
        }
    }

    /**
     * 反射写入字段值。
     *
     * @param field  字段
     * @param target 对象实例
     * @param value  新值
     */
    private static void setField(Field field, Object target, Object value) {
        try {
            field.set(target, value);
        } catch (IllegalAccessException ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "写入字段失败: " + field.getName());
        }
    }
}

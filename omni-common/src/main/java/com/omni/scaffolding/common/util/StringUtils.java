package com.omni.scaffolding.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 字符串工具：继承 Apache Commons {@link org.apache.commons.lang3.StringUtils}，
 * 并补充业务侧常用扩展。
 *
 * <p>可直接通过本类调用父类静态方法，例如 {@code StringUtils.isBlank(s)}。
 * 与 {@code org.springframework.util.StringUtils} 不同，请按需选择 import。
 */
public class StringUtils extends org.apache.commons.lang3.StringUtils {

    private static final Pattern CAMEL_BOUNDARY = Pattern.compile("([a-z])([A-Z]+)");

    /**
     * 供子类或工具框架实例化；日常请使用静态方法。
     */
    public StringUtils() {
    }

    /**
     * 空值回落：{@code value == null} 时返回 {@code defaultValue}。
     *
     * @param value        原值
     * @param defaultValue 默认值
     * @param <T>          类型
     * @return 非 null 的结果
     */
    public static <T> T nvl(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    /**
     * blank 时回落默认值（等价于 {@link #defaultIfBlank(CharSequence, String)}，语义更直观）。
     *
     * @param value        原字符串
     * @param defaultValue 默认值
     * @return 非 blank 字符串，或默认值
     */
    public static String blankToDefault(String value, String defaultValue) {
        return defaultIfBlank(value, defaultValue);
    }

    /**
     * 格式化字符串，占位符为 {@code {}}，按顺序替换。
     *
     * <pre>{@code
     * format("hello {}, age {}", "omni", 1) → "hello omni, age 1"
     * }</pre>
     *
     * @param template 模板，可含 {@code {}}
     * @param params   参数
     * @return 格式化结果；模板为空时返回空串
     */
    public static String format(String template, Object... params) {
        if (isEmpty(template)) {
            return EMPTY;
        }
        if (params == null || params.length == 0) {
            return template;
        }
        StringBuilder sb = new StringBuilder(template.length() + 16);
        int cursor = 0;
        int argIndex = 0;
        while (argIndex < params.length) {
            int idx = template.indexOf("{}", cursor);
            if (idx < 0) {
                break;
            }
            sb.append(template, cursor, idx).append(params[argIndex] == null ? "null" : params[argIndex]);
            cursor = idx + 2;
            argIndex++;
        }
        sb.append(template, cursor, template.length());
        return sb.toString();
    }

    /**
     * 驼峰转下划线：{@code userName} → {@code user_name}。
     *
     * @param camel 驼峰字符串
     * @return 下划线形式；空白原样返回
     */
    public static String toUnderScoreCase(String camel) {
        if (isBlank(camel)) {
            return camel;
        }
        return CAMEL_BOUNDARY.matcher(camel).replaceAll("$1_$2").toLowerCase();
    }

    /**
     * 下划线转驼峰：{@code user_name} → {@code userName}。
     *
     * @param underscore 下划线字符串
     * @return 驼峰形式；空白原样返回
     */
    public static String toCamelCase(String underscore) {
        if (isBlank(underscore)) {
            return underscore;
        }
        StringBuilder sb = new StringBuilder(underscore.length());
        boolean upperNext = false;
        for (int i = 0; i < underscore.length(); i++) {
            char c = underscore.charAt(i);
            if (c == '_') {
                upperNext = true;
                continue;
            }
            if (upperNext) {
                sb.append(Character.toUpperCase(c));
                upperNext = false;
            } else {
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }

    /**
     * 判断是否等于任一候选（忽略大小写）。
     *
     * @param str  待比较，可为 null
     * @param strs 候选
     * @return 命中任一则 {@code true}
     */
    public static boolean equalsAnyIgnoreCase(String str, String... strs) {
        if (strs == null) {
            return false;
        }
        for (String candidate : strs) {
            if (equalsIgnoreCase(str, candidate)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否以 {@code http://} 或 {@code https://} 开头（忽略大小写）。
     *
     * @param link 链接
     * @return 是 http(s) 链接时为 {@code true}
     */
    public static boolean isHttpUrl(String link) {
        return startsWithIgnoreCase(link, "http://") || startsWithIgnoreCase(link, "https://");
    }

    /**
     * 按分隔符拆分为列表，自动 trim，并丢弃 blank 段。
     *
     * @param str       原文
     * @param separator 分隔符，默认 {@code ,}
     * @return 非 blank 片段列表，永不返回 null
     */
    public static List<String> splitTrim(String str, String separator) {
        List<String> result = new ArrayList<>();
        if (isBlank(str)) {
            return result;
        }
        String sep = defaultIfEmpty(separator, ",");
        for (String part : split(str, sep)) {
            if (isNotBlank(part)) {
                result.add(part.trim());
            }
        }
        return result;
    }

    /**
     * 集合是否为空（null 或 size=0）。
     *
     * @param collection 集合
     * @return 为空时 {@code true}
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * 集合是否非空。
     *
     * @param collection 集合
     * @return 非空时 {@code true}
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    /**
     * 截断并追加省略号；长度未超限时原样返回。
     *
     * @param str       原文
     * @param maxLength 最大长度（含省略号），须 ≥ 1
     * @return 截断后的字符串
     */
    public static String ellipsis(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        if (maxLength < 1) {
            throw new IllegalArgumentException("maxLength must be >= 1");
        }
        if (str.length() <= maxLength) {
            return str;
        }
        if (maxLength <= 3) {
            return str.substring(0, maxLength);
        }
        return str.substring(0, maxLength - 3) + "...";
    }
}

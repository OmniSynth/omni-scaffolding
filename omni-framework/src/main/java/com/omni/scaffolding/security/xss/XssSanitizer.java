package com.omni.scaffolding.security.xss;

import org.springframework.web.util.HtmlUtils;

import java.util.regex.Pattern;

/**
 * XSS 字符串清洗工具。
 *
 * <p>{@link XssMode#STRIP}：移除脚本相关片段；{@link XssMode#ESCAPE}：HTML 转义。
 * 对 {@code null} 原样返回。
 */
public final class XssSanitizer {

    private static final Pattern[] STRIP_PATTERNS = {
            // script / style / iframe 等危险标签（含内容）
            Pattern.compile("(?i)<\\s*(script|style|iframe|object|embed|link|meta|svg|form)[^>]*>.*?</\\s*\\1\\s*>",
                    Pattern.DOTALL),
            // 自闭合危险标签
            Pattern.compile("(?i)<\\s*(script|style|iframe|object|embed|link|meta|svg|form)[^>]*/?>"),
            // onxxx= 事件属性
            Pattern.compile("(?i)\\s+on[a-z]+\\s*=\\s*(\"[^\"]*\"|'[^']*'|[^\\s>]+)"),
            // javascript: / vbscript: / data:text/html
            Pattern.compile("(?i)(javascript|vbscript)\\s*:"),
            Pattern.compile("(?i)data\\s*:\\s*text\\s*/\\s*html"),
            // expression( ) IE CSS 表达式
            Pattern.compile("(?i)expression\\s*\\(")
    };

    private XssSanitizer() {
    }

    /**
     * 按策略清洗字符串。
     *
     * @param input 原始输入
     * @param mode  策略
     * @return 清洗后的字符串；{@code input == null} 时返回 {@code null}
     */
    public static String clean(String input, XssMode mode) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        // 去掉 NUL，避免部分解析器截断绕过
        String value = input.replace("\0", "");
        return switch (mode == null ? XssMode.STRIP : mode) {
            case ESCAPE -> HtmlUtils.htmlEscape(value);
            case STRIP -> strip(value);
        };
    }

    /**
     * 按策略移除脚本等危险片段（{@link XssMode#STRIP} 内部实现）。
     *
     * @param value 已去除 NUL 的输入
     * @return 清洗后的字符串
     */
    private static String strip(String value) {
        String result = value;
        for (Pattern pattern : STRIP_PATTERNS) {
            result = pattern.matcher(result).replaceAll("");
        }
        // 残余尖括号标签再剥一层（如 <img src=x onerror=...> 拆分后残留）
        result = result.replaceAll("(?i)<\\s*img[^>]*>", "");
        return result;
    }
}

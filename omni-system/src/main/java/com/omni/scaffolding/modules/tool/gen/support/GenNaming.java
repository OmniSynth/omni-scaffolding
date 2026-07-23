package com.omni.scaffolding.modules.tool.gen.support;

import org.springframework.util.StringUtils;

/**
 * 代码生成命名工具。
 */
public final class GenNaming {

    private GenNaming() {
    }

    /**
     * 下划线转驼峰。
     *
     * @param name       原始名
     * @param capitalize 首字母是否大写
     * @return 驼峰名
     */
    public static String toCamel(String name, boolean capitalize) {
        if (!StringUtils.hasText(name)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        boolean upper = capitalize;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == '_' || c == '-' || c == ' ') {
                upper = true;
                continue;
            }
            if (upper) {
                sb.append(Character.toUpperCase(c));
                upper = false;
            } else {
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }

    /**
     * 表名推导业务名：去掉常见前缀后转小驼峰文件名风格（全小写连字符不用，用小写驼峰段）。
     *
     * @param tableName 表名
     * @return 如 notice / order
     */
    public static String toBusinessName(String tableName) {
        String t = stripPrefix(tableName);
        String camel = toCamel(t, false);
        return camel.isEmpty() ? "biz" : camel;
    }

    /**
     * 表名推导实体类名。
     *
     * @param tableName 表名
     * @return 如 SysNotice
     */
    public static String toClassName(String tableName) {
        return toCamel(tableName, true);
    }

    private static String stripPrefix(String tableName) {
        String t = tableName == null ? "" : tableName.trim().toLowerCase();
        for (String p : new String[]{"sys_", "biz_", "t_", "tb_"}) {
            if (t.startsWith(p)) {
                return t.substring(p.length());
            }
        }
        return t;
    }
}

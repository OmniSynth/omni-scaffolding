package com.omni.scaffolding.modules.tool.gen.support;

import java.util.Locale;
import java.util.Set;

/**
 * MySQL 列类型 → Java 类型 / 默认查询类型。
 */
public final class GenTypeMapper {

    private static final Set<String> AUDIT = Set.of("created_at", "updated_at", "version");

    private GenTypeMapper() {
    }

    public static boolean isAudit(String columnName) {
        return columnName != null && AUDIT.contains(columnName.toLowerCase(Locale.ROOT));
    }

    public static boolean isLogicDelete(String columnName) {
        return "deleted".equalsIgnoreCase(columnName);
    }

    public static boolean isDateTime(String columnType) {
        String t = baseType(columnType);
        return t.contains("date") || t.contains("time") || t.equals("timestamp");
    }

    /**
     * @param columnType 如 varchar(64)、tinyint(1)、bigint
     * @return Java 简单类型名
     */
    public static String toJavaType(String columnType) {
        String t = baseType(columnType);
        String full = columnType == null ? "" : columnType.toLowerCase(Locale.ROOT).trim();
        if (full.startsWith("tinyint(1)")) {
            return "Boolean";
        }
        return switch (t) {
            case "bigint" -> "Long";
            case "int", "integer", "mediumint", "smallint", "tinyint" -> "Integer";
            case "decimal", "numeric", "double", "float" -> "BigDecimal";
            case "bit" -> "Boolean";
            case "datetime", "timestamp", "date", "time" -> "Instant";
            case "json", "text", "mediumtext", "longtext", "tinytext",
                    "varchar", "char", "blob", "longblob", "mediumblob" -> "String";
            default -> "String";
        };
    }

    /**
     * 默认查询类型。
     */
    public static String defaultQueryType(String columnName, String columnType, boolean pk) {
        if (pk || isAudit(columnName) || isLogicDelete(columnName)) {
            return "NONE";
        }
        if (isDateTime(columnType)) {
            return "BETWEEN";
        }
        String java = toJavaType(columnType);
        if ("String".equals(java)) {
            return "LIKE";
        }
        if ("Boolean".equals(java) || "Integer".equals(java) || "Long".equals(java)) {
            return "EQ";
        }
        return "NONE";
    }

    private static String baseType(String columnType) {
        if (columnType == null || columnType.isBlank()) {
            return "varchar";
        }
        String t = columnType.toLowerCase(Locale.ROOT).trim();
        int p = t.indexOf('(');
        if (p > 0) {
            t = t.substring(0, p);
        }
        return t.trim();
    }
}

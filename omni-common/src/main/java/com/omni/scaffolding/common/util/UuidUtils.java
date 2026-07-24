package com.omni.scaffolding.common.util;

import java.util.UUID;

/**
 * 基于 UUID 的 ID 生成工具。
 *
 * <p>数据库业务主键请继续使用 {@link IdGenerator#nextId()}（long，兼容前端 JS 精度）；
 * 本类适合文件名、验证码 ID、trace、令牌等字符串标识场景。
 */
public final class UuidUtils {

    private UuidUtils() {
    }

    /**
     * 标准 UUID 字符串（含连字符），例如 {@code 550e8400-e29b-41d4-a716-446655440000}。
     *
     * @return 36 位 UUID
     */
    public static String randomUuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * 无连字符的 UUID（32 位小写十六进制），便于作文件名 / 缓存 key。
     *
     * @return 32 位 UUID
     */
    public static String simpleUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成带前缀的简单 UUID，例如 {@code file_a1b2...}。
     *
     * @param prefix 前缀，空白时等同于 {@link #simpleUuid()}
     * @return 前缀 + 32 位 UUID
     */
    public static String simpleUuid(String prefix) {
        String id = simpleUuid();
        if (prefix == null || prefix.isBlank()) {
            return id;
        }
        return prefix + id;
    }
}

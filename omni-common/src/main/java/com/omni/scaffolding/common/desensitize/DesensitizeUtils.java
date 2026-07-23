package com.omni.scaffolding.common.desensitize;

/**
 * 数据脱敏算法工具。
 *
 * <p>空串原样返回；非字符串场景由调用方先转为字符串。
 */
public final class DesensitizeUtils {

    private DesensitizeUtils() {
    }

    /**
     * 按类型脱敏，掩码字符默认 {@code *}。
     *
     * @param value 原始字符串
     * @param type  脱敏策略
     * @return 脱敏结果；空串原样返回
     */
    public static String desensitize(String value, DesensitizeType type) {
        return desensitize(value, type, 0, 0, '*');
    }

    /**
     * 按类型与保留位数脱敏。
     *
     * @param value      原始字符串
     * @param type       脱敏策略
     * @param prefixKeep {@link DesensitizeType#CUSTOM} 时保留左侧字符数
     * @param suffixKeep {@link DesensitizeType#CUSTOM} 时保留右侧字符数
     * @param maskChar   掩码字符
     * @return 脱敏结果；空串原样返回
     */
    public static String desensitize(String value,
                                     DesensitizeType type,
                                     int prefixKeep,
                                     int suffixKeep,
                                     char maskChar) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return switch (type) {
            case NAME -> maskName(value, maskChar);
            case MOBILE -> maskMobile(value, maskChar);
            case ID_CARD -> maskIdCard(value, maskChar);
            case EMAIL -> maskEmail(value, maskChar);
            case BANK_CARD -> maskBankCard(value, maskChar);
            case ADDRESS -> maskAddress(value, maskChar);
            case CUSTOM -> maskKeep(value, prefixKeep, suffixKeep, maskChar);
        };
    }

    /**
     * 姓名：1 字 {@code *}；2 字保留首；3+ 保留首尾。
     *
     * @param name     姓名
     * @param maskChar 掩码字符
     * @return 脱敏结果
     */
    public static String maskName(String name, char maskChar) {
        int len = name.length();
        if (len == 1) {
            return String.valueOf(maskChar);
        }
        if (len == 2) {
            return name.charAt(0) + String.valueOf(maskChar);
        }
        return name.charAt(0) + repeat(maskChar, len - 2) + name.charAt(len - 1);
    }

    /**
     * 手机号：保留前 3 后 4。
     *
     * @param mobile   手机号
     * @param maskChar 掩码字符
     * @return 脱敏结果
     */
    public static String maskMobile(String mobile, char maskChar) {
        String digits = mobile.trim();
        if (digits.length() < 7) {
            return maskKeep(digits, 0, 0, maskChar);
        }
        return maskKeep(digits, 3, 4, maskChar);
    }

    /**
     * 身份证：保留前 4 后 4。
     *
     * @param idCard   身份证号
     * @param maskChar 掩码字符
     * @return 脱敏结果
     */
    public static String maskIdCard(String idCard, char maskChar) {
        String v = idCard.trim();
        if (v.length() < 8) {
            return maskKeep(v, 0, 0, maskChar);
        }
        return maskKeep(v, 4, 4, maskChar);
    }

    /**
     * 邮箱：本地部分保留首字符。
     *
     * @param email    邮箱地址
     * @param maskChar 掩码字符
     * @return 脱敏结果
     */
    public static String maskEmail(String email, char maskChar) {
        String v = email.trim();
        int at = v.indexOf('@');
        if (at <= 0) {
            return maskKeep(v, 1, 0, maskChar);
        }
        String local = v.substring(0, at);
        String domain = v.substring(at);
        if (local.length() == 1) {
            return maskChar + domain;
        }
        return local.charAt(0) + repeat(maskChar, Math.max(local.length() - 1, 1)) + domain;
    }

    /**
     * 银行卡：仅保留后 4 位。
     *
     * @param card     银行卡号
     * @param maskChar 掩码字符
     * @return 脱敏结果
     */
    public static String maskBankCard(String card, char maskChar) {
        String v = card.replaceAll("\\s+", "");
        if (v.length() <= 4) {
            return repeat(maskChar, v.length());
        }
        return maskKeep(v, 0, 4, maskChar);
    }

    /**
     * 地址：保留前 6 字符。
     *
     * @param address  地址文本
     * @param maskChar 掩码字符
     * @return 脱敏结果
     */
    public static String maskAddress(String address, char maskChar) {
        String v = address.trim();
        if (v.length() <= 6) {
            return maskKeep(v, Math.max(v.length() / 2, 1), 0, maskChar);
        }
        return maskKeep(v, 6, 0, maskChar);
    }

    /**
     * 保留左右两端，中间用掩码填充。
     *
     * <p>当保留位数之和大于等于长度时，全部掩码，避免泄露原文。
     *
     * @param value      原始字符串
     * @param prefixKeep 保留左侧字符数
     * @param suffixKeep 保留右侧字符数
     * @param maskChar   掩码字符
     * @return 脱敏结果
     */
    public static String maskKeep(String value, int prefixKeep, int suffixKeep, char maskChar) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        int len = value.length();
        int left = Math.max(prefixKeep, 0);
        int right = Math.max(suffixKeep, 0);
        if (left + right >= len) {
            return repeat(maskChar, len);
        }
        return value.substring(0, left) + repeat(maskChar, len - left - right) + value.substring(len - right);
    }

    /**
     * 重复指定字符生成固定长度字符串。
     *
     * @param ch    掩码字符
     * @param count 重复次数
     * @return 重复结果；{@code count <= 0} 时返回空串
     */
    private static String repeat(char ch, int count) {
        if (count <= 0) {
            return "";
        }
        return String.valueOf(ch).repeat(count);
    }
}

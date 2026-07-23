package com.omni.scaffolding.common.desensitize;

/**
 * 内置脱敏策略。
 *
 * <p>{@link #CUSTOM} 时使用注解上的 {@code prefixKeep}/{@code suffixKeep} 控制保留位数。
 */
public enum DesensitizeType {

    /**
     * 中文姓名：张* / 张*三 / 欧**明
     */
    NAME,

    /**
     * 手机号：保留前 3 后 4，如 138****8000
     */
    MOBILE,

    /**
     * 身份证：保留前 4 后 4
     */
    ID_CARD,

    /**
     * 邮箱：保留 @ 前首字符与域名，如 a***@example.com
     */
    EMAIL,

    /**
     * 银行卡：仅保留后 4 位
     */
    BANK_CARD,

    /**
     * 地址：保留前 6 个字符，其余掩码
     */
    ADDRESS,

    /**
     * 自定义：按 prefixKeep / suffixKeep 保留两端
     */
    CUSTOM
}

package com.omni.scaffolding.security.xss;

/**
 * XSS 防护策略。
 */
public enum XssMode {

    /**
     * 剔除危险标签 / 事件 / 伪协议，尽量保留正常业务字符（适合 JSON API）。
     */
    STRIP,

    /**
     * HTML 实体转义（{@code < > " ' &}），更严格，可能改变含特殊字符的合法输入。
     */
    ESCAPE
}

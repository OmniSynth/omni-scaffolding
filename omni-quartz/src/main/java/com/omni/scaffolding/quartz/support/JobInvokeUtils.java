package com.omni.scaffolding.quartz.support;

import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * Quartz 调用目标格式校验工具。
 *
 * <p>实际方法发现与调用由 {@link QuartzJobRegistry} 基于 {@link QuartzInvokable}
 * 白名单完成，本类不再访问 Spring 容器或执行任意反射。
 */
public final class JobInvokeUtils {

    private static final Pattern TARGET = Pattern.compile("^[A-Za-z0-9_]+\\.[A-Za-z0-9_]+$");

    private JobInvokeUtils() {
    }

    /**
     * 校验调用目标格式是否为 {@code beanName.methodName}。
     *
     * @param invokeTarget 调用目标
     * @throws IllegalArgumentException 格式非法
     */
    public static void validateTarget(String invokeTarget) {
        if (!StringUtils.hasText(invokeTarget) || !TARGET.matcher(invokeTarget.trim()).matches()) {
            throw new IllegalArgumentException("调用目标格式非法，应为 beanName.methodName");
        }
    }
}

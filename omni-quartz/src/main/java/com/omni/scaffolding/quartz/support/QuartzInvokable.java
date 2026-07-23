package com.omni.scaffolding.quartz.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记允许由 Quartz 管理台调用的 Spring Bean 方法。
 *
 * <p>方法必须为 {@code public}，且签名只能是无参或单个 {@link String} 参数。
 * 未标记的方法即使属于 Spring Bean，也不会进入 Quartz 调用白名单。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface QuartzInvokable {
}

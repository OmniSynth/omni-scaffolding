package com.omni.scaffolding.common.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * IP 白名单：标注在 Controller 类或方法上，仅白名单内 IP 可访问。
 *
 * <p>生效集合 = 注解 {@link #value()} ∪ 表 {@code sys_ip_whitelist} 中启用记录；
 * 若表中无启用 IP，则回退到 yaml {@code omni.security.ip-whitelist}。
 * 放行后由切面累加该 IP 今日访问次数（Redis）。
 *
 * <p>方法级注解优先于类级注解。
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface IpWhitelist {

    /**
     * 额外允许的 IP，与表 / 兜底配置合并后生效。
     *
     * @return IP 数组，可为多个
     */
    String[] value() default {};
}

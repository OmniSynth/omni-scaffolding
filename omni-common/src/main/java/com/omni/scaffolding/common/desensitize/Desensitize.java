package com.omni.scaffolding.common.desensitize;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字段级数据脱敏，作用于 Jackson JSON 序列化输出。
 *
 * <p>标注在响应 DTO / VO 字段上；入库与内存中仍为明文。请求体反序列化不受影响。
 *
 * <h2>示例</h2>
 * <pre>{@code
 * @Desensitize(type = DesensitizeType.MOBILE)
 * private String mobile;
 *
 * @Desensitize(type = DesensitizeType.CUSTOM, prefixKeep = 2, suffixKeep = 2)
 * private String secretCode;
 * }</pre>
 *
 * <p>需要返回明文时，在 Controller 方法上标注 {@link WithoutDesensitize}。
 *
 * @see DesensitizeType
 * @see com.omni.scaffolding.common.util.DesensitizeUtils
 * @see WithoutDesensitize
 */
@Documented
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = DesensitizeSerializer.class)
public @interface Desensitize {

    /**
     * 脱敏策略。
     */
    DesensitizeType type() default DesensitizeType.CUSTOM;

    /**
     * {@link DesensitizeType#CUSTOM} 时保留左侧字符数。
     */
    int prefixKeep() default 0;

    /**
     * {@link DesensitizeType#CUSTOM} 时保留右侧字符数。
     */
    int suffixKeep() default 0;

    /**
     * 掩码字符，默认 {@code *}；仅取第一个字符。
     */
    String maskChar() default "*";
}

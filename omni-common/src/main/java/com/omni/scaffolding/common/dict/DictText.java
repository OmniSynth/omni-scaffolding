package com.omni.scaffolding.common.dict;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 将字段值按数据字典翻译为同级 {@code 字段名Text} JSON 属性。
 *
 * <p>例如 {@code @DictText("sys_normal_disable")} 标注在 {@code status} 上，
 * 序列化时会保留原始 {@code status}，并追加只读属性 {@code statusText}。
 */
@Documented
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DictText {

    /**
     * 字典类型编码。
     *
     * @return 字典类型编码
     */
    String value();
}

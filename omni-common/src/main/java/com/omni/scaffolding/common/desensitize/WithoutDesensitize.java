package com.omni.scaffolding.common.desensitize;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注在 Controller 方法上时，本次响应跳过字段脱敏（返回明文）。
 *
 * <p>典型场景：后台编辑详情需回填真实手机号/姓名，而列表接口仍脱敏展示。
 *
 * @see Desensitize
 * @see DesensitizeContext
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface WithoutDesensitize {
}

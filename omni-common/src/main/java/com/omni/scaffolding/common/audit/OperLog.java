package com.omni.scaffolding.common.audit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记需要记录操作日志的 Controller 方法。
 *
 * <p>由框架侧切面采集请求信息并异步/独立事务落库。
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperLog {

    /**
     * 业务模块，如「用户管理」。
     */
    String module();

    /**
     * 操作动作，如「新增」「删除」。
     */
    String action();

    /**
     * 是否记录请求参数摘要，默认 true。
     */
    boolean saveParams() default true;
}

/**
 * 基础设施与框架装配配置（位于 {@code omni-framework} 模块）。
 *
 * <p>包含虚拟线程执行器、JPA / MyBatis、Redis、Security 属性、OpenAPI 等。
 * 业务逻辑请勿放在此包，保持“只做装配、不做业务”；本模块禁止依赖 system/demo。
 */
package com.omni.scaffolding.config;

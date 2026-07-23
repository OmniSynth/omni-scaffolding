/**
 * Quartz JDBC 集群调度模块。
 *
 * <p>多实例共享 MySQL {@code QRTZ_*} 表，同一 Trigger 同一时刻仅一台节点触发。
 * 通过 {@code omni.quartz.enabled} 开关控制；关闭时排除 {@code QuartzAutoConfiguration}。
 */
package com.omni.scaffolding.quartz;

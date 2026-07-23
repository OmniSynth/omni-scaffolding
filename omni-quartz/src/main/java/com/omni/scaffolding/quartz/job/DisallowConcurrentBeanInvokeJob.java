package com.omni.scaffolding.quartz.job;

import org.quartz.DisallowConcurrentExecution;

/**
 * 禁止并发的 Bean 方法调用 Job。
 */
@DisallowConcurrentExecution
public class DisallowConcurrentBeanInvokeJob extends BeanInvokeJob {
}

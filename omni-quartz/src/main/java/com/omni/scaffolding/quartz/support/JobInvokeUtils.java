package com.omni.scaffolding.quartz.support;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * 解析并调用 {@code beanName.methodName}；仅允许无参或单 {@link String} 参数方法。
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

    /**
     * 反射调用 Spring Bean 方法。
     *
     * <p>无参时调用 {@code method()}；有参时调用 {@code method(String)}。
     *
     * @param context      Spring 容器
     * @param invokeTarget 调用目标，格式 {@code beanName.methodName}
     * @param jobParams    可选，传入单 String 参数
     * @throws IllegalArgumentException Bean 不存在或方法签名不匹配
     */
    public static void invoke(ApplicationContext context, String invokeTarget, String jobParams) throws Exception {
        validateTarget(invokeTarget);
        String trimmed = invokeTarget.trim();
        int dot = trimmed.lastIndexOf('.');
        String beanName = trimmed.substring(0, dot);
        String methodName = trimmed.substring(dot + 1);

        Object bean;
        try {
            bean = context.getBean(beanName);
        } catch (NoSuchBeanDefinitionException ex) {
            throw new IllegalArgumentException("Bean 不存在: " + beanName);
        }

        Method noArg = ReflectionUtils.findMethod(bean.getClass(), methodName);
        Method oneArg = ReflectionUtils.findMethod(bean.getClass(), methodName, String.class);

        boolean hasParams = StringUtils.hasText(jobParams);
        if (hasParams) {
            if (oneArg == null) {
                throw new IllegalArgumentException("方法不存在或签名不匹配（需要 method(String)）: " + methodName);
            }
            ReflectionUtils.makeAccessible(oneArg);
            oneArg.invoke(bean, jobParams.trim());
            return;
        }
        if (noArg != null && noArg.getParameterCount() == 0) {
            ReflectionUtils.makeAccessible(noArg);
            noArg.invoke(bean);
            return;
        }
        if (oneArg != null) {
            ReflectionUtils.makeAccessible(oneArg);
            oneArg.invoke(bean, (String) null);
            return;
        }
        throw new IllegalArgumentException("方法不存在或签名不匹配（需要无参或 method(String)）: " + methodName);
    }
}

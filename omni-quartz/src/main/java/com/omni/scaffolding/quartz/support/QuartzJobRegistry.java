package com.omni.scaffolding.quartz.support;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Quartz 可调用方法注册表。
 *
 * <p>启动时仅收集带 {@link QuartzInvokable} 的公开方法，运行时按原有
 * {@code beanName.methodName} 标识查找并调用，从而避免从管理台访问任意 Spring Bean。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QuartzJobRegistry implements SmartInitializingSingleton {

    private final ApplicationContext applicationContext;

    private volatile Map<String, RegisteredMethod> methods = Map.of();

    /**
     * 所有单例初始化完成后建立不可变白名单。
     */
    @Override
    public void afterSingletonsInstantiated() {
        Map<String, RegisteredMethod> discovered = new LinkedHashMap<>();
        for (String beanName : applicationContext.getBeanDefinitionNames()) {
            Class<?> beanType = applicationContext.getType(beanName);
            if (beanType == null) {
                continue;
            }
            ReflectionUtils.doWithMethods(beanType,
                    method -> register(discovered, beanName, method),
                    method -> method.isAnnotationPresent(QuartzInvokable.class));
        }
        methods = Map.copyOf(discovered);
        log.info("Registered {} Quartz invokable method(s): {}", methods.size(), methods.keySet());
    }

    /**
     * 校验目标格式且确认目标位于白名单。
     *
     * @param invokeTarget 调用目标
     * @throws IllegalArgumentException 格式非法或未注册
     */
    public void validateTarget(String invokeTarget) {
        String target = normalize(invokeTarget);
        if (!methods.containsKey(target)) {
            throw new IllegalArgumentException("调用目标未注册或未标记 @QuartzInvokable: " + target);
        }
    }

    /**
     * 调用白名单内的方法。
     *
     * @param invokeTarget 调用目标
     * @param jobParams    可选字符串参数
     */
    public void invoke(String invokeTarget, String jobParams) throws Exception {
        String target = normalize(invokeTarget);
        RegisteredMethod registered = methods.get(target);
        if (registered == null) {
            throw new IllegalArgumentException("调用目标未注册或未标记 @QuartzInvokable: " + target);
        }

        boolean hasParams = StringUtils.hasText(jobParams);
        if (hasParams && !registered.acceptsString()) {
            throw new IllegalArgumentException("方法签名不匹配（目标仅支持无参调用）: " + target);
        }

        Object bean = applicationContext.getBean(registered.beanName());
        Method invocable = AopUtils.selectInvocableMethod(registered.method(), bean.getClass());
        try {
            if (registered.acceptsString()) {
                invocable.invoke(bean, hasParams ? jobParams.trim() : null);
            } else {
                invocable.invoke(bean);
            }
        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getTargetException();
            if (cause instanceof Exception exception) {
                throw exception;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw ex;
        }
    }

    /**
     * 返回已注册目标快照，便于启动审计与测试。
     */
    public Set<String> registeredTargets() {
        return methods.keySet();
    }

    private static void register(Map<String, RegisteredMethod> discovered, String beanName, Method method) {
        if (!Modifier.isPublic(method.getModifiers())) {
            throw new IllegalStateException("@QuartzInvokable 方法必须是 public: "
                    + beanName + "." + method.getName());
        }
        Class<?>[] parameterTypes = method.getParameterTypes();
        boolean acceptsString;
        if (parameterTypes.length == 0) {
            acceptsString = false;
        } else if (parameterTypes.length == 1 && parameterTypes[0] == String.class) {
            acceptsString = true;
        } else {
            throw new IllegalStateException("@QuartzInvokable 方法仅支持无参或单 String 参数: "
                    + beanName + "." + method.getName());
        }

        String target = beanName + "." + method.getName();
        JobInvokeUtils.validateTarget(target);
        RegisteredMethod previous = discovered.putIfAbsent(
                target, new RegisteredMethod(beanName, method, acceptsString));
        if (previous != null) {
            throw new IllegalStateException("Quartz 调用目标重复，请勿重载或重复声明: " + target);
        }
    }

    private static String normalize(String invokeTarget) {
        JobInvokeUtils.validateTarget(invokeTarget);
        return invokeTarget.trim();
    }

    private record RegisteredMethod(String beanName, Method method, boolean acceptsString) {
    }
}

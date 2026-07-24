package com.omni.scaffolding.util;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Spring 上下文工具：在非 Spring 管理环境（静态方法、工具类、手工 new）中获取 Bean。
 *
 * <p>由容器启动时注入 {@link ApplicationContext}；关闭时清理静态引用，避免热部署泄漏。
 * 优先仍应通过构造器 / {@code @Autowired} 注入；本类仅作无法注入时的兜底。
 */
@Component
public class SpringUtils implements ApplicationContextAware, DisposableBean {

    private static volatile ApplicationContext applicationContext;

    /**
     * Spring 实例化后写入上下文；请勿手动调用。
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringUtils.applicationContext = applicationContext;
    }

    /**
     * 容器销毁时清空静态引用。
     */
    @Override
    public void destroy() {
        applicationContext = null;
    }

    /**
     * 当前 {@link ApplicationContext}；容器未就绪时返回 {@code null}。
     *
     * @return 上下文，或 null
     */
    @Nullable
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 要求上下文已就绪，否则抛出非法状态异常。
     *
     * @return 非 null 上下文
     */
    public static ApplicationContext requireContext() {
        ApplicationContext ctx = applicationContext;
        if (ctx == null) {
            throw new IllegalStateException("ApplicationContext 尚未初始化，无法通过 SpringUtils 获取 Bean");
        }
        return ctx;
    }

    /**
     * 按类型获取唯一 Bean。
     *
     * @param requiredType Bean 类型
     * @param <T>          类型
     * @return Bean 实例
     */
    public static <T> T getBean(Class<T> requiredType) {
        return requireContext().getBean(requiredType);
    }

    /**
     * 按名称获取 Bean。
     *
     * @param name Bean 名称
     * @return Bean 实例
     */
    public static Object getBean(String name) {
        return requireContext().getBean(name);
    }

    /**
     * 按名称与类型获取 Bean。
     *
     * @param name         Bean 名称
     * @param requiredType 期望类型
     * @param <T>          类型
     * @return Bean 实例
     */
    public static <T> T getBean(String name, Class<T> requiredType) {
        return requireContext().getBean(name, requiredType);
    }

    /**
     * 按类型获取全部 Bean（含子类型）。
     *
     * @param type Bean 类型
     * @param <T>  类型
     * @return name → Bean
     */
    public static <T> Map<String, T> getBeansOfType(Class<T> type) {
        return requireContext().getBeansOfType(type);
    }

    /**
     * 是否包含指定名称的 Bean 定义。
     *
     * @param name Bean 名称
     * @return 存在则为 {@code true}
     */
    public static boolean containsBean(String name) {
        return requireContext().containsBean(name);
    }

    /**
     * 指定名称的 Bean 是否为单例。
     *
     * @param name Bean 名称
     * @return 单例则为 {@code true}
     * @throws NoSuchBeanDefinitionException 名称不存在
     */
    public static boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
        return requireContext().isSingleton(name);
    }

    /**
     * 读取 Bean 类型。
     *
     * @param name Bean 名称
     * @return 类型，不存在时可能为 null
     * @throws NoSuchBeanDefinitionException 名称不存在
     */
    public static Class<?> getType(String name) throws NoSuchBeanDefinitionException {
        return requireContext().getType(name);
    }

    /**
     * 当前 {@link Environment}。
     *
     * @return 环境对象
     */
    public static Environment getEnvironment() {
        return requireContext().getEnvironment();
    }

    /**
     * 读取配置属性。
     *
     * @param key 配置键
     * @return 属性值，不存在时为 null
     */
    @Nullable
    public static String getProperty(String key) {
        return getEnvironment().getProperty(key);
    }

    /**
     * 读取配置属性，缺失时返回默认值。
     *
     * @param key          配置键
     * @param defaultValue 默认值
     * @return 属性值或默认值
     */
    public static String getProperty(String key, String defaultValue) {
        return getEnvironment().getProperty(key, defaultValue);
    }

    /**
     * 发布应用事件。
     *
     * @param event 事件
     */
    public static void publishEvent(ApplicationEvent event) {
        requireContext().publishEvent(event);
    }

    /**
     * 发布任意事件对象（Spring 4.2+）。
     *
     * @param event 事件载荷
     */
    public static void publishEvent(Object event) {
        requireContext().publishEvent(event);
    }
}

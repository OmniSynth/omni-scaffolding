package com.omni.scaffolding.config;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.omni.scaffolding.common.persistence.DataSourceKeys;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 读写分离：{@code *QueryMapper} 复杂读走从库。
 *
 * <p>JPA 默认仍走主库，避免写后立即读遇到复制延迟。
 * 写事务已绑定主库连接后，嵌套的从库切换不会拆掉已有 ConnectionHolder。
 * 未配置从库且 {@code strict=false} 时回落主库，单库可用。
 *
 * <p>需要显式指定时可用 {@code @com.baomidou.dynamic.datasource.annotation.DS}。
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class ReadWriteDataSourceAspect {

    /**
     * MyBatis 复杂读统一走从库。
     *
     * @param pjp 切入点
     * @return 原方法返回值
     * @throws Throwable 业务异常原样抛出
     */
    @Around("execution(* com.omni.scaffolding.modules..mapper.*QueryMapper.*(..))")
    public Object routeQueryMapper(ProceedingJoinPoint pjp) throws Throwable {
        DynamicDataSourceContextHolder.push(DataSourceKeys.SLAVE);
        try {
            return pjp.proceed();
        } finally {
            DynamicDataSourceContextHolder.poll();
        }
    }
}

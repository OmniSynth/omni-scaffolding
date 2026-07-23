package com.omni.scaffolding.config;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.omni.scaffolding.common.persistence.DataSourceKeys;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 读写分离：{@code *QueryMapper} 的<strong>读</strong>方法走从库。
 *
 * <p>JPA 默认仍走主库，避免写后立即读遇到复制延迟。
 * {@code insert/update/delete/clear} 等写方法留在当前数据源（事务内即主库），
 * 避免与 JPA 未提交写入分到不同连接导致外键失败。
 *
 * <p>未配置从库且 {@code strict=false} 时读路由回落 master。
 * 需要显式指定时可用 {@code @com.baomidou.dynamic.datasource.annotation.DS}。
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class ReadWriteDataSourceAspect {

    /**
     * MyBatis 复杂读走从库；写方法不切换。
     *
     * @param pjp 切入点
     * @return 原方法返回值
     * @throws Throwable 业务异常原样抛出
     */
    @Around("execution(* com.omni.scaffolding.modules..mapper.*QueryMapper.*(..))")
    public Object routeQueryMapper(ProceedingJoinPoint pjp) throws Throwable {
        if (!isReadMethod(pjp)) {
            return pjp.proceed();
        }
        DynamicDataSourceContextHolder.push(DataSourceKeys.SLAVE);
        try {
            return pjp.proceed();
        } finally {
            DynamicDataSourceContextHolder.poll();
        }
    }

    /**
     * 按方法名约定识别只读查询（与现有 Mapper 命名对齐）。
     */
    private static boolean isReadMethod(ProceedingJoinPoint pjp) {
        if (!(pjp.getSignature() instanceof MethodSignature signature)) {
            return false;
        }
        String name = signature.getMethod().getName();
        return name.startsWith("find")
                || name.startsWith("list")
                || name.startsWith("search")
                || name.startsWith("count")
                || name.startsWith("get")
                || name.startsWith("select")
                || name.startsWith("page")
                || name.startsWith("load")
                || name.startsWith("query")
                || name.startsWith("exists")
                || name.startsWith("stat");
    }
}

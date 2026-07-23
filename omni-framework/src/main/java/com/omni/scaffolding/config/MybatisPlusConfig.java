package com.omni.scaffolding.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 装配。
 *
 * <p>仅扫描带 {@link org.apache.ibatis.annotations.Mapper} 的接口，
 * 避免把 Spring Data JPA 的 {@code Repository} 误注册成 Mapper（双轨共存的关键点）。
 */
@Configuration
@MapperScan(
        basePackages = "com.omni.scaffolding.modules",
        annotationClass = org.apache.ibatis.annotations.Mapper.class
)
public class MybatisPlusConfig {

    /**
     * 分页插件；列表查询应强制分页，禁止无界全表扫描。
     *
     * @return 已注册 MySQL 分页拦截器的 {@link MybatisPlusInterceptor}
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}

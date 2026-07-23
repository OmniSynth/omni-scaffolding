package com.omni.scaffolding.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA Repository 扫描配置。
 *
 * <p>与 MyBatis 分工：Repository 负责写路径与简单查询；复杂 SQL 见各模块 {@code mapper} 包。
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.omni.scaffolding.modules")
public class JpaConfig {
}

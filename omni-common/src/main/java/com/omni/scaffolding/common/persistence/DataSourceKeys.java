package com.omni.scaffolding.common.persistence;

/**
 * 动态数据源键名约定（主从读写分离）。
 *
 * <p>默认 primary={@link #MASTER}；未配置从库或 {@code strict=false} 时，
 * 指向 {@link #SLAVE} 的切换会回落到主库，因此单库也可直接启动。
 */
public final class DataSourceKeys {

    /**
     * 写库（Flyway / Quartz / JPA 写 / 默认路由）。
     */
    public static final String MASTER = "master";

    /**
     * 读库（MyBatis {@code *QueryMapper} 复杂读）。
     */
    public static final String SLAVE = "slave";

    private DataSourceKeys() {
    }
}

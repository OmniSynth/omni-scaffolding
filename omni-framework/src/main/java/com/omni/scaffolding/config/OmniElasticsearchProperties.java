package com.omni.scaffolding.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Elasticsearch 开关与脚手架约定配置。
 *
 * <p>默认 {@code enabled=false}：不连接 ES，也不加载检索 Bean；
 * 需要时设 {@code omni.elasticsearch.enabled=true} 并配置 {@code spring.elasticsearch.uris}。
 */
@Data
@Component
@ConfigurationProperties(prefix = "omni.elasticsearch")
public class OmniElasticsearchProperties {

    /**
     * 是否启用 Elasticsearch。
     * <p>false（默认）时由 {@link com.omni.scaffolding.config.elasticsearch.ElasticsearchEnableEnvironmentPostProcessor}
     * 排除 ES / Spring Data Elasticsearch 自动配置，应用可无 ES 正常启动。
     */
    private boolean enabled = false;

    /**
     * 演示商品索引名（需与 {@code DemoProductDocument} 的 {@code @Document} 一致）。
     */
    private String productIndex = "omni_demo_product";
}

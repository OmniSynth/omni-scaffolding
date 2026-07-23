package com.omni.scaffolding.config.elasticsearch;

import com.omni.scaffolding.config.OmniElasticsearchProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * Elasticsearch 装配标记（仅 {@code omni.elasticsearch.enabled=true} 时生效）。
 *
 * <p>连接、超时等通用项使用 Spring Boot {@code spring.elasticsearch.*}；
 * 脚手架额外约定见 {@link OmniElasticsearchProperties}。
 * 具体索引读写在业务模块（如 omni-demo）中完成。
 */
@Configuration
@ConditionalOnProperty(prefix = "omni.elasticsearch", name = "enabled", havingValue = "true")
public class ElasticsearchConfig {
}

package com.omni.scaffolding.config.elasticsearch;

import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;

import java.util.Set;

/**
 * 在自动配置导入阶段过滤 Elasticsearch 相关配置。
 *
 * <p>比仅改 {@code spring.autoconfigure.exclude} 更可靠：避免 EPP 时序 / 属性覆盖导致
 * {@code enabled=false} 时仍创建 {@code ReactiveElasticsearchClient} 并触发 Health 探测。
 */
public class ElasticsearchAutoConfigurationImportFilter implements AutoConfigurationImportFilter, EnvironmentAware {

    static final Set<String> ES_AUTO_CONFIGS = Set.of(
            "org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration",
            "org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchClientAutoConfiguration",
            "org.springframework.boot.autoconfigure.elasticsearch.ReactiveElasticsearchClientAutoConfiguration",
            "org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration",
            "org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchRepositoriesAutoConfiguration",
            "org.springframework.boot.autoconfigure.data.elasticsearch.ReactiveElasticsearchRepositoriesAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.elasticsearch.ElasticsearchRestHealthContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.data.elasticsearch.ElasticsearchReactiveHealthContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.export.elastic.ElasticMetricsExportAutoConfiguration"
    );

    private Environment environment = new StandardEnvironment();

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public boolean[] match(String[] autoConfigurationClasses, AutoConfigurationMetadata metadata) {
        boolean enabled = environment.getProperty(
                ElasticsearchEnableEnvironmentPostProcessor.ENABLED_KEY, Boolean.class, false);
        boolean[] matches = new boolean[autoConfigurationClasses.length];
        for (int i = 0; i < autoConfigurationClasses.length; i++) {
            String className = autoConfigurationClasses[i];
            // true=保留；关闭开关时丢弃 ES 相关自动配置
            matches[i] = className == null || enabled || !ES_AUTO_CONFIGS.contains(className);
        }
        return matches;
    }
}

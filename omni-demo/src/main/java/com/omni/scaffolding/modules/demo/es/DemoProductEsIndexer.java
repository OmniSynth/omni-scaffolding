package com.omni.scaffolding.modules.demo.es;

import com.omni.scaffolding.config.OmniElasticsearchProperties;
import com.omni.scaffolding.modules.demo.entity.DemoProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Component;

/**
 * 将 MySQL 商品同步写入 ES（仅 {@code omni.elasticsearch.enabled=true} 时注册）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "omni.elasticsearch", name = "enabled", havingValue = "true")
public class DemoProductEsIndexer {

    private final ElasticsearchOperations elasticsearchOperations;
    private final OmniElasticsearchProperties elasticsearchProperties;

    /**
     * 单条索引（创建/更新商品后调用）。
     */
    public void index(DemoProduct product) {
        DemoProductDocument doc = toDocument(product);
        elasticsearchOperations.save(doc, indexCoordinates());
        log.debug("ES indexed product id={}, sku={}", doc.getId(), doc.getSku());
    }

    /**
     * 按主键删除文档（逻辑删除后可调用）。
     */
    public void deleteById(Long id) {
        elasticsearchOperations.delete(String.valueOf(id), indexCoordinates());
    }

    /**
     * 获取当前配置的商品索引坐标。
     *
     * @return ES 索引坐标
     */
    private IndexCoordinates indexCoordinates() {
        return IndexCoordinates.of(elasticsearchProperties.getProductIndex());
    }

    static DemoProductDocument toDocument(DemoProduct product) {
        DemoProductDocument doc = new DemoProductDocument();
        doc.setId(String.valueOf(product.getId()));
        doc.setSku(product.getSku());
        doc.setName(product.getName());
        doc.setCategory(product.getCategory());
        doc.setPriceCents(product.getPriceCents());
        doc.setStock(product.getStock());
        doc.setStatus(product.getStatus());
        doc.setCreatedAt(product.getCreatedAt());
        return doc;
    }
}

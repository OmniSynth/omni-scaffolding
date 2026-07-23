package com.omni.scaffolding.modules.demo.service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.omni.scaffolding.common.api.PageQuery;
import com.omni.scaffolding.common.api.PageResult;
import com.omni.scaffolding.config.OmniElasticsearchProperties;
import com.omni.scaffolding.modules.demo.dto.ProductView;
import com.omni.scaffolding.modules.demo.entity.DemoProduct;
import com.omni.scaffolding.modules.demo.es.DemoProductDocument;
import com.omni.scaffolding.modules.demo.es.DemoProductEsIndexer;
import com.omni.scaffolding.modules.demo.repository.DemoProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品 ES 检索演示：建索引、全量重建、多字段搜索。
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "omni.elasticsearch", name = "enabled", havingValue = "true")
public class DemoProductEsService implements ApplicationRunner {

    private final ElasticsearchOperations elasticsearchOperations;
    private final OmniElasticsearchProperties elasticsearchProperties;
    private final DemoProductRepository productRepository;
    private final DemoProductEsIndexer productEsIndexer;

    /**
     * 启动时确保索引与 mapping 存在。
     *
     * @param args 启动参数（未使用）
     */
    @Override
    public void run(ApplicationArguments args) {
        ensureIndex();
    }

    /**
     * 全量重建：从 MySQL 拉取未删除商品写入 ES。
     *
     * @return 索引文档数
     */
    public int reindexAll() {
        ensureIndex();
        List<DemoProduct> products = productRepository.findAll().stream()
                .filter(p -> p.getDeleted() == null || p.getDeleted() == 0)
                .toList();
        for (DemoProduct product : products) {
            productEsIndexer.index(product);
        }
        log.info("ES reindex finished, count={}, index={}", products.size(), elasticsearchProperties.getProductIndex());
        return products.size();
    }

    /**
     * ES 分页搜索（对照 MyBatis {@code GET /api/demo/products}）。
     *
     * @param keyword  匹配 name / sku / category
     * @param category 精确过滤类目
     * @param status   精确过滤状态
     * @param page     页码
     * @param size     每页条数
     * @return 分页结果
     */
    public PageResult<ProductView> search(String keyword, String category, String status, Long page, Long size) {
        PageQuery pq = PageQuery.of(page, size);
        Query query = buildQuery(keyword, category, status);
        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(query)
                .withPageable(PageRequest.of((int) (pq.getPage() - 1), (int) pq.getSize()))
                .build();

        SearchHits<DemoProductDocument> hits = elasticsearchOperations
                .search(nativeQuery, DemoProductDocument.class, indexCoordinates());
        List<ProductView> records = hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(this::toView)
                .toList();
        return pq.toResult(hits.getTotalHits(), records);
    }

    /**
     * 确保 ES 索引与 mapping 已创建。
     */
    private void ensureIndex() {
        IndexOperations indexOps = elasticsearchOperations.indexOps(DemoProductDocument.class);
        if (!indexOps.exists()) {
            indexOps.createWithMapping();
            log.info("ES index created: {}", elasticsearchProperties.getProductIndex());
        }
    }

    /**
     * 获取当前配置的商品索引坐标。
     *
     * @return ES 索引坐标
     */
    private IndexCoordinates indexCoordinates() {
        return IndexCoordinates.of(elasticsearchProperties.getProductIndex());
    }

    /**
     * 组装 ES 搜索 Query：关键词 multi_match，类目 / 状态 term 过滤。
     *
     * @param keyword  关键词，可为空
     * @param category 类目，可为空
     * @param status   状态，可为空
     * @return Elasticsearch Query
     */
    private static Query buildQuery(String keyword, String category, String status) {
        List<Query> must = new ArrayList<>();
        List<Query> filter = new ArrayList<>();

        if (StringUtils.hasText(keyword)) {
            must.add(Query.of(q -> q.multiMatch(m -> m
                    .query(keyword.trim())
                    .fields("name^2", "sku", "category"))));
        }
        if (StringUtils.hasText(category)) {
            filter.add(Query.of(q -> q.term(t -> t.field("category").value(category.trim()))));
        }
        if (StringUtils.hasText(status)) {
            filter.add(Query.of(q -> q.term(t -> t.field("status").value(status.trim()))));
        }

        if (must.isEmpty() && filter.isEmpty()) {
            return Query.of(q -> q.matchAll(m -> m));
        }

        BoolQuery.Builder bool = new BoolQuery.Builder();
        must.forEach(bool::must);
        filter.forEach(bool::filter);
        return Query.of(q -> q.bool(bool.build()));
    }

    /**
     * ES 文档转 API 读模型 {@link ProductView}。
     *
     * @param doc ES 商品文档
     * @return 读模型
     */
    private ProductView toView(DemoProductDocument doc) {
        ProductView view = new ProductView();
        view.setId(Long.valueOf(doc.getId()));
        view.setSku(doc.getSku());
        view.setName(doc.getName());
        view.setCategory(doc.getCategory());
        view.setPriceCents(doc.getPriceCents());
        view.setStock(doc.getStock());
        view.setStatus(doc.getStatus());
        view.setCreatedAt(doc.getCreatedAt());
        return view;
    }
}

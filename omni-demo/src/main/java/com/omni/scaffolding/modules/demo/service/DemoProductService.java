package com.omni.scaffolding.modules.demo.service;

import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.api.PageQuery;
import com.omni.scaffolding.common.api.PageResult;
import com.omni.scaffolding.common.cache.CacheNames;
import com.omni.scaffolding.common.cache.RedisKeys;
import com.omni.scaffolding.common.exception.BusinessException;
import com.omni.scaffolding.infra.lock.DistributedLockService;
import com.omni.scaffolding.modules.demo.dto.CategoryStatsView;
import com.omni.scaffolding.modules.demo.dto.ProductCreateRequest;
import com.omni.scaffolding.modules.demo.dto.ProductView;
import com.omni.scaffolding.modules.demo.entity.DemoProduct;
import com.omni.scaffolding.modules.demo.es.DemoProductEsIndexer;
import com.omni.scaffolding.modules.demo.mapper.DemoProductQueryMapper;
import com.omni.scaffolding.modules.demo.repository.DemoProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 商品演示服务 —— 完整展示双轨持久化 + 分布式锁 + 缓存。
 *
 * <ul>
 *   <li>写：JPA {@link DemoProductRepository}</li>
 *   <li>复杂读 / 聚合：MyBatis {@link DemoProductQueryMapper}</li>
 *   <li>创建时按 SKU 加 Redis 锁，防止并发重复创建</li>
 *   <li>ES 启用时：写成功后同步索引（{@link DemoProductEsIndexer}）</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class DemoProductService {

    private final DemoProductRepository productRepository;
    private final DemoProductQueryMapper productQueryMapper;
    private final DistributedLockService lockService;
    /**
     * ES 关闭时为空，不影响主流程。
     */
    private final ObjectProvider<DemoProductEsIndexer> productEsIndexer;

    /**
     * 创建商品（JPA 写路径）。
     *
     * <p>锁粒度=SKU；写成功后失效统计缓存。
     *
     * @param request 创建请求
     * @return 创建后的读模型
     */
    @Transactional
    @CacheEvict(cacheNames = CacheNames.PRODUCT_STATS, allEntries = true)
    public ProductView create(ProductCreateRequest request) {
        String lockKey = RedisKeys.lockProductSku(request.getSku());
        return lockService.executeWithLock(lockKey, 3, 10, TimeUnit.SECONDS, () -> {
            if (productRepository.existsBySkuAndDeleted(request.getSku(), 0)) {
                throw new BusinessException(ErrorCode.CONFLICT, "SKU 已存在");
            }
            DemoProduct product = new DemoProduct();
            product.setId(ThreadLocalRandom.current().nextLong(1_000_000L, Long.MAX_VALUE));
            product.setSku(request.getSku());
            product.setName(request.getName());
            product.setCategory(request.getCategory());
            product.setPriceCents(request.getPriceCents());
            product.setStock(request.getStock());
            product.setStatus("ACTIVE");
            product.setDeleted(0);
            DemoProduct saved = productRepository.save(product);
            productEsIndexer.ifAvailable(indexer -> indexer.index(saved));
            return toView(saved);
        });
    }

    /**
     * 简单主键查询走 JPA。
     *
     * @param id 商品 ID
     * @return 未删除的商品实体
     */
    @Transactional(readOnly = true)
    public DemoProduct getById(Long id) {
        return productRepository.findById(id)
                .filter(p -> p.getDeleted() == 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "商品不存在"));
    }

    /**
     * 动态条件分页搜索（MyBatis XML），适合多可选过滤条件场景。
     *
     * @param category  可选，类目
     * @param keyword   可选，关键词
     * @param status    可选，状态
     * @param minPrice  可选，最低价（分）
     * @param maxPrice  可选，最高价（分）
     * @param page      页码
     * @param size      每页条数
     * @return 分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<ProductView> search(String category,
                                          String keyword,
                                          String status,
                                          Long minPrice,
                                          Long maxPrice,
                                          Long page,
                                          Long size) {
        PageQuery pq = PageQuery.of(page, size);
        long total = productQueryMapper.countProducts(category, keyword, status, minPrice, maxPrice);
        if (total == 0) {
            return pq.toResult(0, List.of());
        }
        return pq.toResult(total, productQueryMapper.searchProducts(
                category, keyword, status, minPrice, maxPrice, pq.getSize(), pq.getOffset()));
    }

    /**
     * 按类目聚合统计（MyBatis），结果适合短时缓存。
     *
     * @return 各类目统计行
     */
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.PRODUCT_STATS)
    public List<CategoryStatsView> categoryStats() {
        return productQueryMapper.categoryStats();
    }

    /**
     * 实体转 API 读模型 {@link ProductView}。
     *
     * @param product 商品实体
     * @return 读模型
     */
    private ProductView toView(DemoProduct product) {
        ProductView view = new ProductView();
        view.setId(product.getId());
        view.setSku(product.getSku());
        view.setName(product.getName());
        view.setCategory(product.getCategory());
        view.setPriceCents(product.getPriceCents());
        view.setStock(product.getStock());
        view.setStatus(product.getStatus());
        view.setCreatedAt(product.getCreatedAt());
        return view;
    }
}

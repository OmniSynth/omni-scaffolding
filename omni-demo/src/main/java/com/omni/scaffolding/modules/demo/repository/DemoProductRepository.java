package com.omni.scaffolding.modules.demo.repository;

import com.omni.scaffolding.modules.demo.entity.DemoProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 演示商品 JPA Repository。
 *
 * <p>写路径与按 SKU 的简单查询；复杂统计可走 MyBatis 或其它读模型。
 */
public interface DemoProductRepository extends JpaRepository<DemoProduct, Long> {

    /**
     * 按 SKU 查询指定删除标记的商品。
     *
     * @param sku     业务唯一键
     * @param deleted 删除标记，业务侧通常传 {@code 0}
     * @return 匹配的商品，不存在则空
     */
    Optional<DemoProduct> findBySkuAndDeleted(String sku, Integer deleted);

    /**
     * SKU 是否已存在（创建时唯一性校验）。
     *
     * @param sku     业务唯一键
     * @param deleted 删除标记，业务侧通常传 {@code 0}
     * @return 已存在返回 {@code true}
     */
    boolean existsBySkuAndDeleted(String sku, Integer deleted);
}

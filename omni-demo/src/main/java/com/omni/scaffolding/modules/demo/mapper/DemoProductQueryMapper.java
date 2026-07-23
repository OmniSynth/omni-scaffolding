package com.omni.scaffolding.modules.demo.mapper;

import com.omni.scaffolding.modules.demo.dto.CategoryStatsView;
import com.omni.scaffolding.modules.demo.dto.ProductView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 演示商品复杂查询 Mapper（MyBatis 读）。
 *
 * <p>对应 XML：{@code classpath:mapper/demo/DemoProductQueryMapper.xml}。
 * 商品主表写入走 JPA（{@code DemoProductRepository}）；本接口演示动态条件查询与聚合报表。
 */
@Mapper
public interface DemoProductQueryMapper {

    /**
     * 多条件统计商品数。
     *
     * @param category 可选，类目
     * @param keyword  可选，关键词
     * @param status   可选，状态
     * @param minPrice 可选，最低价（分）
     * @param maxPrice 可选，最高价（分）
     * @return 符合条件的总数
     */
    long countProducts(@Param("category") String category,
                       @Param("keyword") String keyword,
                       @Param("status") String status,
                       @Param("minPrice") Long minPrice,
                       @Param("maxPrice") Long maxPrice);

    /**
     * 多条件动态分页查询，结果映射到读模型 {@link ProductView}，不复用 JPA Entity。
     *
     * @param category 可选，类目
     * @param keyword  可选，关键词
     * @param status   可选，状态
     * @param minPrice 可选，最低价（分）
     * @param maxPrice 可选，最高价（分）
     * @param limit    每页条数
     * @param offset   偏移量
     * @return 当前页记录
     */
    List<ProductView> searchProducts(@Param("category") String category,
                                     @Param("keyword") String keyword,
                                     @Param("status") String status,
                                     @Param("minPrice") Long minPrice,
                                     @Param("maxPrice") Long maxPrice,
                                     @Param("limit") long limit,
                                     @Param("offset") long offset);

    /**
     * 类目维度聚合统计，演示报表类 SQL。
     *
     * @return 各类目统计行
     */
    List<CategoryStatsView> categoryStats();
}

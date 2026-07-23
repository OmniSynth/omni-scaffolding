package com.omni.scaffolding.modules.demo.controller;

import com.omni.scaffolding.common.api.ApiResponse;
import com.omni.scaffolding.common.api.PageResult;
import com.omni.scaffolding.modules.demo.dto.CategoryStatsView;
import com.omni.scaffolding.modules.demo.dto.ProductCreateRequest;
import com.omni.scaffolding.modules.demo.dto.ProductView;
import com.omni.scaffolding.modules.demo.entity.DemoProduct;
import com.omni.scaffolding.modules.demo.service.DemoProductService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商品演示 API：对照 JPA 写 / MyBatis 复杂读。
 *
 * <p>权限码：{@code demo:product:read} / {@code demo:product:write}。
 */
@Tag(name = "Demo Products")
@RestController
@RequestMapping("/api/demo/products")
@RequiredArgsConstructor
public class DemoProductController {

    private final DemoProductService productService;

    /**
     * 创建商品（JPA 写 + Redis 分布式锁）。
     *
     * @param request 商品信息
     * @return 创建后的读模型
     */
    @Operation(summary = "创建商品（JPA + Redis 锁）")
    @PostMapping
    @PreAuthorize("hasAuthority('demo:product:write')")
    @RateLimiter(name = "api")
    public ApiResponse<ProductView> create(@Valid @RequestBody ProductCreateRequest request) {
        return ApiResponse.ok(productService.create(request));
    }

    /**
     * 按主键查询商品（JPA 简单读）。
     *
     * @param id 商品 ID
     * @return 商品实体
     */
    @Operation(summary = "按 ID 查询（JPA）")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('demo:product:read')")
    @RateLimiter(name = "api")
    public ApiResponse<DemoProduct> get(@PathVariable Long id) {
        return ApiResponse.ok(productService.getById(id));
    }

    /**
     * 多条件动态分页搜索（MyBatis 复杂读）。
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
    @Operation(summary = "动态条件分页搜索（MyBatis）")
    @GetMapping
    @PreAuthorize("hasAuthority('demo:product:read')")
    @RateLimiter(name = "api")
    public ApiResponse<PageResult<ProductView>> search(@RequestParam(required = false) String category,
                                                       @RequestParam(required = false) String keyword,
                                                       @RequestParam(required = false) String status,
                                                       @RequestParam(required = false) Long minPrice,
                                                       @RequestParam(required = false) Long maxPrice,
                                                       @RequestParam(required = false) Long page,
                                                       @RequestParam(required = false) Long size) {
        return ApiResponse.ok(productService.search(category, keyword, status, minPrice, maxPrice, page, size));
    }

    /**
     * 按类目聚合统计（MyBatis 报表 SQL）。
     *
     * @return 各类目商品数与金额汇总
     */
    @Operation(summary = "按类目聚合统计（MyBatis）")
    @GetMapping("/stats/by-category")
    @PreAuthorize("hasAuthority('demo:product:read')")
    @RateLimiter(name = "api")
    public ApiResponse<List<CategoryStatsView>> categoryStats() {
        return ApiResponse.ok(productService.categoryStats());
    }
}

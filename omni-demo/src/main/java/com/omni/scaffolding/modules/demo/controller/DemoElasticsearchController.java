package com.omni.scaffolding.modules.demo.controller;

import com.omni.scaffolding.common.api.ApiResponse;
import com.omni.scaffolding.common.api.PageResult;
import com.omni.scaffolding.modules.demo.dto.ProductView;
import com.omni.scaffolding.modules.demo.service.DemoProductEsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Elasticsearch 演示 API：仅启用 ES 时注册该 Controller。
 */
@Tag(name = "Demo Elasticsearch")
@RestController
@RequestMapping("/api/demo/es/products")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "omni.elasticsearch", name = "enabled", havingValue = "true")
public class DemoElasticsearchController {

    private final DemoProductEsService productEsService;

    /**
     * 全量重建商品 ES 索引（MySQL → Elasticsearch）。
     *
     * @return 索引文档数
     */
    @Operation(summary = "全量重建商品索引（MySQL → ES）")
    @PostMapping("/reindex")
    @PreAuthorize("hasAuthority('demo:product:write')")
    public ApiResponse<Map<String, Integer>> reindex() {
        int count = productEsService.reindexAll();
        return ApiResponse.ok(Map.of("indexed", count));
    }

    /**
     * ES 分页搜索商品（对照 MyBatis 动态查询接口）。
     *
     * @param keyword  可选，匹配 name / sku / category
     * @param category 可选，精确类目
     * @param status   可选，精确状态
     * @param page     页码
     * @param size     每页条数
     * @return 分页结果
     */
    @Operation(summary = "ES 分页搜索商品（对照 MyBatis 动态查询）")
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('demo:product:read')")
    public ApiResponse<PageResult<ProductView>> search(@RequestParam(required = false) String keyword,
                                                       @RequestParam(required = false) String category,
                                                       @RequestParam(required = false) String status,
                                                       @RequestParam(required = false) Long page,
                                                       @RequestParam(required = false) Long size) {
        return ApiResponse.ok(productEsService.search(keyword, category, status, page, size));
    }
}

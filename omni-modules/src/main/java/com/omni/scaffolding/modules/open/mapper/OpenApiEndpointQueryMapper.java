package com.omni.scaffolding.modules.open.mapper;

import com.omni.scaffolding.modules.open.dto.endpoint.OpenEndpointView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 开放接口目录复杂读（MyBatis，从库优先）。
 */
@Mapper
public interface OpenApiEndpointQueryMapper {

    /**
     * 统计符合条件的接口数。
     *
     * @param keyword 可选关键字
     * @param status  可选启停
     * @return 总数
     */
    long countEndpoints(@Param("keyword") String keyword, @Param("status") Boolean status);

    /**
     * 分页列表。
     *
     * @param keyword 可选关键字
     * @param status  可选启停
     * @param limit   条数
     * @param offset  偏移
     * @return 读模型列表
     */
    List<OpenEndpointView> listEndpoints(@Param("keyword") String keyword,
                                         @Param("status") Boolean status,
                                         @Param("limit") long limit,
                                         @Param("offset") long offset);

    /**
     * 按主键查详情。
     *
     * @param id 主键
     * @return 读模型，不存在为 null
     */
    OpenEndpointView findById(@Param("id") Long id);

    /**
     * 某客户端已绑定且启用的接口列表（鉴权用）。
     *
     * @param clientId 客户端主键
     * @return 接口列表
     */
    List<OpenEndpointView> listEnabledByClientId(@Param("clientId") Long clientId);

    /**
     * 全部启用接口（管理端下拉）。
     *
     * @return 启用列表
     */
    List<OpenEndpointView> listAllEnabled();
}

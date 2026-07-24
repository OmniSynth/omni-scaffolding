package com.omni.scaffolding.modules.open.mapper;

import com.omni.scaffolding.modules.open.dto.client.OpenClientView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 开放 API 客户端复杂读与关联写。
 *
 * <p>{@code insert/delete} 关联表走主库（命名不被读写切面切到 slave）。
 */
@Mapper
public interface OpenApiClientQueryMapper {

    /**
     * 统计客户端数。
     */
    long countClients(@Param("keyword") String keyword, @Param("status") Boolean status);

    /**
     * 分页列表（不含 IP / 绑定明细）。
     */
    List<OpenClientView> listClients(@Param("keyword") String keyword,
                                     @Param("status") Boolean status,
                                     @Param("limit") long limit,
                                     @Param("offset") long offset);

    /**
     * 按主键查基础信息。
     */
    OpenClientView findById(@Param("id") Long id);

    /**
     * 客户端 IP 白名单。
     */
    List<String> listIpsByClientId(@Param("clientId") Long clientId);

    /**
     * 客户端已绑定接口 ID。
     */
    List<Long> listEndpointIdsByClientId(@Param("clientId") Long clientId);

    /**
     * 清空 IP 白名单。
     */
    void deleteIps(@Param("clientId") Long clientId);

    /**
     * 插入一条 IP。
     */
    void insertIp(@Param("clientId") Long clientId, @Param("ipAddr") String ipAddr);

    /**
     * 清空接口绑定。
     */
    void deleteEndpoints(@Param("clientId") Long clientId);

    /**
     * 绑定一条接口。
     */
    void insertEndpoint(@Param("clientId") Long clientId, @Param("endpointId") Long endpointId);

    /**
     * 统计某接口被多少客户端绑定（删除接口前校验）。
     */
    long countBindingsByEndpointId(@Param("endpointId") Long endpointId);
}

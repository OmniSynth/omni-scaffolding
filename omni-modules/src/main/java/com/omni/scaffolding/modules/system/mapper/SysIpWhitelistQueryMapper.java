package com.omni.scaffolding.modules.system.mapper;

import com.omni.scaffolding.modules.system.dto.ip.IpWhitelistView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * IP 白名单读模型 Mapper（MyBatis）。
 *
 * <p>管理端列表 / 详情走本接口；启用 IP 列表供运行时校验缓存加载。
 */
@Mapper
public interface SysIpWhitelistQueryMapper {

    /**
     * 统计符合条件的未删除记录数。
     *
     * @param keyword 可选，匹配 IP / 备注
     * @param status  可选，启停状态
     * @return 总数
     */
    long count(@Param("keyword") String keyword, @Param("status") Boolean status);

    /**
     * 分页查询未删除白名单。
     *
     * @param keyword 可选，匹配 IP / 备注
     * @param status  可选，启停状态
     * @param limit   每页条数
     * @param offset  偏移量
     * @return 读模型列表
     */
    List<IpWhitelistView> list(@Param("keyword") String keyword,
                               @Param("status") Boolean status,
                               @Param("limit") long limit,
                               @Param("offset") long offset);

    /**
     * 按主键查询未删除详情。
     *
     * @param id 主键
     * @return 读模型，不存在则为 {@code null}
     */
    IpWhitelistView findById(@Param("id") Long id);

    /**
     * 列出全部启用且未删除的 IP。
     *
     * @return IP 列表，可能为空
     */
    List<String> listEnabledIps();
}

package com.omni.scaffolding.modules.system.mapper;

import com.omni.scaffolding.modules.system.dto.config.ConfigView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统参数读模型 Mapper（MyBatis）。
 *
 * <p>管理端列表 / 详情及按键查询启用中的参数值。
 */
@Mapper
public interface SysConfigQueryMapper {

    /**
     * 统计符合条件的未删除参数数。
     *
     * @param keyword 可选，匹配参数键 / 名称 / 值
     * @return 总数
     */
    long countConfigs(@Param("keyword") String keyword);

    /**
     * 分页查询未删除参数。
     *
     * @param keyword 可选，匹配参数键 / 名称 / 值
     * @param limit   每页条数
     * @param offset  偏移量
     * @return 读模型列表
     */
    List<ConfigView> listConfigs(@Param("keyword") String keyword,
                                 @Param("limit") long limit,
                                 @Param("offset") long offset);

    /**
     * 按主键查询未删除参数详情。
     *
     * @param configId 参数主键
     * @return 读模型，不存在则为 {@code null}
     */
    ConfigView findById(@Param("configId") Long configId);

    /**
     * 按参数键查询启用且未删除的参数。
     *
     * @param configKey 参数键
     * @return 读模型，不存在或已停用则为 {@code null}
     */
    ConfigView findByKey(@Param("configKey") String configKey);
}

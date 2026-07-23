package com.omni.scaffolding.modules.system.mapper;

import com.omni.scaffolding.modules.system.dto.dict.DictTypeView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 字典类型读模型 Mapper（MyBatis）。
 */
@Mapper
public interface SysDictTypeQueryMapper {

    /**
     * 统计符合条件的未删除字典类型数。
     *
     * @param keyword 可选，匹配编码 / 名称
     * @return 总数
     */
    long countTypes(@Param("keyword") String keyword);

    /**
     * 分页查询未删除字典类型。
     *
     * @param keyword 可选，匹配编码 / 名称
     * @param limit   每页条数
     * @param offset  偏移量
     * @return 读模型列表
     */
    List<DictTypeView> listTypes(@Param("keyword") String keyword,
                                 @Param("limit") long limit,
                                 @Param("offset") long offset);

    /**
     * 按主键查询未删除字典类型详情。
     *
     * @param typeId 类型主键
     * @return 读模型，不存在则为 {@code null}
     */
    DictTypeView findById(@Param("typeId") Long typeId);

    /**
     * 统计指定类型编码下的未删除字典数据数。
     *
     * @param typeCode 类型编码
     * @return 数据条数
     */
    long countDataByTypeCode(@Param("typeCode") String typeCode);
}

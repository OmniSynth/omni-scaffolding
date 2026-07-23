package com.omni.scaffolding.modules.system.mapper;

import com.omni.scaffolding.modules.system.dto.dict.DictDataView;
import com.omni.scaffolding.modules.system.dto.dict.DictOptionView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 字典数据读模型 Mapper（MyBatis）。
 */
@Mapper
public interface SysDictDataQueryMapper {

    /**
     * 统计指定类型下符合条件的未删除字典数据数。
     *
     * @param typeCode 类型编码
     * @param keyword  可选，匹配标签 / 值
     * @return 总数
     */
    long countData(@Param("typeCode") String typeCode, @Param("keyword") String keyword);

    /**
     * 分页查询指定类型下的未删除字典数据。
     *
     * @param typeCode 类型编码
     * @param keyword  可选，匹配标签 / 值
     * @param limit    每页条数
     * @param offset   偏移量
     * @return 读模型列表
     */
    List<DictDataView> listData(@Param("typeCode") String typeCode,
                                @Param("keyword") String keyword,
                                @Param("limit") long limit,
                                @Param("offset") long offset);

    /**
     * 按主键查询未删除字典数据详情。
     *
     * @param dataId 数据主键
     * @return 读模型，不存在则为 {@code null}
     */
    DictDataView findById(@Param("dataId") Long dataId);

    /**
     * 列出指定类型下启用中的下拉选项；类型停用时返回空。
     *
     * @param typeCode 类型编码
     * @return 选项列表，可能为空
     */
    List<DictOptionView> listOptions(@Param("typeCode") String typeCode);

    /**
     * 清除指定类型下除指定主键外的默认项标记。
     *
     * @param typeCode  类型编码
     * @param excludeId 保留默认标记的数据主键，可为 {@code null}
     */
    void clearDefaultFlag(@Param("typeCode") String typeCode, @Param("excludeId") Long excludeId);
}

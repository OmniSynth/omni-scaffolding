package com.omni.scaffolding.modules.system.mapper;

import com.omni.scaffolding.modules.system.dto.FileView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文件元数据复杂读（MyBatis，走从库约定）。
 */
@Mapper
public interface SysFileQueryMapper {

    /**
     * 统计符合条件的文件数（不含已删除）。
     *
     * @param keyword           可选，匹配原始文件名
     * @param bizType           可选，业务类型精确匹配
     * @param storageType       可选，存储类型精确匹配
     * @param contentTypePrefix 可选，MIME 前缀（如 {@code image/}）
     * @return 总数
     */
    long countFiles(@Param("keyword") String keyword,
                    @Param("bizType") String bizType,
                    @Param("storageType") String storageType,
                    @Param("contentTypePrefix") String contentTypePrefix);

    /**
     * 分页搜索文件元数据。
     *
     * @param keyword           可选，匹配原始文件名
     * @param bizType           可选，业务类型
     * @param storageType       可选，存储类型
     * @param contentTypePrefix 可选，MIME 前缀
     * @param limit             每页条数
     * @param offset            偏移量
     * @return 当前页记录（不含 previewUrl，由服务层填充）
     */
    List<FileView> searchFiles(@Param("keyword") String keyword,
                               @Param("bizType") String bizType,
                               @Param("storageType") String storageType,
                               @Param("contentTypePrefix") String contentTypePrefix,
                               @Param("limit") long limit,
                               @Param("offset") long offset);
}

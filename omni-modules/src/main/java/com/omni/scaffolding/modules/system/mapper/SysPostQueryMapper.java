package com.omni.scaffolding.modules.system.mapper;

import com.omni.scaffolding.modules.system.dto.post.PostView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 岗位复杂读 Mapper（MyBatis）。
 *
 * <p>对应 XML：{@code classpath:mapper/system/SysPostQueryMapper.xml}。
 * 岗位主表写入走 JPA（{@code SysPostRepository}）；本接口负责列表 / 详情读模型与绑定用户数。
 */
@Mapper
public interface SysPostQueryMapper {

    /**
     * 岗位总数；{@code keyword} 模糊匹配编码或名称。
     *
     * @param keyword 可选，匹配编码 / 名称
     * @return 总数
     */
    long countPosts(@Param("keyword") String keyword);

    /**
     * 岗位分页列表；{@code keyword} 模糊匹配编码或名称，为空则不过滤。
     *
     * @param keyword 可选，匹配编码 / 名称
     * @param limit   每页条数
     * @param offset  偏移量
     * @return 读模型列表
     */
    List<PostView> listPosts(@Param("keyword") String keyword,
                             @Param("limit") long limit,
                             @Param("offset") long offset);

    /**
     * 按岗位 ID 查询读模型；不存在或已删除返回 {@code null}。
     *
     * @param postId 岗位主键
     * @return 读模型，不存在则为 {@code null}
     */
    PostView findById(@Param("postId") Long postId);

    /**
     * 统计仍绑定该岗位的未删除用户数（删岗位前校验）。
     *
     * @param postId 岗位主键
     * @return 用户数
     */
    long countUsersByPostId(@Param("postId") Long postId);
}

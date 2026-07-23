package com.omni.scaffolding.modules.system.mapper;

import com.omni.scaffolding.modules.system.dto.notice.NoticeView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通知公告读模型 Mapper（MyBatis）。
 *
 * <p>管理端列表 / 详情及用户端未读、收件箱查询。
 */
@Mapper
public interface SysNoticeQueryMapper {

    /**
     * 统计符合条件的未删除公告数。
     *
     * @param keyword 可选，匹配标题 / 内容
     * @param status  可选，启停状态
     * @param type    可选，公告类型
     * @return 总数
     */
    long countNotices(@Param("keyword") String keyword,
                      @Param("status") Boolean status,
                      @Param("type") String type);

    /**
     * 分页查询未删除公告。
     *
     * @param keyword 可选，匹配标题 / 内容
     * @param status  可选，启停状态
     * @param type    可选，公告类型
     * @param limit   每页条数
     * @param offset  偏移量
     * @return 读模型列表
     */
    List<NoticeView> listNotices(@Param("keyword") String keyword,
                                 @Param("status") Boolean status,
                                 @Param("type") String type,
                                 @Param("limit") long limit,
                                 @Param("offset") long offset);

    /**
     * 按主键查询未删除公告详情。
     *
     * @param noticeId 公告主键
     * @return 读模型，不存在则为 {@code null}
     */
    NoticeView findById(@Param("noticeId") Long noticeId);

    /**
     * 列出当前用户未读的启用公告。
     *
     * @param userId 用户主键
     * @return 读模型列表，可能为空
     */
    List<NoticeView> listUnread(@Param("userId") Long userId);

    /**
     * 统计当前用户未读公告数。
     *
     * @param userId 用户主键
     * @return 未读数量
     */
    long countUnread(@Param("userId") Long userId);

    /**
     * 查询当前用户公告收件箱（含已读标记）。
     *
     * @param userId 用户主键
     * @param limit  最大条数
     * @return 读模型列表，可能为空
     */
    List<NoticeView> listInbox(@Param("userId") Long userId, @Param("limit") long limit);

    /**
     * 列出当前用户未读公告主键。
     *
     * @param userId 用户主键
     * @return 公告主键列表，可能为空
     */
    List<Long> listUnreadIds(@Param("userId") Long userId);
}

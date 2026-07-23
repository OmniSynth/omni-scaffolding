package com.omni.scaffolding.modules.system.repository;

import com.omni.scaffolding.modules.system.entity.SysNoticeRead;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 公告已读 JPA 写仓储。
 */
public interface SysNoticeReadRepository extends JpaRepository<SysNoticeRead, Long> {

    /**
     * 判断用户是否已读指定公告。
     *
     * @param noticeId 公告主键
     * @param userId   用户主键
     * @return 已读则为 {@code true}
     */
    boolean existsByNoticeIdAndUserId(Long noticeId, Long userId);
}

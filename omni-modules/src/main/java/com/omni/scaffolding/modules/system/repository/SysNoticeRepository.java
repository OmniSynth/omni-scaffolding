package com.omni.scaffolding.modules.system.repository;

import com.omni.scaffolding.modules.system.entity.SysNotice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 通知公告 JPA 写仓储。
 */
public interface SysNoticeRepository extends JpaRepository<SysNotice, Long> {

    /**
     * 按主键查询未删除记录。
     *
     * @param id      主键
     * @param deleted 删除标记，通常传 {@code 0}
     * @return 实体，不存在则为空
     */
    Optional<SysNotice> findByIdAndDeleted(Long id, Integer deleted);
}

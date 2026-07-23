package com.omni.scaffolding.modules.system.repository;

import com.omni.scaffolding.modules.system.entity.SysJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 定时任务 JPA 写仓储。
 */
public interface SysJobRepository extends JpaRepository<SysJob, Long> {

    /**
     * 按主键查询未删除记录。
     *
     * @param id      主键
     * @param deleted 删除标记，通常传 {@code 0}
     * @return 实体，不存在则为空
     */
    Optional<SysJob> findByIdAndDeleted(Long id, Integer deleted);

    /**
     * 按删除标记与启停状态查询任务列表。
     *
     * @param deleted 删除标记，通常传 {@code 0}
     * @param status  启停状态
     * @return 任务列表，可能为空
     */
    List<SysJob> findByDeletedAndStatus(Integer deleted, Boolean status);

    /**
     * 判断未删除范围内是否已存在该任务名称。
     *
     * @param jobName 任务名称
     * @param deleted 删除标记，通常传 {@code 0}
     * @return 存在则为 {@code true}
     */
    boolean existsByJobNameAndDeleted(String jobName, Integer deleted);

    /**
     * 判断未删除范围内是否存在「同名称且非本主键」的记录。
     *
     * @param jobName 任务名称
     * @param deleted 删除标记，通常传 {@code 0}
     * @param id      排除的主键
     * @return 存在冲突则为 {@code true}
     */
    boolean existsByJobNameAndDeletedAndIdNot(String jobName, Integer deleted, Long id);
}

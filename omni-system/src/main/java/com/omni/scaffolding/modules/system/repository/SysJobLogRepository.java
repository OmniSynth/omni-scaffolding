package com.omni.scaffolding.modules.system.repository;

import com.omni.scaffolding.modules.system.entity.SysJobLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 定时任务执行日志 JPA 写仓储。
 */
public interface SysJobLogRepository extends JpaRepository<SysJobLog, Long> {

    /**
     * 按任务主键物理删除全部执行日志。
     *
     * @param jobId 任务主键
     * @return 删除行数
     */
    @Modifying
    @Query("delete from SysJobLog l where l.jobId = :jobId")
    int deleteByJobId(@Param("jobId") Long jobId);
}

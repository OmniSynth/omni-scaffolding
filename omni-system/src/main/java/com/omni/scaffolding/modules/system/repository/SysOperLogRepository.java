package com.omni.scaffolding.modules.system.repository;

import com.omni.scaffolding.modules.system.entity.SysOperLog;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 操作日志 JPA Repository（写路径）。
 */
public interface SysOperLogRepository extends JpaRepository<SysOperLog, Long> {
}

package com.omni.scaffolding.modules.system.repository;

import com.omni.scaffolding.modules.system.entity.SysLoginLog;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 登录日志 JPA Repository（写路径）。
 */
public interface SysLoginLogRepository extends JpaRepository<SysLoginLog, Long> {
}

package com.omni.scaffolding.modules.system.repository;

import com.omni.scaffolding.modules.system.entity.SysIpWhitelist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * IP 白名单 JPA 写仓储。
 */
public interface SysIpWhitelistRepository extends JpaRepository<SysIpWhitelist, Long> {

    /**
     * 按主键查询未删除记录。
     *
     * @param id      主键
     * @param deleted 删除标记，通常传 {@code 0}
     * @return 实体，不存在则为空
     */
    Optional<SysIpWhitelist> findByIdAndDeleted(Long id, Integer deleted);

    /**
     * 按 IP 查询（含已软删），用于唯一键冲突时的恢复或释放。
     *
     * @param ipAddr IP 地址
     * @return 实体，不存在则为空
     */
    Optional<SysIpWhitelist> findByIpAddr(String ipAddr);

    /**
     * 判断未删除范围内是否已存在该 IP。
     *
     * @param ipAddr  IP 地址
     * @param deleted 删除标记，通常传 {@code 0}
     * @return 存在则为 {@code true}
     */
    boolean existsByIpAddrAndDeleted(String ipAddr, Integer deleted);

    /**
     * 判断未删除范围内是否存在「同 IP 且非本主键」的记录。
     *
     * @param ipAddr  IP 地址
     * @param deleted 删除标记，通常传 {@code 0}
     * @param id      排除的主键
     * @return 存在冲突则为 {@code true}
     */
    boolean existsByIpAddrAndDeletedAndIdNot(String ipAddr, Integer deleted, Long id);
}

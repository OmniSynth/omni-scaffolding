package com.omni.scaffolding.modules.system.repository;

import com.omni.scaffolding.modules.system.entity.SysRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 角色 JPA Repository。
 *
 * <p>负责角色主表写入与编码唯一性检查；角色列表（含菜单 ID、用户数）走 MyBatis。
 */
public interface SysRoleRepository extends JpaRepository<SysRole, Long> {

    /**
     * 按主键查询指定删除标记的角色。
     *
     * @param id      角色 ID
     * @param deleted 删除标记，业务侧通常传 {@code 0}
     */
    Optional<SysRole> findByIdAndDeleted(Long id, Integer deleted);

    /**
     * 按角色编码查询（更新时判重、按码定位）。
     *
     * @param code    角色编码
     * @param deleted 删除标记
     * @return 角色实体
     */
    Optional<SysRole> findByCodeAndDeleted(String code, Integer deleted);

    /**
     * 编码是否已存在（创建时唯一性校验）。
     *
     * @param code    角色编码
     * @param deleted 删除标记
     * @return 是否存在
     */
    boolean existsByCodeAndDeleted(String code, Integer deleted);
}

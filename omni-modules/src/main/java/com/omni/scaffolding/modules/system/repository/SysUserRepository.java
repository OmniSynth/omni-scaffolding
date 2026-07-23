package com.omni.scaffolding.modules.system.repository;

import com.omni.scaffolding.modules.system.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 用户 JPA Repository。
 *
 * <p>负责用户主表写路径与按用户名 / 手机 / 邮箱的唯一性查询；
 * 详情联查、搜索、角色岗位关联维护走 MyBatis（{@code SysUserQueryMapper}）。
 */
public interface SysUserRepository extends JpaRepository<SysUser, Long> {

    /**
     * 按登录用户名查询（含删除标记过滤）。
     *
     * @param username 登录名
     * @param deleted  删除标记，业务侧通常传 {@code 0}
     */
    Optional<SysUser> findByUsernameAndDeleted(String username, Integer deleted);

    /**
     * 按手机号查询（唯一性校验）。
     */
    Optional<SysUser> findByMobileAndDeleted(String mobile, Integer deleted);

    /**
     * 按邮箱查询（唯一性校验）。
     */
    Optional<SysUser> findByEmailAndDeleted(String email, Integer deleted);
}

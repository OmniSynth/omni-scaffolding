package com.omni.scaffolding.modules.system.mapper;

import com.omni.scaffolding.modules.system.dto.role.RoleView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色复杂读 / 角色-菜单关联维护 Mapper（MyBatis）。
 *
 * <p>对应 XML：{@code classpath:mapper/system/SysRoleQueryMapper.xml}。
 * 角色主表 CRUD 走 JPA（{@code SysRoleRepository}）；本接口负责列表读模型、
 * 菜单授权关联表维护，以及绑定用户数统计。
 */
@Mapper
public interface SysRoleQueryMapper {

    /**
     * 未删除角色总数。
     *
     * @return 总数
     */
    long countRoles();

    /**
     * 分页列出未删除角色（含 dataScope、status、userCount，并通过嵌套查询填充 menuIds）。
     *
     * @param limit  每页条数
     * @param offset 偏移量
     * @return 读模型列表
     */
    List<RoleView> listRoles(@Param("limit") long limit, @Param("offset") long offset);

    /**
     * 按角色 ID 查询读模型；不存在或已删除返回 {@code null}。
     *
     * @param roleId 角色主键
     * @return 读模型，不存在则为 {@code null}
     */
    RoleView findById(@Param("roleId") Long roleId);

    /**
     * 查询角色已分配的菜单（含按钮）ID 列表。
     *
     * @param roleId 角色主键
     * @return 菜单 ID 列表，可能为空
     */
    List<Long> findMenuIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 清空角色的全部菜单授权（全量替换前调用）。
     *
     * @param roleId 角色主键
     */
    void deleteRoleMenus(@Param("roleId") Long roleId);

    /**
     * 写入一条角色-菜单关联。
     *
     * @param roleId 角色主键
     * @param menuId 菜单主键
     */
    void insertRoleMenu(@Param("roleId") Long roleId, @Param("menuId") Long menuId);

    /**
     * 统计仍绑定该角色的未删除用户数（删角色前校验）。
     *
     * @param roleId 角色主键
     * @return 用户数
     */
    long countUsersByRoleId(@Param("roleId") Long roleId);
}


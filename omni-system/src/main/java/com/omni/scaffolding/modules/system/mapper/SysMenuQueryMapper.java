package com.omni.scaffolding.modules.system.mapper;

import com.omni.scaffolding.modules.system.dto.MenuTreeNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 菜单复杂读 Mapper（MyBatis）。
 *
 * <p>对应 XML：{@code classpath:mapper/system/SysMenuQueryMapper.xml}。
 * 菜单主表写入走 JPA（{@code SysMenuRepository}）；本接口返回扁平节点，由服务层组树。
 */
@Mapper
public interface SysMenuQueryMapper {

    /**
     * 全部未删除菜单（含 BUTTON），供管理端组树与角色授权勾选。
     */
    List<MenuTreeNode> listAll();

    /**
     * 当前用户侧栏菜单：经角色授权，且菜单可见、启用的 DIR/MENU（不含 BUTTON）。
     *
     * <p>仅统计启用中的角色（{@code sys_role.status = 1}）。
     *
     * @param userId 用户主键
     */
    List<MenuTreeNode> listSidebarMenusByUserId(@Param("userId") Long userId);
}

package com.omni.scaffolding.modules.system.mapper;

import com.omni.scaffolding.modules.system.dto.UserAuthView;
import com.omni.scaffolding.modules.system.dto.UserDetailView;
import com.omni.scaffolding.security.datascope.DataScopeQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户复杂查询 / 关联表维护 Mapper（MyBatis）。
 *
 * <p>对应 XML：{@code classpath:mapper/system/SysUserQueryMapper.xml}。
 * 用户主表写入走 JPA（{@code SysUserRepository}）；本接口负责：
 * <ul>
 *   <li>登录与动态权限所需的认证 / 角色 / 权限 / 数据范围读模型</li>
 *   <li>用户详情与数据范围过滤搜索</li>
 *   <li>{@code sys_user_role} / {@code sys_user_post} 关联维护</li>
 * </ul>
 */
@Mapper
public interface SysUserQueryMapper {

    /**
     * 登录用：按用户名取认证字段（密码哈希、启用状态、部门等）。
     *
     * @param username 登录名
     * @return 认证读模型，不存在则为 {@code null}
     */
    UserAuthView findAuthViewByUsername(@Param("username") String username);

    /**
     * 动态权限用：按用户 ID 取认证字段。
     *
     * @param userId 用户主键
     * @return 认证读模型，不存在则为 {@code null}
     */
    UserAuthView findAuthViewById(@Param("userId") Long userId);

    /**
     * 用户有效角色编码列表（仅启用且未删除的角色）。
     *
     * @param userId 用户主键
     * @return 角色编码列表，可能为空
     */
    List<String> findRoleCodesByUserId(@Param("userId") Long userId);

    /**
     * 用户所属启用角色的 {@code data_scope} 列表，供合并最宽松范围。
     *
     * @param userId 用户主键
     * @return 数据范围列表，可能为空
     */
    List<String> findDataScopesByUserId(@Param("userId") Long userId);

    /**
     * 权限码去重列表：启用角色 → 角色菜单 → 启用菜单的 {@code perms}。
     *
     * @param userId 用户主键
     * @return 权限码列表，可能为空
     */
    List<String> findPermissionCodesByUserId(@Param("userId") Long userId);

    /**
     * 用户已绑定角色 ID 列表（含停用角色，便于编辑回填）。
     *
     * @param userId 用户主键
     * @return 角色 ID 列表，可能为空
     */
    List<Long> findRoleIdsByUserId(@Param("userId") Long userId);

    /**
     * 用户已绑定角色名称列表（列表展示用中文名）。
     *
     * @param userId 用户主键
     * @return 角色名称列表，可能为空
     */
    List<String> findRoleNamesByUserId(@Param("userId") Long userId);

    /**
     * 用户已绑定岗位 ID 列表。
     *
     * @param userId 用户主键
     * @return 岗位 ID 列表，可能为空
     */
    List<Long> findPostIdsByUserId(@Param("userId") Long userId);

    /**
     * 用户已绑定岗位名称列表（按排序）。
     *
     * @param userId 用户主键
     * @return 岗位名称列表，可能为空
     */
    List<String> findPostNamesByUserId(@Param("userId") Long userId);

    /**
     * 用户详情读模型：部门名 + 岗位 + 角色 + 权限（嵌套 / 关联查询）。
     *
     * @param userId 用户主键
     * @return 详情读模型，不存在则为 {@code null}
     */
    UserDetailView findUserDetail(@Param("userId") Long userId);

    /**
     * 关键字搜索用户总数（含数据范围过滤）。
     *
     * @param keyword 可选，匹配用户名 / 昵称 / 姓名 / 手机 / 邮箱
     * @param ds      当前登录用户数据范围参数
     * @return 总数
     */
    long countUsers(@Param("keyword") String keyword, @Param("ds") DataScopeQuery ds);

    /**
     * 关键字分页搜索用户，并按 {@code ds} 做数据范围过滤。
     *
     * @param keyword 可选，匹配用户名 / 昵称 / 姓名 / 手机 / 邮箱
     * @param ds      当前登录用户数据范围参数
     * @param limit   每页条数
     * @param offset  偏移量
     * @return 读模型列表
     */
    List<UserDetailView> searchUsers(@Param("keyword") String keyword,
                                     @Param("ds") DataScopeQuery ds,
                                     @Param("limit") long limit,
                                     @Param("offset") long offset);

    /**
     * 统计部门下未删除用户数（删部门前校验）。
     *
     * @param deptId 部门主键
     * @return 用户数
     */
    long countByDeptId(@Param("deptId") Long deptId);

    /**
     * 清空用户全部角色关联（全量替换前调用）。
     *
     * @param userId 用户主键
     */
    void deleteUserRoles(@Param("userId") Long userId);

    /**
     * 写入一条用户-角色关联。
     *
     * @param userId 用户主键
     * @param roleId 角色主键
     */
    void insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    /**
     * 清空用户全部岗位关联（全量替换前调用）。
     *
     * @param userId 用户主键
     */
    void deleteUserPosts(@Param("userId") Long userId);

    /**
     * 写入一条用户-岗位关联。
     *
     * @param userId 用户主键
     * @param postId 岗位主键
     */
    void insertUserPost(@Param("userId") Long userId, @Param("postId") Long postId);
}

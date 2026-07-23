package com.omni.scaffolding.modules.system.entity;

import com.omni.scaffolding.common.persistence.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 系统用户实体（JPA 写模型）。
 *
 * <p>复杂读（含角色权限、部门名称、岗位）请用 {@code SysUserQueryMapper} + {@code UserDetailView}，不要在此硬拼关联。
 */
@Getter
@Setter
@Entity
@Table(name = "sys_user")
public class SysUser extends BaseAuditableEntity {

    /**
     * 主键 ID（脚手架演示用应用侧发号，生产可换雪花 / 号段）。
     */
    @Id
    private Long id;

    /**
     * 登录用户名，全局唯一。
     */
    @Column(nullable = false, unique = true, length = 64)
    private String username;

    /**
     * 密码哈希（BCrypt），禁止存明文。
     */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /**
     * 显示昵称。
     */
    @Column(nullable = false, length = 64)
    private String nickname;

    /**
     * 真实姓名。
     */
    @Column(name = "real_name", length = 64)
    private String realName;

    /**
     * 手机号，唯一（可空）。
     */
    @Column(length = 32)
    private String mobile;

    /**
     * 邮箱，唯一（可空）。
     */
    @Column(length = 128)
    private String email;

    /**
     * 性别：UNKNOWN / MALE / FEMALE。
     */
    @Column(nullable = false, length = 16)
    private String gender = "UNKNOWN";

    /**
     * 头像访问路径，如 {@code /uploads/avatars/xxx.png}。
     */
    @Column(length = 512)
    private String avatar;

    /**
     * 所属部门 ID，关联 {@code sys_dept.id}；数据范围过滤依赖此字段。
     */
    @Column(name = "dept_id", nullable = false)
    private Long deptId;

    /**
     * 是否启用；false 时禁止登录。
     */
    @Column(nullable = false)
    private Boolean enabled = true;

    /**
     * 逻辑删除标记：0=正常，1=已删除。
     */
    @Column(nullable = false)
    private Integer deleted = 0;
}

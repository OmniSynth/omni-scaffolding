package com.omni.scaffolding.modules.system.entity;

import com.omni.scaffolding.common.persistence.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * IP 白名单实体（JPA 写模型）。
 *
 * <p>对应表 {@code sys_ip_whitelist}；启用记录供 {@code @IpWhitelist} 校验合并使用。
 */
@Getter
@Setter
@Entity
@Table(name = "sys_ip_whitelist")
public class SysIpWhitelist extends BaseAuditableEntity {

    /**
     * 主键。
     */
    @Id
    private Long id;

    /**
     * IP 地址（IPv4 / IPv6），表内唯一。
     */
    @Column(name = "ip_addr", nullable = false, length = 64)
    private String ipAddr;

    /**
     * 备注说明。
     */
    @Column(length = 255)
    private String remark;

    /**
     * 是否启用：{@code true} 参与校验，{@code false} 停用。
     */
    @Column(nullable = false)
    private Boolean status = true;

    /**
     * 逻辑删除：0 正常，1 已删除。
     */
    @Column(nullable = false)
    private Integer deleted = 0;
}

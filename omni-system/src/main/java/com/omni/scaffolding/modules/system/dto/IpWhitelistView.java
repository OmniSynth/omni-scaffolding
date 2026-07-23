package com.omni.scaffolding.modules.system.dto;

import lombok.Data;

import java.time.Instant;

/**
 * IP 白名单读模型（管理端列表 / 详情）。
 */
@Data
public class IpWhitelistView {

    /** 主键。 */
    private Long id;

    /** IP 地址。 */
    private String ipAddr;

    /** 备注。 */
    private String remark;

    /** 是否启用。 */
    private Boolean status;

    /** 创建时间。 */
    private Instant createdAt;

    /** 最后更新时间。 */
    private Instant updatedAt;
}

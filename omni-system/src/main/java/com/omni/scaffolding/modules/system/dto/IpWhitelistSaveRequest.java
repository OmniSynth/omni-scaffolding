package com.omni.scaffolding.modules.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * IP 白名单新增 / 修改请求体。
 */
@Data
public class IpWhitelistSaveRequest {

    /**
     * IP 地址（IPv4 / IPv6），必填。
     */
    @NotBlank(message = "IP 不能为空")
    @Size(max = 64)
    private String ipAddr;

    /**
     * 备注，可空。
     */
    @Size(max = 255)
    private String remark;

    /**
     * 是否启用，默认启用。
     */
    @NotNull
    private Boolean status = true;
}

package com.omni.scaffolding.modules.open.dto.endpoint;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建 / 修改开放接口目录请求。
 */
@Data
public class OpenEndpointSaveRequest {

    /**
     * 接口编码，未删除范围内唯一。
     */
    @NotBlank(message = "接口编码不能为空")
    @Size(max = 64, message = "接口编码长度不能超过 64")
    private String code;

    /**
     * 接口名称。
     */
    @NotBlank(message = "接口名称不能为空")
    @Size(max = 128, message = "接口名称长度不能超过 128")
    private String name;

    /**
     * HTTP 方法，如 GET；{@code *} 表示任意。
     */
    @NotBlank(message = "HTTP 方法不能为空")
    @Size(max = 16, message = "HTTP 方法长度不能超过 16")
    private String httpMethod;

    /**
     * Ant 风格路径模式。
     */
    @NotBlank(message = "路径模式不能为空")
    @Size(max = 255, message = "路径模式长度不能超过 255")
    private String pathPattern;

    /**
     * 备注。
     */
    @Size(max = 255, message = "备注长度不能超过 255")
    private String remark;

    /**
     * 是否启用。
     */
    @NotNull(message = "状态不能为空")
    private Boolean status;
}

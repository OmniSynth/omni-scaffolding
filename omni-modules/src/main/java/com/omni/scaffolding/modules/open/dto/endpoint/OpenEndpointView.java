package com.omni.scaffolding.modules.open.dto.endpoint;

import lombok.Data;

/**
 * 开放接口目录读模型。
 */
@Data
public class OpenEndpointView {

    /**
     * 主键。
     */
    private Long id;

    /**
     * 接口编码。
     */
    private String code;

    /**
     * 接口名称。
     */
    private String name;

    /**
     * HTTP 方法（含 {@code *}）。
     */
    private String httpMethod;

    /**
     * Ant 路径模式。
     */
    private String pathPattern;

    /**
     * 备注。
     */
    private String remark;

    /**
     * 是否启用。
     */
    private Boolean status;
}

package com.omni.scaffolding.modules.system.dto;

import lombok.Data;

/**
 * 岗位读模型。
 */
@Data
public class PostView {

    /**
     * 岗位 ID。
     */
    private Long id;

    /**
     * 岗位编码。
     */
    private String code;

    /**
     * 岗位名称。
     */
    private String name;

    /**
     * 排序。
     */
    private Integer sort;

    /**
     * 是否启用。
     */
    private Boolean status;
}

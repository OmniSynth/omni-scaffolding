package com.omni.scaffolding.modules.ops.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 删除索引请求。
 */
@Data
public class MysqlDropIndexRequest {

    /**
     * 表名，必填。
     */
    @NotBlank(message = "表名不能为空")
    @Size(max = 64, message = "表名长度不能超过 64")
    private String table;

    /**
     * 索引名，必填。
     */
    @NotBlank(message = "索引名不能为空")
    @Size(max = 64, message = "索引名长度不能超过 64")
    private String name;
}

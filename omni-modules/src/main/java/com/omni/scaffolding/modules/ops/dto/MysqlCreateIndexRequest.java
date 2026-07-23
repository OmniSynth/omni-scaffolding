package com.omni.scaffolding.modules.ops.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 创建索引请求。
 */
@Data
public class MysqlCreateIndexRequest {

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

    /**
     * 索引列，必填。
     */
    @NotEmpty(message = "索引列不能为空")
    @Size(max = 16, message = "索引列最多 16 个")
    private List<@NotBlank(message = "列名不能为空") @Size(max = 64) String> columns;

    /**
     * 是否唯一索引。
     */
    private boolean unique;
}

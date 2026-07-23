package com.omni.scaffolding.modules.tool.gen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 代码生成预览文件。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenFileView {

    /**
     * ZIP 内相对路径。
     */
    private String path;

    /**
     * 文件内容。
     */
    private String content;
}

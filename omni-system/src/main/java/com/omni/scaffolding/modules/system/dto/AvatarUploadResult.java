package com.omni.scaffolding.modules.system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 头像上传结果。
 */
@Data
@AllArgsConstructor
public class AvatarUploadResult {

    /**
     * 可访问的相对 URL，如 {@code /uploads/avatars/xxx.png}。
     */
    private String url;
}

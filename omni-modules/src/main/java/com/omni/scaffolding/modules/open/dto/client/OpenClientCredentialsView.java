package com.omni.scaffolding.modules.open.dto.client;

import lombok.Data;

/**
 * 创建 / 重置密钥后一次性返回的明文凭证。
 *
 * <p>关闭弹窗后服务端不再下发明文，请妥善保存。
 */
@Data
public class OpenClientCredentialsView {

    /**
     * 客户端主键。
     */
    private Long id;

    /**
     * 客户端名称。
     */
    private String name;

    /**
     * 公开 AccessKey。
     */
    private String accessKey;

    /**
     * 明文 API Key（请求头 {@code X-Api-Key}），仅此时返回。
     */
    private String apiKey;

    /**
     * 明文 AccessSecret（二期签名预留），仅此时返回。
     */
    private String accessSecret;
}

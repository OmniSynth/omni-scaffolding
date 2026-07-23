package com.omni.scaffolding.security.captcha;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录验证码挑战（关闭时仅 {@code enabled=false}）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaptchaChallenge {

    /**
     * 是否启用验证码。
     */
    private boolean enabled;

    /**
     * 验证码 ID，登录时回传。
     */
    private String captchaId;

    /**
     * PNG 的 data URL（{@code data:image/png;base64,...}）。
     */
    private String imageBase64;

    public static CaptchaChallenge disabled() {
        return new CaptchaChallenge(false, null, null);
    }
}

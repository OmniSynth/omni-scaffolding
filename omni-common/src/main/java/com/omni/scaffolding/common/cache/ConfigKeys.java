package com.omni.scaffolding.common.cache;

/**
 * 系统参数键常量（对应 {@code sys_config.config_key}）。
 *
 * <p>禁止在业务代码中书写魔法字符串，统一引用本类常量。
 */
public final class ConfigKeys {

    /**
     * 新建用户 / 重置密码时的默认初始密码提示值。
     */
    public static final String ACCOUNT_INIT_PASSWORD = "sys.account.initPassword";

    /**
     * 用户未上传头像时的默认地址。
     */
    public static final String USER_DEFAULT_AVATAR = "sys.user.defaultAvatar";

    /**
     * 浏览器标题 / 登录页展示名。
     */
    public static final String UI_TITLE = "sys.ui.title";

    /**
     * 前端是否展示水印。
     *
     * <p>取值：{@code true} 展示，{@code false} 关闭（大小写不敏感）。
     */
    public static final String UI_WATERMARK = "sys.ui.watermark";

    private ConfigKeys() {
    }
}

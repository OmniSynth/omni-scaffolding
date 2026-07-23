package com.omni.scaffolding.security.captcha;

import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.cache.RedisKeys;
import com.omni.scaffolding.common.exception.BusinessException;
import com.omni.scaffolding.config.OmniSecurityProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;

/**
 * 登录图形验证码：发挑战写入 Redis，登录时一次性校验并删除。
 */
@Service
@RequiredArgsConstructor
public class CaptchaService {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final OmniSecurityProperties securityProperties;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 是否启用验证码。
     */
    public boolean isEnabled() {
        OmniSecurityProperties.Captcha cfg = securityProperties.getCaptcha();
        return cfg != null && cfg.isEnabled();
    }

    /**
     * 签发验证码挑战；未启用时返回 {@link CaptchaChallenge#disabled()}。
     */
    public CaptchaChallenge createChallenge() {
        if (!isEnabled()) {
            return CaptchaChallenge.disabled();
        }
        OmniSecurityProperties.Captcha cfg = securityProperties.getCaptcha();
        int len = Math.max(4, Math.min(8, cfg.getLength()));
        String code = randomCode(len);
        String captchaId = UUID.randomUUID().toString().replace("-", "");
        int ttl = Math.max(30, cfg.getTtlSeconds());
        stringRedisTemplate.opsForValue().set(
                RedisKeys.loginCaptcha(captchaId),
                code.toLowerCase(Locale.ROOT),
                Duration.ofSeconds(ttl));
        String image = renderPngDataUrl(code);
        return new CaptchaChallenge(true, captchaId, image);
    }

    /**
     * 校验并消费验证码；未启用时直接放行。
     *
     * @param captchaId 挑战 ID
     * @param answer    用户输入
     */
    public void verifyAndConsume(String captchaId, String answer) {
        if (!isEnabled()) {
            return;
        }
        if (!StringUtils.hasText(captchaId) || !StringUtils.hasText(answer)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请输入验证码");
        }
        String key = RedisKeys.loginCaptcha(captchaId.trim());
        String expected = stringRedisTemplate.opsForValue().get(key);
        stringRedisTemplate.delete(key);
        if (!StringUtils.hasText(expected)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "验证码已过期，请刷新");
        }
        if (!expected.equalsIgnoreCase(answer.trim())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "验证码错误");
        }
    }

    private static String randomCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

    private static String renderPngDataUrl(String code) {
        int width = 120;
        int height = 40;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(248, 250, 252));
            g.fillRect(0, 0, width, height);
            for (int i = 0; i < 6; i++) {
                g.setColor(new Color(148 + RANDOM.nextInt(80), 163 + RANDOM.nextInt(60), 184 + RANDOM.nextInt(40)));
                g.drawLine(RANDOM.nextInt(width), RANDOM.nextInt(height),
                        RANDOM.nextInt(width), RANDOM.nextInt(height));
            }
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
            int charWidth = width / (code.length() + 1);
            for (int i = 0; i < code.length(); i++) {
                g.setColor(new Color(15 + RANDOM.nextInt(40), 23 + RANDOM.nextInt(40), 42 + RANDOM.nextInt(40)));
                int x = charWidth * (i + 1) - 8;
                int y = 28 + RANDOM.nextInt(5);
                g.drawString(String.valueOf(code.charAt(i)), x, y);
            }
        } finally {
            g.dispose();
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", out);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "验证码生成失败");
        }
    }
}

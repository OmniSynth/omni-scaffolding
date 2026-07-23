package com.omni.scaffolding.security.password;

import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.exception.BusinessException;
import com.omni.scaffolding.config.OmniSecurityProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 按 {@link OmniSecurityProperties.PasswordPolicy} 校验明文密码。
 */
@Component
@RequiredArgsConstructor
public class PasswordPolicyValidator {

    private final OmniSecurityProperties securityProperties;

    /**
     * 校验密码；不通过抛 {@link BusinessException}。
     *
     * @param password 明文密码
     */
    public void validate(String password) {
        OmniSecurityProperties.PasswordPolicy policy = securityProperties.getPasswordPolicy();
        if (policy == null) {
            return;
        }
        if (!StringUtils.hasText(password)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "密码不能为空");
        }
        int len = password.length();
        int min = Math.max(1, policy.getMinLength());
        int max = Math.max(min, policy.getMaxLength());
        if (len < min || len > max) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "密码长度须在 " + min + "~" + max + " 之间");
        }
        if (policy.isRequireUppercase() && password.chars().noneMatch(Character::isUpperCase)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "密码须包含大写字母");
        }
        if (policy.isRequireLowercase() && password.chars().noneMatch(Character::isLowerCase)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "密码须包含小写字母");
        }
        if (policy.isRequireDigit() && password.chars().noneMatch(Character::isDigit)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "密码须包含数字");
        }
        if (policy.isRequireSpecial() && password.chars().allMatch(ch -> Character.isLetterOrDigit(ch))) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "密码须包含特殊字符");
        }
    }

    public boolean isForceChangeOnCreate() {
        OmniSecurityProperties.PasswordPolicy policy = securityProperties.getPasswordPolicy();
        return policy != null && policy.isForceChangeOnCreate();
    }

    public boolean isForceChangeOnReset() {
        OmniSecurityProperties.PasswordPolicy policy = securityProperties.getPasswordPolicy();
        return policy != null && policy.isForceChangeOnReset();
    }
}

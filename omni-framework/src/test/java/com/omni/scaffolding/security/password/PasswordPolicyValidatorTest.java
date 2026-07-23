package com.omni.scaffolding.security.password;

import com.omni.scaffolding.config.OmniSecurityProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordPolicyValidatorTest {

    private OmniSecurityProperties properties;
    private PasswordPolicyValidator validator;

    @BeforeEach
    void setUp() {
        properties = new OmniSecurityProperties();
        validator = new PasswordPolicyValidator(properties);
    }

    @Test
    void defaultPolicyAcceptsAdmin123() {
        assertThatCode(() -> validator.validate("admin123")).doesNotThrowAnyException();
    }

    @Test
    void rejectsTooShort() {
        properties.getPasswordPolicy().setMinLength(8);
        assertThatThrownBy(() -> validator.validate("abc12"))
                .hasMessageContaining("密码长度");
    }

    @Test
    void requiresDigitWhenConfigured() {
        properties.getPasswordPolicy().setRequireDigit(true);
        assertThatThrownBy(() -> validator.validate("abcdef"))
                .hasMessageContaining("数字");
        assertThatCode(() -> validator.validate("abcdef1")).doesNotThrowAnyException();
    }

    @Test
    void requiresUpperWhenConfigured() {
        properties.getPasswordPolicy().setRequireUppercase(true);
        assertThatThrownBy(() -> validator.validate("admin123"))
                .hasMessageContaining("大写");
        assertThatCode(() -> validator.validate("Admin123")).doesNotThrowAnyException();
    }
}

package com.omni.scaffolding.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProdDeployConfigCheckerTest {

    @Test
    void evaluatesAllItemsAndReportsEveryFailureAtOnce() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");
        env.setProperty("OMNI_JWT_SECRET", "123");
        env.setProperty("OMNI_SIGN_SECRET", "short");
        env.setProperty("OMNI_ADMIN_INITIAL_PASSWORD", "tooshort");
        // CORS 故意不设

        List<ProdDeployConfigChecker.CheckItem> items = ProdDeployConfigChecker.evaluate(env);
        String report = ProdDeployConfigChecker.formatReport(items);

        assertThat(items).hasSize(4);
        assertThat(items.stream().filter(i -> !i.ok()).count()).isEqualTo(4);
        assertThat(report)
                .contains("[FAIL] OMNI_JWT_SECRET")
                .contains("[FAIL] OMNI_SIGN_SECRET")
                .contains("[FAIL] OMNI_ADMIN_INITIAL_PASSWORD")
                .contains("[FAIL] OMNI_CORS_ORIGINS")
                .contains("失败 4 项");
    }

    @Test
    void admin123AllowedWhenLengthRuleSatisfied() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");
        env.setProperty("OMNI_JWT_SECRET", "vWcunHYLV2yGl9xQsjbpBJk8MRT0N7Sg");
        env.setProperty("OMNI_SIGN_SECRET", "sign-secret-ok");
        // 仅长度规则：12 位即可，无 admin123 黑名单
        env.setProperty("OMNI_ADMIN_INITIAL_PASSWORD", "admin1234567");
        env.setProperty("OMNI_CORS_ORIGINS", "https://example.com");

        List<ProdDeployConfigChecker.CheckItem> items = ProdDeployConfigChecker.evaluate(env);
        assertThat(items.stream().filter(i -> !i.ok())).isEmpty();
    }

    @Test
    void requireValidOrThrowUsesShortExceptionMessage() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");
        env.setProperty("OMNI_JWT_SECRET", "123");
        env.setProperty("OMNI_SIGN_SECRET", "sign-ok-8");
        env.setProperty("OMNI_ADMIN_INITIAL_PASSWORD", "short");
        env.setProperty("OMNI_CORS_ORIGINS", "https://example.com");

        assertThatThrownBy(() -> ProdDeployConfigChecker.requireValidOrThrow(env))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("共 2 项不合格")
                .hasMessageContaining("详见上方清单")
                .hasMessageNotContaining("[FAIL]");
    }

    @Test
    void detectsProdFromArgsWhenActiveProfilesEmpty() {
        MockEnvironment env = new MockEnvironment();
        assertThat(ProdDeployConfigChecker.isProd(env, "--spring.profiles.active=prod")).isTrue();
        assertThat(ProdDeployConfigChecker.isProd(env, "--spring.profiles.active=dev")).isFalse();
    }
}

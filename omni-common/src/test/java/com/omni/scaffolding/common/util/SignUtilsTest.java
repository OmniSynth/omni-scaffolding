package com.omni.scaffolding.common.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SignUtilsTest {

    @Test
    void hmacIsStableAndCaseSensitiveHex() {
        String payload = SignUtils.loginPayload("1710000000000", "nonce-1", "admin", "admin123");
        String a = SignUtils.hmacSha256Hex("secret", payload);
        String b = SignUtils.hmacSha256Hex("secret", payload);
        assertThat(a).isEqualTo(b);
        assertThat(a).matches("[0-9a-f]{64}");
        assertThat(SignUtils.equalsHex(a, b)).isTrue();
        assertThat(SignUtils.equalsHex(a, "deadbeef")).isFalse();
    }
}

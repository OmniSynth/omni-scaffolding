package com.omni.scaffolding.security;

import com.omni.scaffolding.config.OmniSecurityProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        OmniSecurityProperties properties = new OmniSecurityProperties();
        properties.getJwt().setSecret("test-secret-key-with-at-least-32-bytes!!");
        properties.getJwt().setExpirationMs(3_600_000L);
        jwtService = new JwtService(properties);
    }

    @Test
    void generateAndValidateToken() {
        IssuedToken issued = jwtService.generateToken(
                "admin", 1L, 1L, "ALL", List.of("ADMIN"), List.of("demo:product:read"));
        String token = issued.accessToken();
        assertThat(token).isNotBlank();
        assertThat(issued.jti()).isNotBlank();
        assertThat(issued.expireAt()).isAfter(java.time.Instant.now());
        assertThat(jwtService.isValid(token)).isTrue();
        assertThat(jwtService.parseClaims(token).getSubject()).isEqualTo("admin");
        assertThat(jwtService.parseClaims(token).getId()).isEqualTo(issued.jti());
        assertThat(jwtService.parseClaims(token).get("uid", Long.class)).isEqualTo(1L);
        assertThat(jwtService.parseClaims(token).get("deptId", Long.class)).isEqualTo(1L);
        assertThat(jwtService.parseClaims(token).get("dataScope", String.class)).isEqualTo("ALL");
    }
}

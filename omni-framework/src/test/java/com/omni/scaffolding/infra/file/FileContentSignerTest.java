package com.omni.scaffolding.infra.file;

import com.omni.scaffolding.config.OmniFileProperties;
import com.omni.scaffolding.config.OmniSecurityProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileContentSignerTest {

    private FileContentSigner signer;

    @BeforeEach
    void setUp() {
        OmniFileProperties fileProperties = new OmniFileProperties();
        fileProperties.setSignSecret("file-sign-secret-at-least-32-bytes!!");
        fileProperties.setSignTtlSeconds(600);
        OmniSecurityProperties securityProperties = new OmniSecurityProperties();
        securityProperties.getJwt().setSecret("jwt-fallback-secret-at-least-32-bytes!");
        signer = new FileContentSigner(fileProperties, securityProperties);
    }

    @Test
    void verifyAcceptsValidSignature() {
        long fileId = 1001L;
        long expire = signer.defaultExpireEpoch();
        String sign = signer.sign(fileId, expire);
        assertTrue(signer.verify(fileId, expire, sign));
    }

    @Test
    void verifyRejectsExpiredOrTampered() {
        long fileId = 1001L;
        long expire = System.currentTimeMillis() / 1000 - 10;
        String sign = signer.sign(fileId, expire + 1000);
        assertFalse(signer.verify(fileId, expire, sign));
        assertFalse(signer.verify(fileId, signer.defaultExpireEpoch(), "deadbeef"));
        assertFalse(signer.verify(fileId, null, sign));
    }
}

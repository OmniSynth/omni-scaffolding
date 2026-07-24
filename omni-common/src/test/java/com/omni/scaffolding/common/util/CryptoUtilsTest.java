package com.omni.scaffolding.common.util;

import org.junit.jupiter.api.Test;

import java.security.KeyPair;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CryptoUtilsTest {

    @Test
    void digestAlgorithmsAreStable() {
        assertThat(DigestUtils.md5Hex("omni")).isEqualTo(DigestUtils.md5Hex("omni")).hasSize(32);
        assertThat(DigestUtils.sha1Hex("omni")).hasSize(40);
        assertThat(DigestUtils.sha256Hex("omni")).hasSize(64);
        assertThat(DigestUtils.sha512Hex("omni")).hasSize(128);
        assertThat(DigestUtils.md5Hex(null)).isEqualTo(DigestUtils.md5Hex(""));
    }

    @Test
    void aesGcmRoundTrip() {
        String key = "0123456789abcdef"; // 16 bytes AES-128
        String cipher = AesUtils.encryptGcm("你好 Omni", key);
        assertThat(AesUtils.decryptGcm(cipher, key)).isEqualTo("你好 Omni");
        assertThat(AesUtils.encryptGcm("你好 Omni", key)).isNotEqualTo(cipher);
    }

    @Test
    void aesCbcRoundTripWithDerivedKey() {
        byte[] key = AesUtils.deriveKeySha256("passphrase");
        String cipher = AesUtils.encryptCbc("payload", key);
        assertThat(new String(AesUtils.decryptCbcToBytes(cipher, key))).isEqualTo("payload");
    }

    @Test
    void aesRejectsBadKeyLength() {
        assertThatThrownBy(() -> AesUtils.encryptGcm("x", "short"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("16, 24 or 32");
    }

    @Test
    void rsaEncryptDecryptAndSign() {
        KeyPair pair = RsaUtils.generateKeyPair();
        String pub = RsaUtils.encodePublicKey(pair.getPublic());
        String pri = RsaUtils.encodePrivateKey(pair.getPrivate());

        String cipher = RsaUtils.encrypt("secret-data", pub);
        assertThat(RsaUtils.decrypt(cipher, pri)).isEqualTo("secret-data");

        String signature = RsaUtils.sign("hello", pri);
        assertThat(RsaUtils.verify("hello", signature, pub)).isTrue();
        assertThat(RsaUtils.verify("tampered", signature, pub)).isFalse();
    }

    @Test
    void hmacMatchesSignUtilsSha256() {
        String payload = "a\nb\nc";
        assertThat(HmacUtils.sha256Hex("secret", payload))
                .isEqualTo(SignUtils.hmacSha256Hex("secret", payload));
        assertThat(HmacUtils.sha512Hex("secret", payload)).hasSize(128);
    }

    @Test
    void generateAesKeyBase64() {
        String key = AesUtils.generateKeyBase64(256);
        byte[] raw = java.util.Base64.getDecoder().decode(key);
        assertThat(raw).hasSize(32);
        String cipher = AesUtils.encryptGcm("ok", raw);
        assertThat(new String(AesUtils.decryptGcmToBytes(cipher, raw))).isEqualTo("ok");
    }
}

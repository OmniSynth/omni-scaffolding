package com.omni.scaffolding.common.util;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * RSA 非对称加解密与签名工具。
 *
 * <p>加解密默认 {@code RSA/ECB/OAEPWithSHA-256AndMGF1Padding}；签名默认 {@code SHA256withRSA}。
 * 密钥以 Base64（无 PEM 头）交换：公钥为 X.509，私钥为 PKCS#8。
 *
 * <p>RSA 加密适合短数据（密钥交换、小密文）；大文件请用 AES 加密内容，再用 RSA 加密 AES 密钥。
 */
public final class RsaUtils {

    public static final String TRANSFORMATION_OAEP = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    public static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    private static final int DEFAULT_KEY_SIZE = 2048;

    private RsaUtils() {
    }

    /**
     * 生成 RSA 密钥对（默认 2048 位）。
     *
     * @return 密钥对
     */
    public static KeyPair generateKeyPair() {
        return generateKeyPair(DEFAULT_KEY_SIZE);
    }

    /**
     * 生成 RSA 密钥对。
     *
     * @param keySize 建议 ≥ 2048
     * @return 密钥对
     */
    public static KeyPair generateKeyPair(int keySize) {
        if (keySize < 2048) {
            throw new IllegalArgumentException("RSA keySize should be >= 2048");
        }
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(keySize);
            return generator.generateKeyPair();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("generate RSA key pair failed", ex);
        }
    }

    /**
     * 公钥编码为 Base64（X.509）。
     *
     * @param publicKey 公钥
     * @return Base64
     */
    public static String encodePublicKey(PublicKey publicKey) {
        require(publicKey, "publicKey");
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    /**
     * 私钥编码为 Base64（PKCS#8）。
     *
     * @param privateKey 私钥
     * @return Base64
     */
    public static String encodePrivateKey(PrivateKey privateKey) {
        require(privateKey, "privateKey");
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    /**
     * 从 Base64（X.509）还原公钥。
     *
     * @param base64 公钥
     * @return {@link PublicKey}
     */
    public static PublicKey decodePublicKey(String base64) {
        try {
            byte[] encoded = Base64.getDecoder().decode(requireBase64(base64, "publicKey"));
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(encoded));
        } catch (GeneralSecurityException | IllegalArgumentException ex) {
            throw new IllegalArgumentException("invalid RSA public key", ex);
        }
    }

    /**
     * 从 Base64（PKCS#8）还原私钥。
     *
     * @param base64 私钥
     * @return {@link PrivateKey}
     */
    public static PrivateKey decodePrivateKey(String base64) {
        try {
            byte[] encoded = Base64.getDecoder().decode(requireBase64(base64, "privateKey"));
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(encoded));
        } catch (GeneralSecurityException | IllegalArgumentException ex) {
            throw new IllegalArgumentException("invalid RSA private key", ex);
        }
    }

    /**
     * 公钥加密，返回 Base64 密文。
     *
     * @param plainText       明文（不宜过长）
     * @param publicKeyBase64 Base64 公钥
     * @return Base64 密文
     */
    public static String encrypt(String plainText, String publicKeyBase64) {
        return encrypt(bytes(plainText), decodePublicKey(publicKeyBase64));
    }

    /**
     * 公钥加密，返回 Base64 密文。
     *
     * @param plain     明文字节
     * @param publicKey 公钥
     * @return Base64 密文
     */
    public static String encrypt(byte[] plain, PublicKey publicKey) {
        require(publicKey, "publicKey");
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION_OAEP);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(plain == null ? new byte[0] : plain));
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("RSA encrypt failed", ex);
        }
    }

    /**
     * 私钥解密。
     *
     * @param cipherTextBase64 Base64 密文
     * @param privateKeyBase64 Base64 私钥
     * @return UTF-8 明文
     */
    public static String decrypt(String cipherTextBase64, String privateKeyBase64) {
        return new String(decryptToBytes(cipherTextBase64, decodePrivateKey(privateKeyBase64)), StandardCharsets.UTF_8);
    }

    /**
     * 私钥解密为字节。
     *
     * @param cipherTextBase64 Base64 密文
     * @param privateKey       私钥
     * @return 明文字节
     */
    public static byte[] decryptToBytes(String cipherTextBase64, PrivateKey privateKey) {
        require(privateKey, "privateKey");
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION_OAEP);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher.doFinal(Base64.getDecoder().decode(requireBase64(cipherTextBase64, "cipherText")));
        } catch (GeneralSecurityException | IllegalArgumentException ex) {
            throw new IllegalStateException("RSA decrypt failed", ex);
        }
    }

    /**
     * 私钥签名，返回 Base64 签名。
     *
     * @param data             原文
     * @param privateKeyBase64 Base64 私钥
     * @return Base64 签名
     */
    public static String sign(String data, String privateKeyBase64) {
        return sign(bytes(data), decodePrivateKey(privateKeyBase64));
    }

    /**
     * 私钥签名，返回 Base64 签名。
     *
     * @param data       原文字节
     * @param privateKey 私钥
     * @return Base64 签名
     */
    public static String sign(byte[] data, PrivateKey privateKey) {
        require(privateKey, "privateKey");
        try {
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(privateKey);
            signature.update(data == null ? new byte[0] : data);
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("RSA sign failed", ex);
        }
    }

    /**
     * 公钥验签。
     *
     * @param data            原文
     * @param signatureBase64 Base64 签名
     * @param publicKeyBase64 Base64 公钥
     * @return 是否通过
     */
    public static boolean verify(String data, String signatureBase64, String publicKeyBase64) {
        return verify(bytes(data), signatureBase64, decodePublicKey(publicKeyBase64));
    }

    /**
     * 公钥验签。
     *
     * @param data            原文字节
     * @param signatureBase64 Base64 签名
     * @param publicKey       公钥
     * @return 是否通过
     */
    public static boolean verify(byte[] data, String signatureBase64, PublicKey publicKey) {
        require(publicKey, "publicKey");
        try {
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initVerify(publicKey);
            signature.update(data == null ? new byte[0] : data);
            return signature.verify(Base64.getDecoder().decode(requireBase64(signatureBase64, "signature")));
        } catch (GeneralSecurityException | IllegalArgumentException ex) {
            return false;
        }
    }

    private static void require(Object value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " must not be null");
        }
    }

    private static String requireBase64(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    private static byte[] bytes(String text) {
        return (text == null ? "" : text).getBytes(StandardCharsets.UTF_8);
    }
}

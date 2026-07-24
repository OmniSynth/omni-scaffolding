package com.omni.scaffolding.common.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES 对称加解密工具。
 *
 * <p>默认推荐 {@code AES/GCM/NoPadding}（密文格式：Base64(IV 12 字节 + ciphertext+tag)）。
 * 另提供 CBC 兼容模式（密文格式：Base64(IV 16 字节 + ciphertext)）。
 *
 * <p>密钥长度须为 16 / 24 / 32 字节（对应 AES-128/192/256）；字符串密钥按 UTF-8 取字节，
 * 长度不符时抛出 {@link IllegalArgumentException}。需要任意口令派生密钥时可先对口令做 SHA-256。
 */
public final class AesUtils {

    public static final String TRANSFORMATION_GCM = "AES/GCM/NoPadding";
    public static final String TRANSFORMATION_CBC = "AES/CBC/PKCS5Padding";

    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_BITS = 128;
    private static final int CBC_IV_LENGTH = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    private AesUtils() {
    }

    /**
     * 生成随机 AES 密钥并 Base64 编码。
     *
     * @param keyBits 128 / 192 / 256
     * @return Base64 密钥
     */
    public static String generateKeyBase64(int keyBits) {
        if (keyBits != 128 && keyBits != 192 && keyBits != 256) {
            throw new IllegalArgumentException("AES keyBits must be 128, 192 or 256");
        }
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(keyBits, RANDOM);
            SecretKey key = generator.generateKey();
            return Base64.getEncoder().encodeToString(key.getEncoded());
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("generate AES key failed", ex);
        }
    }

    /**
     * AES-GCM 加密，返回 Base64(IV + ciphertext)。
     *
     * @param plainText 明文
     * @param key      16/24/32 字节密钥（UTF-8）
     * @return Base64 密文
     */
    public static String encryptGcm(String plainText, String key) {
        return encryptGcm(bytes(plainText), keyBytes(key));
    }

    /**
     * AES-GCM 加密，返回 Base64(IV + ciphertext)。
     *
     * @param plainText 明文
     * @param key       密钥字节
     * @return Base64 密文
     */
    public static String encryptGcm(String plainText, byte[] key) {
        return encryptGcm(bytes(plainText), key);
    }

    /**
     * AES-GCM 加密，返回 Base64(IV + ciphertext)。
     *
     * @param plain 明文字节
     * @param key   密钥字节
     * @return Base64 密文
     */
    public static String encryptGcm(byte[] plain, byte[] key) {
        requireKey(key);
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION_GCM);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] cipherText = cipher.doFinal(plain);
            return Base64.getEncoder().encodeToString(concat(iv, cipherText));
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("AES-GCM encrypt failed", ex);
        }
    }

    /**
     * AES-GCM 解密。
     *
     * @param cipherTextBase64 {@link #encryptGcm} 产物
     * @param key              16/24/32 字节密钥（UTF-8）
     * @return UTF-8 明文
     */
    public static String decryptGcm(String cipherTextBase64, String key) {
        return new String(decryptGcmToBytes(cipherTextBase64, keyBytes(key)), StandardCharsets.UTF_8);
    }

    /**
     * AES-GCM 解密。
     *
     * @param cipherTextBase64 {@link #encryptGcm} 产物
     * @param key              密钥字节
     * @return UTF-8 明文
     */
    public static String decryptGcm(String cipherTextBase64, byte[] key) {
        return new String(decryptGcmToBytes(cipherTextBase64, key), StandardCharsets.UTF_8);
    }

    /**
     * AES-GCM 解密为字节。
     *
     * @param cipherTextBase64 {@link #encryptGcm} 产物
     * @param key              密钥字节
     * @return 明文字节
     */
    public static byte[] decryptGcmToBytes(String cipherTextBase64, byte[] key) {
        requireKey(key);
        byte[] packed = Base64.getDecoder().decode(requireCipherText(cipherTextBase64));
        if (packed.length <= GCM_IV_LENGTH) {
            throw new IllegalArgumentException("AES-GCM cipher text too short");
        }
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] cipherText = new byte[packed.length - GCM_IV_LENGTH];
            System.arraycopy(packed, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(packed, GCM_IV_LENGTH, cipherText, 0, cipherText.length);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION_GCM);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(GCM_TAG_BITS, iv));
            return cipher.doFinal(cipherText);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("AES-GCM decrypt failed", ex);
        }
    }

    /**
     * AES-CBC 加密，返回 Base64(IV + ciphertext)。兼容旧系统时使用。
     *
     * @param plainText 明文
     * @param key      16/24/32 字节密钥（UTF-8）
     * @return Base64 密文
     */
    public static String encryptCbc(String plainText, String key) {
        return encryptCbc(bytes(plainText), keyBytes(key));
    }

    /**
     * AES-CBC 加密，返回 Base64(IV + ciphertext)。
     *
     * @param plainText 明文
     * @param key       密钥字节
     * @return Base64 密文
     */
    public static String encryptCbc(String plainText, byte[] key) {
        return encryptCbc(bytes(plainText), key);
    }

    /**
     * AES-CBC 加密，返回 Base64(IV + ciphertext)。
     *
     * @param plain 明文字节
     * @param key   密钥字节
     * @return Base64 密文
     */
    public static String encryptCbc(byte[] plain, byte[] key) {
        requireKey(key);
        try {
            byte[] iv = new byte[CBC_IV_LENGTH];
            RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION_CBC);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
            byte[] cipherText = cipher.doFinal(plain);
            return Base64.getEncoder().encodeToString(concat(iv, cipherText));
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("AES-CBC encrypt failed", ex);
        }
    }

    /**
     * AES-CBC 解密。
     *
     * @param cipherTextBase64 {@link #encryptCbc} 产物
     * @param key              16/24/32 字节密钥（UTF-8）
     * @return UTF-8 明文
     */
    public static String decryptCbc(String cipherTextBase64, String key) {
        return new String(decryptCbcToBytes(cipherTextBase64, keyBytes(key)), StandardCharsets.UTF_8);
    }

    /**
     * AES-CBC 解密。
     *
     * @param cipherTextBase64 {@link #encryptCbc} 产物
     * @param key              密钥字节
     * @return UTF-8 明文
     */
    public static String decryptCbc(String cipherTextBase64, byte[] key) {
        return new String(decryptCbcToBytes(cipherTextBase64, key), StandardCharsets.UTF_8);
    }

    /**
     * AES-CBC 解密为字节。
     *
     * @param cipherTextBase64 {@link #encryptCbc} 产物
     * @param key              密钥字节
     * @return 明文字节
     */
    public static byte[] decryptCbcToBytes(String cipherTextBase64, byte[] key) {
        requireKey(key);
        byte[] packed = Base64.getDecoder().decode(requireCipherText(cipherTextBase64));
        if (packed.length <= CBC_IV_LENGTH) {
            throw new IllegalArgumentException("AES-CBC cipher text too short");
        }
        try {
            byte[] iv = new byte[CBC_IV_LENGTH];
            byte[] cipherText = new byte[packed.length - CBC_IV_LENGTH];
            System.arraycopy(packed, 0, iv, 0, CBC_IV_LENGTH);
            System.arraycopy(packed, CBC_IV_LENGTH, cipherText, 0, cipherText.length);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION_CBC);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
            return cipher.doFinal(cipherText);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("AES-CBC decrypt failed", ex);
        }
    }

    /**
     * 将任意口令派生为 32 字节 AES-256 密钥（SHA-256），便于固定长度密钥场景。
     *
     * @param passphrase 口令，不可为空
     * @return 32 字节密钥
     */
    public static byte[] deriveKeySha256(String passphrase) {
        if (passphrase == null || passphrase.isBlank()) {
            throw new IllegalArgumentException("passphrase must not be blank");
        }
        return DigestUtils.sha256(passphrase.getBytes(StandardCharsets.UTF_8));
    }

    private static void requireKey(byte[] key) {
        if (key == null || (key.length != 16 && key.length != 24 && key.length != 32)) {
            throw new IllegalArgumentException("AES key length must be 16, 24 or 32 bytes");
        }
    }

    private static String requireCipherText(String cipherTextBase64) {
        if (cipherTextBase64 == null || cipherTextBase64.isBlank()) {
            throw new IllegalArgumentException("cipher text must not be blank");
        }
        return cipherTextBase64;
    }

    private static byte[] keyBytes(String key) {
        if (key == null) {
            throw new IllegalArgumentException("AES key must not be null");
        }
        return key.getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] bytes(String text) {
        return (text == null ? "" : text).getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] concat(byte[] left, byte[] right) {
        return ByteBuffer.allocate(left.length + right.length).put(left).put(right).array();
    }
}

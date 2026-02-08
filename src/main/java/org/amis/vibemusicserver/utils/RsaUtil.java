package org.amis.vibemusicserver.utils;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * @author : KwokChichung
 * @description : RSA加解密工具类
 * @createDate : 2026/2/9
 */
@Slf4j
@Component
public class RsaUtil {

    @Value("${rsa.private-key}")
    private String privateKeyStr;

    @Value("${rsa.public-key}")
    private String publicKeyStr;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    public void init() {
        try {
            privateKey = getPrivateKey(privateKeyStr);
            publicKey = getPublicKey(publicKeyStr);
            log.info("RSA密钥初始化成功");
        } catch (Exception e) {
            log.error("RSA密钥初始化失败", e);
        }
    }

    /**
     * 使用私钥解密
     *
     * @param encryptedData 加密后的数据
     * @return 解密后的明文
     */
    public String decrypt(String encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("RSA解密失败", e);
            throw new RuntimeException("密码解密失败");
        }
    }

    /**
     * 使用公钥加密
     *
     * @param data 明文数据
     * @return 加密后的数据
     */
    public String encrypt(String data) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] encryptedBytes = cipher.doFinal(dataBytes);
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            log.error("RSA加密失败", e);
            throw new RuntimeException("密码加密失败");
        }
    }

    /**
     * 获取公钥字符串（提供给前端使用）
     */
    public String getPublicKeyStr() {
        return publicKeyStr;
    }


    /**
     * 从字符串获取私钥（支持 PEM 和 Base64 格式）
     */
    private PrivateKey getPrivateKey(String keyStr) throws Exception {
        // 移除 PEM 头尾标记
        String cleanedKey = cleanPemKey(keyStr);
        byte[] keyBytes = Base64.getDecoder().decode(cleanedKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * 从字符串获取公钥（支持 PEM 和 Base64 格式）
     */
    private PublicKey getPublicKey(String keyStr) throws Exception {
        // 移除 PEM 头尾标记
        String cleanedKey = cleanPemKey(keyStr);
        byte[] keyBytes = Base64.getDecoder().decode(cleanedKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * 清理 PEM 格式的密钥（移除头尾标记）
     */
    private String cleanPemKey(String key) {
        if (key == null) {
            return key;
        }
        // 移除换行符
        key = key.replaceAll("\\s", "");
        // 移除 BEGIN/END 标记
        key = key.replace("-----BEGINPUBLICKEY-----", "")
                 .replace("-----ENDPUBLICKEY-----", "")
                 .replace("-----BEGINPRIVATEKEY-----", "")
                 .replace("-----ENDPRIVATEKEY-----", "")
                 .replace("-----BEGINRSAPRIVATEKEY-----", "")
                 .replace("-----ENDRSAPRIVATEKEY-----", "");
        return key;
    }

    /**
     * 生成RSA密钥对（仅用于初始化配置）
     */
    public static void main(String[] args) throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        String privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
        String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());

        System.out.println("=======================================================");
        System.out.println("RSA Key Pair Generated Successfully!");
        System.out.println("=======================================================");
        System.out.println();
        System.out.println("Add the following to application.yml:");
        System.out.println();
        System.out.println("rsa:");
        System.out.println("  private-key: " + privateKey);
        System.out.println("  public-key: " + publicKey);
        System.out.println();
        System.out.println("=======================================================");
    }

}
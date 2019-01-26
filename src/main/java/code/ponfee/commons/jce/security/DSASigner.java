package code.ponfee.commons.jce.security;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.DSAKey;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import code.ponfee.commons.util.ObjectUtils;

/**
 * DSA签名/验签（只用于数字签名）
 * @author fupf
 */
public final class DSASigner {

    private static final String ALGORITHM = "DSA";
    private static final String PUBLIC_KEY = "DSAPublicKey";
    private static final String PRIVATE_KEY = "DSAPrivateKey";

    /**
     * 默认生成密钥
     * @return 密钥对象
     */
    public static Map<String, DSAKey> initKey() {
        return initKey(ObjectUtils.uuid32(), 1024);
    }

    /**
     * 生成密钥
     * @param seed 种子
     * @param keySize   must be a multiple of 64, r
     *                  anging from 512 to 1024 (inclusive).
     * @return 密钥对象
     */
    public static Map<String, DSAKey> initKey(String seed, int keySize) {
        KeyPairGenerator keygen;
        try {
            keygen = KeyPairGenerator.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException(e); // cannot happened
        }
        // 初始化随机产生器
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.setSeed(seed.getBytes());
        keygen.initialize(keySize, secureRandom);
        KeyPair pair = keygen.genKeyPair();
        return ImmutableMap.of(PUBLIC_KEY, (DSAKey) pair.getPublic(), 
                               PRIVATE_KEY, (DSAKey) pair.getPrivate());
    }

    /**
     * 取得私钥
     * @param keyMap
     * @return
     */
    public static DSAPrivateKey getPrivateKey(Map<String, DSAKey> keyMap) {
        return (DSAPrivateKey) keyMap.get(PRIVATE_KEY);
    }

    public static DSAPrivateKey getPrivateKey(byte[] privateKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            return (DSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKey));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * 取得公钥
     * @param keyMap
     * @return
     */
    public static DSAPublicKey getPublicKey(Map<String, DSAKey> keyMap) {
        return (DSAPublicKey) keyMap.get(PUBLIC_KEY);
    }

    public static DSAPublicKey getPublicKey(byte[] publicKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            return (DSAPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(publicKey));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * 用私钥对信息生成数字签名
     * @param data 原文数据
     * @param privateKey 私钥
     * @return  签名结果
     */
    public static byte[] sign(byte[] data, byte[] privateKey) {
        return sign(data, getPrivateKey(privateKey));
    }

    public static byte[] sign(byte[] data, DSAPrivateKey privateKey) {
        try {
            Signature signature = Signature.getInstance(privateKey.getAlgorithm());
            signature.initSign(privateKey);
            signature.update(data);
            return signature.sign();
        } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * 校验数字签名
     * @param origin 原文数据
     * @param publicKey 公钥
     * @param signed 签名数据
     * @return 校验成功返回true 失败返回false
     * 
     */
    public static boolean verify(byte[] origin, byte[] publicKey, byte[] signed) {
        return verify(origin, getPublicKey(publicKey), signed);
    }

    public static boolean verify(byte[] origin, DSAPublicKey publicKey, byte[] signed) {
        try {
            Signature signature = Signature.getInstance(publicKey.getAlgorithm());
            signature.initVerify(publicKey);
            signature.update(origin);
            return signature.verify(signed);
        } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
            throw new SecurityException(e);
        }
    }

}

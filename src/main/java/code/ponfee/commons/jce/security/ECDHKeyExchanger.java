package code.ponfee.commons.jce.security;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;

import com.google.common.collect.ImmutableMap;

import code.ponfee.commons.jce.symmetric.Algorithm;

import static  code.ponfee.commons.jce.Providers.*;

/**
 * ECDH Key Exchange
 * Key-Agreement
 * @author fupf
 */
public final class ECDHKeyExchanger {

    private static final String ALGORITHM = "ECDH"; // ECDH算法名称
    private static final String PUBLIC_KEY = "ECDHPublicKey";
    private static final String PRIVATE_KEY = "ECDHPrivateKey";

    public static Map<String, byte[]> initPartAKey() {
        return initPartAKey(256);
    }

    /**
     * 初始化甲方密钥
     * 
     * @param keySize the key size: 192/224/256/384/521
     * @return the key map
     */
    public static Map<String, byte[]> initPartAKey(int keySize) {
        KeyPairGenerator keyPairGenerator;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM, BC);
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException(e);
        }
        keyPairGenerator.initialize(keySize); // must be 256
        KeyPair pair = keyPairGenerator.generateKeyPair();
        return ImmutableMap.of(PUBLIC_KEY, pair.getPublic().getEncoded(), // 甲方公钥
                               PRIVATE_KEY, pair.getPrivate().getEncoded()); // 甲方私钥
    }

    public static Map<String, byte[]> initPartBKey(byte[] partAPubKey) {
        return initPartBKey(getPublicKey(partAPubKey));
    }

    /**
     * 初始化乙方密钥
     * @param partAPublicKey 甲方公钥
     * @return
     */
    public static Map<String, byte[]> initPartBKey(ECPublicKey partAPublicKey) {
        // 由甲方公钥构建乙方密钥
        KeyPairGenerator keyPairGen;
        try {
            keyPairGen = KeyPairGenerator.getInstance(partAPublicKey.getAlgorithm(), BC);
            keyPairGen.initialize(partAPublicKey.getParams());
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw new SecurityException(e);
        }

        KeyPair keyPair = keyPairGen.generateKeyPair();
        return ImmutableMap.of(PUBLIC_KEY, keyPair.getPublic().getEncoded(),  // 乙方公钥
                               PRIVATE_KEY, keyPair.getPrivate().getEncoded());  // 乙方私钥
    }

    /**
     * ECPublicKey convert to byte array
     * @param key the ECPublicKey
     * @return byte array encoded of ECPublicKey
     */
    public static byte[] encode(ECPublicKey key) {
        return key.getEncoded();
    }

    /**
     * ECPrivateKey convert to byte array
     * @param key the ECPrivateKey
     * @return byte array encoded of ECPrivateKey
     */
    public static byte[] encode(ECPrivateKey key) {
        return key.getEncoded();
    }

    /**
     * 取得私钥
     * @param keyMap
     * @return
     */
    public static byte[] getPrivateKey(Map<String, byte[]> keyMap) {
        return keyMap.get(PRIVATE_KEY);
    }

    /**
     * 取得公钥
     * @param keyMap
     * @return
     */
    public static byte[] getPublicKey(Map<String, byte[]> keyMap) {
        return keyMap.get(PUBLIC_KEY);
    }

    /**
     * 取得私钥
     * @param privateKey
     * @return
     */
    public static ECPrivateKey getPrivateKey(byte[] privateKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM, BC);
            PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(privateKey);
            return (ECPrivateKey) keyFactory.generatePrivate(pkcs8KeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * 取得公钥
     * @param publicKey
     * @return
     */
    public static ECPublicKey getPublicKey(byte[] publicKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM, BC);
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(publicKey);
            return (ECPublicKey) keyFactory.generatePublic(x509KeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * 双方公私钥生成（协商）对称密钥
     * @param bPriKey
     * @param aPubKey
     * @return
     */
    public static SecretKey genSecretKey(byte[] bPriKey, byte[] aPubKey) {
        return genSecretKey(getPrivateKey(bPriKey), getPublicKey(aPubKey));
    }

    /**
     * 双方公私钥生成（协商）对称密钥
     * @param bPriKey
     * @param aPubKey
     * @return
     */
    public static SecretKey genSecretKey(ECPrivateKey bPriKey, ECPublicKey aPubKey) {
        try {
            KeyAgreement keyAgree = KeyAgreement.getInstance(aPubKey.getAlgorithm(), BC);
            keyAgree.init(bPriKey);
            keyAgree.doPhase(aPubKey, true);
            // 生成对称密钥，使用3DES对称加密，192位的AES被限制出口
            return keyAgree.generateSecret(Algorithm.DESede.name());
        } catch (InvalidKeyException | NoSuchAlgorithmException | IllegalStateException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * 加密<br>
     * @param data 待加密数据
     * @param secretKey 双方公私钥协商的对称密钥
     * @return
     */
    public static byte[] encrypt(byte[] data, SecretKey secretKey) {
        try {
            Cipher cipher = Cipher.getInstance(secretKey.getAlgorithm(), BC);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }

    /**
     * 解密<br>
     * @param data 待解密数据
     * @param secretKey 双方公私钥协商的对称密钥
     * @return
     */
    public static byte[] decrypt(byte[] data, SecretKey secretKey) {
        try {
            Cipher cipher = Cipher.getInstance(secretKey.getAlgorithm(), BC);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }

}

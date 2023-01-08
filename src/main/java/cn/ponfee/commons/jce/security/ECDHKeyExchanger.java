/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.jce.security;

import cn.ponfee.commons.jce.Providers;
import cn.ponfee.commons.jce.symmetric.Algorithm;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * ECDH Key Exchange
 * Key-Agreement
 * 
 * @author Ponfee
 */
public final class ECDHKeyExchanger {

    private static final String ALGORITHM = "ECDH"; // ECDH算法名称

    public static Pair<ECPublicKey, ECPrivateKey> initPartAKey() {
        return initPartAKey(256);
    }

    /**
     * 初始化甲方密钥
     * 
     * @param keySize the key size: 192/224/256/384/521
     * @return the key map
     */
    public static Pair<ECPublicKey, ECPrivateKey> initPartAKey(int keySize) {
        KeyPairGenerator keyPairGenerator = Providers.getKeyPairGenerator(ALGORITHM);
        keyPairGenerator.initialize(keySize); // must be 256
        KeyPair pair = keyPairGenerator.generateKeyPair();
        return ImmutablePair.of(
           (ECPublicKey) pair.getPublic(), // 甲方公钥
           (ECPrivateKey) pair.getPrivate() // 甲方私钥
       );
    }

    public static Pair<ECPublicKey, ECPrivateKey> initPartBKey(byte[] partAPubKey) {
        return initPartBKey(decodePublicKey(partAPubKey));
    }

    /**
     * 初始化乙方密钥
     * @param partAPublicKey 甲方公钥
     * @return
     */
    public static Pair<ECPublicKey, ECPrivateKey> initPartBKey(ECPublicKey partAPublicKey) {
        // 由甲方公钥构建乙方密钥
        KeyPairGenerator keyPairGen = Providers.getKeyPairGenerator(partAPublicKey.getAlgorithm());
        try {
            keyPairGen.initialize(partAPublicKey.getParams());
        } catch (InvalidAlgorithmParameterException e) {
            throw new SecurityException(e);
        }

        KeyPair keyPair = keyPairGen.generateKeyPair();
        return ImmutablePair.of(
            (ECPublicKey) keyPair.getPublic(), // 乙方公钥
            (ECPrivateKey) keyPair.getPrivate() // 乙方私钥
        );
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
     * @param privateKey
     * @return
     */
    public static ECPrivateKey decodePrivateKey(byte[] privateKey) {
        KeyFactory keyFactory = Providers.getKeyFactory(ALGORITHM);
        try {
            PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(privateKey);
            return (ECPrivateKey) keyFactory.generatePrivate(pkcs8KeySpec);
        } catch (InvalidKeySpecException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * 取得公钥
     * @param publicKey
     * @return
     */
    public static ECPublicKey decodePublicKey(byte[] publicKey) {
        KeyFactory keyFactory = Providers.getKeyFactory(ALGORITHM);
        try {
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(publicKey);
            return (ECPublicKey) keyFactory.generatePublic(x509KeySpec);
        } catch (InvalidKeySpecException e) {
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
        return genSecretKey(decodePrivateKey(bPriKey), decodePublicKey(aPubKey));
    }

    /**
     * 双方公私钥生成（协商）对称密钥
     * @param bPriKey
     * @param aPubKey
     * @return
     */
    public static SecretKey genSecretKey(ECPrivateKey bPriKey, ECPublicKey aPubKey) {
        KeyAgreement keyAgree = Providers.getKeyAgreement(aPubKey.getAlgorithm());
        try {
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
        Cipher cipher = Providers.getCipher(secretKey.getAlgorithm());
        try {
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
        Cipher cipher = Providers.getCipher(secretKey.getAlgorithm());
        try {
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }

}

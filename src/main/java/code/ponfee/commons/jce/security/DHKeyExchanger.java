package code.ponfee.commons.jce.security;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHKey;
import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.interfaces.DHPublicKey;

import com.google.common.collect.ImmutableMap;

import code.ponfee.commons.jce.symmetric.Algorithm;

/**
 * Diffie-Hellman Key Exchange
 * Key-Agreement
 * 
 * 1、生成质数p
 * 2、找到p的原根，满足： g mod p, g^2 mod p, ..., g^(p-1) mod p
 *    是各不相同的整数，并且以某种排列方式组成了从1到p-1的所有整数
 * 3、对于一个整数b和质数p的一个原根g，可以找到惟一的指数i，使得
 *    b=g^i mod p， 0<=i<=p-1，指数i称为b的以g为基数的模p的离散对数或者指数，
 *    该值被记为ind(g ,p(b))
 * 
 * 4、用户A选择一个随机数作为私钥XA<p，并计算公钥YA=g^XA mod p
 * 5、用户B选择一个随机数作为私钥XB<p，并计算公钥YB=g^XB mod p
 * 6、(YB)^XA mod p = K = (YA)^XB mod p
 * 7、 K = YB^XA mod p
 *      = (g^XB mod p)^XA mod p
 *      = (g^XB)^XA mod p          <-->  (a^b) mod p = ((a mod p)^b) mod p
 *      = (g^XA mod p)^XB mod p
 *      = (YA)^XB mod p
 *
 * g^(a*b) mod p = g^(b*a) mod p
 *
 * @author fupf
 */
public final class DHKeyExchanger {

    private static final String ALGORITHM = "DH"; // DH算法名称
    private static final String PUBLIC_KEY = "DHPublicKey";
    private static final String PRIVATE_KEY = "DHPrivateKey";

    public static Map<String, DHKey> initPartAKey() {
        return initPartAKey(1024);
    }

    /**
     * 初始化甲方密钥
     * @param keySize must be a multiple of 64, ranging from 512 to 1024 (inclusive).
     * @return
     */
    public static Map<String, DHKey> initPartAKey(int keySize) {
        KeyPairGenerator keyPairGenerator;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException(e);
        }
        keyPairGenerator.initialize(keySize);
        KeyPair pair = keyPairGenerator.generateKeyPair();
        return ImmutableMap.of(PUBLIC_KEY, (DHKey) pair.getPublic(), // 甲方公钥
                               PRIVATE_KEY, (DHKey) pair.getPrivate()); // 甲方私钥
    }

    /**
     * 初始化乙方密钥
     * @param partAPubKey 甲方公钥
     * @return
     */
    public static Map<String, DHKey> initPartBKey(byte[] partAPubKey) {
        return initPartBKey(getPublicKey(partAPubKey));
    }

    /**
     * 初始化乙方密钥
     * @param partAPublicKey 甲方公钥
     * @return
     */
    public static Map<String, DHKey> initPartBKey(DHPublicKey partAPublicKey) {
        // 由甲方公钥构建乙方密钥
        KeyPairGenerator keyPairGen;
        try {
            keyPairGen = KeyPairGenerator.getInstance(partAPublicKey.getAlgorithm());
            keyPairGen.initialize(partAPublicKey.getParams());
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw new SecurityException(e);
        }

        KeyPair keyPair = keyPairGen.generateKeyPair();
        return ImmutableMap.of(PUBLIC_KEY, (DHKey) keyPair.getPublic(),  // 乙方公钥
                               PRIVATE_KEY, (DHKey) keyPair.getPrivate());  // 乙方私钥
    }

    /**
     * DHPublicKey convert to byte array
     * @param key the DHPublicKey
     * @return byte array encoded of DHPublicKey
     */
    public static byte[] encode(DHPublicKey key) {
        return key.getEncoded();
    }

    /**
     * DHPrivateKey convert to byte array
     * @param key the DHPrivateKey
     * @return byte array encoded of DHPrivateKey
     */
    public static byte[] encode(DHPrivateKey key) {
        return key.getEncoded();
    }

    /**
     * 取得私钥
     * @param keyMap
     * @return
     */
    public static DHPrivateKey getPrivateKey(Map<String, DHKey> keyMap) {
        return (DHPrivateKey) keyMap.get(PRIVATE_KEY);
    }

    /**
     * 取得公钥
     * @param keyMap
     * @return
     */
    public static DHPublicKey getPublicKey(Map<String, DHKey> keyMap) {
        return (DHPublicKey) keyMap.get(PUBLIC_KEY);
    }

    /**
     * 取得私钥
     * @param privateKey
     * @return
     */
    public static DHPrivateKey getPrivateKey(byte[] privateKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(privateKey);
            return (DHPrivateKey) keyFactory.generatePrivate(pkcs8KeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * 取得公钥
     * @param publicKey
     * @return
     */
    public static DHPublicKey getPublicKey(byte[] publicKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(publicKey);
            return (DHPublicKey) keyFactory.generatePublic(x509KeySpec);
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
    public static SecretKey genSecretKey(DHPrivateKey bPriKey, DHPublicKey aPubKey) {
        try {
            KeyAgreement keyAgree = KeyAgreement.getInstance(aPubKey.getAlgorithm());
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
            Cipher cipher = Cipher.getInstance(secretKey.getAlgorithm());
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
            Cipher cipher = Cipher.getInstance(secretKey.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }

}

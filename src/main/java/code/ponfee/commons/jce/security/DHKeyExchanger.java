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

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.interfaces.DHPublicKey;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import code.ponfee.commons.jce.Providers;
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
 * @author Ponfee
 */
public final class DHKeyExchanger {

    private static final String ALGORITHM = "DH"; // DH算法名称

    // -------------------------------------------------------------初始化甲方密钥对
    public static Pair<DHPublicKey, DHPrivateKey> initPartAKey() {
        return initPartAKey(1024);
    }

    /**
     * 初始化甲方密钥
     * @param keySize must be a multiple of 64, ranging from 512 to 1024 (inclusive).
     * @return
     */
    public static Pair<DHPublicKey, DHPrivateKey> initPartAKey(int keySize) {
        KeyPairGenerator keyPairGenerator = Providers.getKeyPairGenerator(ALGORITHM);
        keyPairGenerator.initialize(keySize);
        KeyPair pair = keyPairGenerator.generateKeyPair();
        return ImmutablePair.of(
            (DHPublicKey) pair.getPublic(), // 甲方公钥
            (DHPrivateKey) pair.getPrivate() // // 甲方私钥
        );
    }

    // -------------------------------------------------------------初始化已方密钥对
    /**
     * 初始化乙方密钥
     * @param partAPubKey 甲方公钥
     * @return
     */
    public static Pair<DHPublicKey, DHPrivateKey> initPartBKey(byte[] partAPubKey) {
        return initPartBKey(decodePublicKey(partAPubKey));
    }

    /**
     * 初始化乙方密钥
     * 
     * @param partAPublicKey 甲方公钥
     * @return
     */
    public static Pair<DHPublicKey, DHPrivateKey> initPartBKey(DHPublicKey partAPublicKey) {
        // 由甲方公钥构建乙方密钥
        KeyPairGenerator keyPairGen = Providers.getKeyPairGenerator(partAPublicKey.getAlgorithm());
        try {
            keyPairGen.initialize(partAPublicKey.getParams());
        } catch (InvalidAlgorithmParameterException e) {
            throw new SecurityException(e);
        }

        KeyPair keyPair = keyPairGen.generateKeyPair();
        return ImmutablePair.of(
            (DHPublicKey) keyPair.getPublic(), // 乙方公钥
            (DHPrivateKey) keyPair.getPrivate() // // 乙方私钥
        );
    }

    // -------------------------------------------------------------密钥序列化
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

    // -------------------------------------------------------------密钥反序列化
    /**
     * 取得私钥
     * @param privateKey
     * @return
     */
    public static DHPrivateKey decodePrivateKey(byte[] privateKey) {
        KeyFactory keyFactory = Providers.getKeyFactory(ALGORITHM);
        try {
            PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(privateKey);
            return (DHPrivateKey) keyFactory.generatePrivate(pkcs8KeySpec);
        } catch (InvalidKeySpecException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * 取得公钥
     * @param publicKey
     * @return
     */
    public static DHPublicKey decodePublicKey(byte[] publicKey) {
        KeyFactory keyFactory = Providers.getKeyFactory(ALGORITHM);
        try {
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(publicKey);
            return (DHPublicKey) keyFactory.generatePublic(x509KeySpec);
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
    public static SecretKey genSecretKey(DHPrivateKey bPriKey, DHPublicKey aPubKey) {
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

package code.ponfee.commons.jce.security;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.ECKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

/**
 * ECDSA签名算法工具类
 * http://blog.csdn.net/qq_30866297/article/details/51465439
 * 
 * @author Ponfee
 */
public final class ECDSASigner {

    public enum ECDSASignAlgorithms {
        SHA1withECDSA, SHA256withECDSA, // 
        SHA384withECDSA, SHA512withECDSA
    }

    private static final String ALGORITHM = "EC";

    public static final String PRIVATE_KEY = "ECPrivateKey";
    public static final String PUBLIC_KEY = "ECPublicKey";

    public static Map<String, ECKey> generateKeyPair() {
        return generateKeyPair(256);
    }

    /**
     * 密钥生成
     * @param keySize  the key size: 192/224/256/384/521/571
     * @return ec key map
     */
    public static Map<String, ECKey> generateKeyPair(int keySize) {
        KeyPairGenerator keyPairGen;
        try {
            keyPairGen = KeyPairGenerator.getInstance(ALGORITHM);
        } catch (Exception e) {
            throw new SecurityException(e);
        }
        keyPairGen.initialize(keySize);
        KeyPair keyPair = keyPairGen.generateKeyPair();
        return ImmutableMap.of(PRIVATE_KEY, (ECPrivateKey) keyPair.getPrivate(), 
                               PUBLIC_KEY, (ECPublicKey) keyPair.getPublic());
    }

    /**
     * ECPublicKey convert to byte array
     * @param key  the ECPublicKey
     * @return byte array encoded of ECPublicKey
     */
    public static byte[] encode(ECPublicKey key) {
        return key.getEncoded();
    }

    /**
     * ECPrivateKey convert to byte array
     * @param key  the ECPrivateKey
     * @return byte array encoded of ECPrivateKey
     */
    public static byte[] encode(ECPrivateKey key) {
        return key.getEncoded();
    }

    /**
     * get the ECPublicKey from generate key map
     * {@link #generateKeyPair(int)}
     * @param keyMap
     * @return ECPublicKey
     */
    public static ECPublicKey getPublicKey(Map<String, ECKey> keyMap) {
        return (ECPublicKey) keyMap.get(PUBLIC_KEY);
    }

    /**
     * get the ECPrivateKey from generate key map
     * {@link #generateKeyPair(int)}
     * @param keyMap
     * @return ECPrivateKey
     */
    public static ECPrivateKey getPrivateKey(Map<String, ECKey> keyMap) {
        return (ECPrivateKey) keyMap.get(PRIVATE_KEY);
    }

    /**
     * get ECPublicKey from byte array
     * @param publicKey
     * @return
     */
    public static ECPublicKey getPublicKey(byte[] publicKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            return (ECPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(publicKey));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * get ECPrivateKey from byte array
     * @param privateKey
     * @return
     */
    public static ECPrivateKey getPrivateKey(byte[] privateKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            return (ECPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKey));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new SecurityException(e);
        }
    }

    public static byte[] signSha1(byte[] data, ECPrivateKey privateKey) {
        return sign(data, privateKey, ECDSASignAlgorithms.SHA1withECDSA);
    }

    public static boolean verifySha1(byte[] data, byte[] signed, ECPublicKey publicKey) {
        return verify(data, signed, publicKey, ECDSASignAlgorithms.SHA1withECDSA);
    }

    public static byte[] signSha256(byte[] data, ECPrivateKey privateKey) {
        return sign(data, privateKey, ECDSASignAlgorithms.SHA256withECDSA);
    }

    public static boolean verifySha256(byte[] data, byte[] signed, ECPublicKey publicKey) {
        return verify(data, signed, publicKey, ECDSASignAlgorithms.SHA256withECDSA);
    }

    public static byte[] signSha512(byte[] data, ECPrivateKey privateKey) {
        return sign(data, privateKey, ECDSASignAlgorithms.SHA512withECDSA);
    }

    public static boolean verifySha512(byte[] data, byte[] signed, ECPublicKey publicKey) {
        return verify(data, signed, publicKey, ECDSASignAlgorithms.SHA512withECDSA);
    }

    private static byte[] sign(byte[] data, ECPrivateKey privateKey, 
                               ECDSASignAlgorithms algorithm) {
        try {
            Signature signature = Signature.getInstance(algorithm.name());
            signature.initSign(privateKey);
            signature.update(data);
            return signature.sign();
        } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
            throw new SecurityException(e);
        }
    }

    private static boolean verify(byte[] data, byte[] signed, ECPublicKey publicKey, 
                                  ECDSASignAlgorithms algorithm) {
        try {
            Signature signature = Signature.getInstance(algorithm.name());
            signature.initVerify(publicKey);
            signature.update(data);
            return signature.verify(signed);
        } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
            throw new SecurityException(e);
        }
    }

    /*public static <T extends Key & ECKey> byte[] encrypt(byte[] data, T key) {
        return docrypt(data, key, Cipher.ENCRYPT_MODE);
    }

    public static <T extends Key & ECKey> byte[] decrypt(byte[] encrypted, T key) {
        return docrypt(encrypted, key, Cipher.DECRYPT_MODE);
    }

    private static byte[] docrypt(byte[] data, Key key, int cryptMode) {
        try {
            Cipher cipher = Cipher.getInstance(key.getAlgorithm());
            cipher.init(cryptMode, key);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }*/

}

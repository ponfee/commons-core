package code.ponfee.commons.jce.security;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import code.ponfee.commons.jce.Providers;

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

    public static Pair<ECPublicKey, ECPrivateKey> generateKeyPair() {
        return generateKeyPair(256);
    }

    /**
     * 密钥生成
     * @param keySize  the key size: 192/224/256/384/521/571
     * @return ec key map
     */
    public static Pair<ECPublicKey, ECPrivateKey> generateKeyPair(int keySize) {
        KeyPairGenerator keyPairGen = Providers.getKeyPairGenerator(ALGORITHM);
        keyPairGen.initialize(keySize);
        KeyPair keyPair = keyPairGen.generateKeyPair();
        return ImmutablePair.of(
            (ECPublicKey) keyPair.getPublic(), (ECPrivateKey) keyPair.getPrivate()
        );
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
     * get ECPublicKey from byte array
     * @param publicKey
     * @return
     */
    public static ECPublicKey decodePublicKey(byte[] publicKey) {
        KeyFactory keyFactory = Providers.getKeyFactory(ALGORITHM);
        try {
            return (ECPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(publicKey));
        } catch (InvalidKeySpecException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * get ECPrivateKey from byte array
     * @param privateKey
     * @return
     */
    public static ECPrivateKey decodePrivateKey(byte[] privateKey) {
        KeyFactory keyFactory = Providers.getKeyFactory(ALGORITHM);
        try {
            return (ECPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKey));
        } catch (InvalidKeySpecException e) {
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
        Signature signature = Providers.getSignature(algorithm.name());
        try {
            signature.initSign(privateKey);
            signature.update(data);
            return signature.sign();
        } catch (InvalidKeyException | SignatureException e) {
            throw new SecurityException(e);
        }
    }

    private static boolean verify(byte[] data, byte[] signed, ECPublicKey publicKey, 
                                  ECDSASignAlgorithms algorithm) {
        Signature signature = Providers.getSignature(algorithm.name());
        try {
            signature.initVerify(publicKey);
            signature.update(data);
            return signature.verify(signed);
        } catch (InvalidKeyException | SignatureException e) {
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

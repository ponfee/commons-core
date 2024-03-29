/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.jce.security;

import cn.ponfee.commons.io.Closeables;
import cn.ponfee.commons.io.Files;
import cn.ponfee.commons.jce.Providers;
import cn.ponfee.commons.jce.RSASignAlgorithms;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.security.*;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static cn.ponfee.commons.jce.RSACipherPaddings.ECB_PKCS1PADDING;
import static cn.ponfee.commons.jce.RSACipherPaddings.NONE_NOPADDING;

/**
 * <pre>
 * 基于大整数因式分解的数学难题（费马小定理）
 * n=p*q, p,q互质
 *
 * RSA Cryptor
 * 加/解密
 * 签名/验签
 * </pre>
 *
 * @author Ponfee
 */
public final class RSACryptor {
    private RSACryptor() {}

    static final String ALG_RSA = "RSA";

    public static RSAKeyPair generateKeyPair() {
        return generateKeyPair(1024);
    }

    /**
     * 密钥生成
     * @param keySize   the RSA key size, optional is 512 or 1028
     *                  or 2048 or 4096
     * @return RSAKeyPair
     */
    public static RSAKeyPair generateKeyPair(int keySize) {
        KeyPairGenerator keyPairGen = Providers.getKeyPairGenerator(ALG_RSA);
        keyPairGen.initialize(keySize);
        KeyPair pair = keyPairGen.generateKeyPair();
        return new RSAKeyPair(
            (RSAPrivateKey) pair.getPrivate(), (RSAPublicKey) pair.getPublic()
        );
    }

    // ---------------------------------------sign/verify---------------------------------------
    /**
     * MD5 sign
     * @param data
     * @param privateKey
     * @return
     */
    public static byte[] signMd5(byte[] data, RSAPrivateKey privateKey) {
        return sign(data, privateKey, RSASignAlgorithms.MD5withRSA);
    }

    /**
     * SHA1 sign
     * @param data
     * @param privateKey
     * @return
     */
    public static byte[] signSha1(byte[] data, RSAPrivateKey privateKey) {
        return sign(data, privateKey, RSASignAlgorithms.SHA1withRSA);
    }

    /**
     * SHA256 sign
     * @param data
     * @param privateKey
     * @return
     */
    public static byte[] signSha256(byte[] data, RSAPrivateKey privateKey) {
        return sign(data, privateKey, RSASignAlgorithms.SHA256withRSA);
    }

    /**
     * verify MD5 signature
     * @param data
     * @param publicKey
     * @param signed
     * @return
     */
    public static boolean verifyMd5(byte[] data, RSAPublicKey publicKey, byte[] signed) {
        return verify(data, publicKey, signed, RSASignAlgorithms.MD5withRSA);
    }

    /**
     * verify SHA1 signature
     * @param data
     * @param publicKey
     * @param signed
     * @return
     */
    public static boolean verifySha1(byte[] data, RSAPublicKey publicKey, byte[] signed) {
        return verify(data, publicKey, signed, RSASignAlgorithms.SHA1withRSA);
    }

    /**
     * verify SHA256 signature
     * @param data
     * @param publicKey
     * @param signed
     * @return
     */
    public static boolean verifySha256(byte[] data, RSAPublicKey publicKey, byte[] signed) {
        return verify(data, publicKey, signed, RSASignAlgorithms.SHA256withRSA);
    }

    /**
     * <pre>
     *   1、可以通过修改生成密钥的长度来调整密文长度
     *   2、不管明文长度是多少，RSA生成的密文长度总是固定的
     *   3、明文长度不能超过密钥长度：
     *     1）SUN JDK默认的RSA加密实现不允许明文长度超过密钥长度减去11字节（byte）：比如1024位（bit）的密钥，
     *        则待加密的明文最长为1024/8-11=117（byte）
     *     2）BouncyCastle提供的加密算法能够支持到的RSA明文长度最长为密钥长度
     *   4、每次生成的密文都不一致证明加密算法安全：这是因为在加密前使用RSA/None/PKCS1Padding对明文信息进行了
     *      随机数填充，为了防止已知明文攻击，随机长度的填充来防止攻击者知道明文的长度。
     *   5、javax.crypto.Cipher是线程不安全的
     * </pre>
     * 
     * 大数据分块加密
     * @param data 源数据
     * @param key
     * @return
     */
    public static <T extends Key & RSAKey> byte[] encrypt(byte[] data, T key) {
        return docrypt(data, key, Cipher.ENCRYPT_MODE, true);
    }

    public static <T extends Key & RSAKey> byte[] encryptNoPadding(byte[] data, T key) {
        return docrypt(data, key, Cipher.ENCRYPT_MODE, false);
    }

    public static <T extends Key & RSAKey> void encrypt(InputStream input, T key, 
                                                        OutputStream out) {
        docrypt(input, key, out, Cipher.ENCRYPT_MODE, true);
    }

    public static <T extends Key & RSAKey> void encryptNoPadding(InputStream input, 
                                                                 T key, OutputStream out) {
        docrypt(input, key, out, Cipher.ENCRYPT_MODE, false);
    }

    /**
     * 大数据分块解密
     * @param encrypted
     * @param key
     * @return
     */
    public static <T extends Key & RSAKey> byte[] decrypt(byte[] encrypted, T key) {
        return docrypt(encrypted, key, Cipher.DECRYPT_MODE, true);
    }

    public static <T extends Key & RSAKey> byte[] decryptNoPadding(byte[] encrypted, T key) {
        return docrypt(encrypted, key, Cipher.DECRYPT_MODE, false);
    }

    public static <T extends Key & RSAKey> void decrypt(InputStream input, T key, 
                                                        OutputStream out) {
        docrypt(input, key, out, Cipher.DECRYPT_MODE, true);
    }

    public static <T extends Key & RSAKey> void decryptNoPadding(InputStream input, 
                                                                 T key, OutputStream out) {
        docrypt(input, key, out, Cipher.DECRYPT_MODE, false);
    }

    // -----------------------------------private methods-------------------------------------
    private static <T extends Key & RSAKey> int getBlockSize(int cryptMode, T key) {
        return (cryptMode == Cipher.ENCRYPT_MODE)
               ? key.getModulus().bitLength() / 8 - 11
               : key.getModulus().bitLength() / 8;
    }

    private static <T extends Key & RSAKey> void docrypt(InputStream input, T key, OutputStream out, 
                                                         int cryptMode, boolean isPadding) {
        // Providers.getCipher(key.getAlgorithm())
        Cipher cipher = Providers.getCipher(
            key.getAlgorithm() + (isPadding ? ECB_PKCS1PADDING.transform() : NONE_NOPADDING.transform())
        );
        try {
            cipher.init(cryptMode, key);
            byte[] buffer = new byte[getBlockSize(cryptMode, key)];
            for (int len; (len = input.read(buffer)) != Files.EOF;) {
                out.write(cipher.doFinal(buffer, 0, len));
            }
            out.flush();
        } catch (Exception e) {
            throw new SecurityException(e);
        } finally {
            Closeables.console(input);
        }
    }

    /**
     * JDK supported:
     * Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
     * 
     * BC supported: 
     *   Cipher.getInstance("RSA/None/NoPadding", Providers.BC);
     *   Cipher.getInstance("RSA/ECB/NOPADDING", Providers.BC);
     *   ...
     * @param data
     * @param key
     * @param cryptMode
     * @param isPadding
     * @return
     */
    private static <T extends Key & RSAKey> byte[] docrypt(byte[] data, T key, 
                                                           int cryptMode, boolean isPadding) {
        int blockSize = getBlockSize(cryptMode, key);
        // Providers.getCipher(key.getAlgorithm())
        Cipher cipher = Providers.getCipher(
            key.getAlgorithm() + (isPadding ? ECB_PKCS1PADDING.transform() : NONE_NOPADDING.transform())
        );
        try {
            cipher.init(cryptMode, key);
            ByteArrayOutputStream out = new ByteArrayOutputStream(data.length);
            byte[] block;
            for (int offSet = 0, len = data.length; offSet < len; offSet += blockSize) {
                block = cipher.doFinal(data, offSet, Math.min(blockSize, len - offSet));
                out.write(block, 0, block.length);
            }
            out.flush();
            return out.toByteArray();
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }

    /**
     * 数据签名
     * @param data
     * @param privateKey
     * @param alg
     * @return
     */
    private static byte[] sign(byte[] data, RSAPrivateKey privateKey, RSASignAlgorithms alg) {
        Signature signature = Providers.getSignature(alg.name());
        try {
            signature.initSign(privateKey);
            signature.update(data);
            return signature.sign();
        } catch (SignatureException | InvalidKeyException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * 验证签名
     * @param data
     * @param publicKey
     * @param signed
     * @param alg
     * @return
     */
    private static boolean verify(byte[] data, RSAPublicKey publicKey, 
                                  byte[] signed, RSASignAlgorithms alg) {

        Signature signature = Providers.getSignature(alg.name());
        try {
            signature.initVerify(publicKey);
            signature.update(data);
            return signature.verify(signed);
        } catch (InvalidKeyException | SignatureException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * RSA密钥对
     */
    public static final class RSAKeyPair implements Serializable {
        private static final long serialVersionUID = -1592700389671199076L;
        private final RSAPrivateKey privateKey;
        private final RSAPublicKey publicKey;

        private RSAKeyPair(RSAPrivateKey privateKey, RSAPublicKey publicKey) {
            this.privateKey = privateKey;
            this.publicKey = publicKey;
        }

        public RSAPrivateKey getPrivateKey() {
            return privateKey;
        }

        public RSAPublicKey getPublicKey() {
            return publicKey;
        }

        public String toPkcs8PrivateKey() {
            return RSAPrivateKeys.toPkcs8(privateKey);
        }

        public String toPkcs1PrivateKey() {
            return RSAPrivateKeys.toPkcs1(privateKey);
        }

        public String toPkcs8PublicKey() {
            return RSAPublicKeys.toPkcs8(publicKey);
        }

        public String toPkcs1PublicKey() {
            return RSAPublicKeys.toPkcs1(publicKey);
        }
    }

}

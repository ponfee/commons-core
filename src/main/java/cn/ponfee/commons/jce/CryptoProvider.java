/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.jce;

import cn.ponfee.commons.io.Files;
import cn.ponfee.commons.jce.security.ECDSASigner;
import cn.ponfee.commons.jce.security.RSACryptor;
import cn.ponfee.commons.jce.security.RSAPrivateKeys;
import cn.ponfee.commons.jce.security.RSAPublicKeys;
import cn.ponfee.commons.jce.sm.SM2;
import cn.ponfee.commons.jce.symmetric.SymmetricCryptor;
import cn.ponfee.commons.util.Base64UrlSafe;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Objects;

/**
 * 加解密服务提供
 * 
 * @author Ponfee
 */
public abstract class CryptoProvider {

    /**
     * Encrypts data
     * 
     * @param original  the origin data
     * @return encrypted data
     */
    public abstract byte[] encrypt(@Nonnull byte[] original);

    /**
     * Decrypts data
     * 
     * @param encrypted  the encrypted data
     * @return origin data
     */
    public abstract byte[] decrypt(@Nonnull byte[] encrypted);

    /**
     * Signs the data
     * 
     * @param data  the data
     * @return signature data
     */
    public byte[] sign(byte[] data) {
        throw new UnsupportedOperationException("cannot support signature.");
    }

    /**
     * Verify the data signature
     * 
     * @param data   the origin data
     * @param signed the signed data
     * @return {@code true} verify success
     */
    public boolean verify(byte[] data, byte[] signed) {
        throw new UnsupportedOperationException("cannot support verify signature.");
    }

    /**
     * Encrypts the string data
     * 
     * @param plaintext  the plain text
     * @return encrypted data
     */
    public final String encrypt(String plaintext) {
        return encrypt(plaintext, StandardCharsets.UTF_8);
    }

    /**
     * 字符串数据加密
     * 
     * @param plaintext 明文
     * @param charset   字符串编码
     * @return encrypted data
     */
    public final String encrypt(String plaintext, Charset charset) {
        if (plaintext == null) {
            return null;
        }

        return Base64UrlSafe.encode(
            this.encrypt(plaintext.getBytes(charset))
        );
    }

    /**
     * Decrypts data
     * 
     * @param ciphertext  the encryted data of base64 string
     * @return origin data of string
     */
    public final String decrypt(String ciphertext) {
        return decrypt(ciphertext, StandardCharsets.UTF_8);
    }

    /**
     * Decrypts data
     * 
     * @param ciphertext  the encryted data of base64 string
     * @param charset     the origin data charset
     * @return origin data of string
     */
    public final String decrypt(String ciphertext, Charset charset) {
        if (ciphertext == null) {
            return null;
        }

        return new String(
            decrypt(Base64UrlSafe.decode(ciphertext)), 
            charset
        );
    }

    /**
     * Signs data
     * 
     * @param data  the data
     * @return signed data
     */
    public final String sign(String data) {
        return sign(data, Files.UTF_8);
    }

    /**
     * Signs data
     * 
     * @param data  the string data
     * @param charset the charset of string data
     * @return signed data
     */
    public final String sign(String data, String charset) {
        if (StringUtils.isEmpty(data)) {
            return null;
        }
        return Base64UrlSafe.encode(
            sign(data.getBytes(Charset.forName(charset)))
        );
    }

    /**
     * Verifys the data
     * 
     * @param data the origin data
     * @param signed the signed data
     * @return {@code true} verify success
     */
    public final boolean verify(String data, String signed) {
        return verify(data, Files.UTF_8, signed);
    }

    /**
     * Verifys the data
     * 
     * @param data  the data
     * @param charset the charset
     * @param signed the signed data
     * @return {@code true} verify success
     */
    public final boolean verify(String data, String charset, String signed) {
        return verify(
             data.getBytes(Charset.forName(charset)), 
             Base64UrlSafe.decode(signed)
        );
    }

    // -----------------------------------------------------------------------SymmetricCryptor
    /**
     * 对称密钥组件
     * @param symmetricKey {@link SymmetricCryptor}
     * @return
     */
    public static CryptoProvider symmetricKeyProvider(SymmetricCryptor symmetricKey) {
        // the symmetricKey is thread-safe
        return new CryptoProvider() {
            @Override
            public byte[] encrypt(byte[] original) {
                return symmetricKey.encrypt(
                    Objects.requireNonNull(original)
                );
            }

            @Override
            public byte[] decrypt(byte[] encrypted) {
                return symmetricKey.decrypt(encrypted);
            }
        };
    }

    // -----------------------------------------------------------------------RSA
    /**
     * rsa public key密钥组件
     * 
     * @param pkcs8PublicKey  the string of pkcs8 public key format
     * @return
     */
    public static CryptoProvider rsaPublicKeyProvider(String pkcs8PublicKey) {
        return new CryptoProvider() {
            final RSAPublicKey pubKey = RSAPublicKeys.fromPkcs8(pkcs8PublicKey); // thread-safe

            @Override
            public byte[] encrypt(byte[] original) {
                return RSACryptor.encrypt(original, pubKey); // 公钥加密
            }

            @Override
            public byte[] decrypt(byte[] encrypted) {
                // cannot support public key decrypt
                throw new UnsupportedOperationException("cannot support decrypt.");
            }

            @Override
            public boolean verify(byte[] data, byte[] signed) {
                return RSACryptor.verifySha1(data, pubKey, signed);
            }
        };
    }

    /**
     * pkcs8PrivateKey include public exponent
     * 
     * forbid use private key encrypt and use public key decrypt
     * 
     * @param pkcs8PrivateKey  the string of pkcs8 private key format
     * @return
     */
    public static CryptoProvider rsaPrivateKeyProvider(String pkcs8PrivateKey) {
        RSAPrivateKey priKey = RSAPrivateKeys.fromPkcs8(pkcs8PrivateKey); // thread-safe
        return rsaProvider(priKey, RSAPrivateKeys.extractPublicKey(priKey));
    }

    /**
     * Creates CryptoProvider of RSA 
     * 
     * @param priKey the RSAPrivateKey
     * @param pubKey the pubKey
     * @return a CryptoProvider of RSA 
     */
    public static CryptoProvider rsaProvider(RSAPrivateKey priKey, RSAPublicKey pubKey) {
        return new CryptoProvider() {
            @Override
            public byte[] encrypt(byte[] original) {
                // only support public key encrypt
                // forbid encrypt with private key
                return RSACryptor.encrypt(original, pubKey); // 公钥加密
            }

            @Override
            public byte[] decrypt(byte[] encrypted) {
                // only support private key decrypt
                return RSACryptor.decrypt(encrypted, priKey); // 私钥解密
            }

            @Override
            public byte[] sign(byte[] data) {
                return RSACryptor.signSha1(data, priKey);
            }

            @Override
            public boolean verify(byte[] data, byte[] signed) {
                return RSACryptor.verifySha1(data, pubKey, signed);
            }
        };
    }

    // -----------------------------------------------------------------------SM2
    public static CryptoProvider sm2PublicKeyProvider(byte[] publicKey) {
        return sm2PublicKeyProvider(ECParameters.SM2_BEST, publicKey);
    }

    public static CryptoProvider sm2PublicKeyProvider(ECParameters ecParameter, 
                                                      byte[] publicKey) {
        return new CryptoProvider() {
            final byte[] publicKey0 = Arrays.copyOf(publicKey, publicKey.length);

            @Override
            public byte[] encrypt(byte[] original) {
                return SM2.encrypt(ecParameter, publicKey0, original); // 公钥加密
            }

            @Override
            public byte[] decrypt(byte[] encrypted) {
                throw new UnsupportedOperationException("cannot support decrypt.");
            }

            @Override
            public boolean verify(byte[] data, byte[] signed) {
                return SM2.verify(ecParameter, data, signed, publicKey0);
            }
        };
    }

    public static CryptoProvider sm2PrivateKeyProvider(byte[] publicKey, 
                                                       byte[] privateKey) {
        return sm2PrivateKeyProvider(ECParameters.SM2_BEST, publicKey, privateKey);
    }

    public static CryptoProvider sm2PrivateKeyProvider(ECParameters ecParameter,
                                                       byte[] publicKey, 
                                                       byte[] privateKey) {
        return new CryptoProvider() {
            final byte[] publicKey0  = Arrays.copyOf(publicKey, publicKey.length);
            final byte[] privateKey0 = Arrays.copyOf(privateKey, privateKey.length);

            @Override
            public byte[] encrypt(byte[] original) {
                return SM2.encrypt(ecParameter, publicKey0, original); // 公钥加密
            }

            @Override
            public byte[] decrypt(byte[] encrypted) { // 私钥解密
                return SM2.decrypt(ecParameter, privateKey0, encrypted);
            }

            @Override
            public byte[] sign(byte[] data) { // sign data by SM3WithSM2
                return SM2.sign(ecParameter, data, publicKey0, privateKey0);
            }

            @Override
            public boolean verify(byte[] data, byte[] signed) { // verify the SM3WithSM2 signature
                return SM2.verify(ecParameter, data, signed, publicKey0);
            }
        };
    }

    // -----------------------------------------------------------------------ECDSASinger
    public static CryptoProvider ecdsaPublicKeyProvider(byte[] publicKey) {
        return new CryptoProvider() {
            final ECPublicKey publicKey0 = ECDSASigner.decodePublicKey(publicKey);

            @Override
            public byte[] encrypt(byte[] original) {
                throw new UnsupportedOperationException("ECDSA cannot support encrypt.");
            }

            @Override
            public byte[] decrypt(byte[] encrypted) {
                throw new UnsupportedOperationException("ECDSA cannot support decrypt.");
            }

            @Override
            public boolean verify(byte[] data, byte[] signed) {
                return ECDSASigner.verifySha256(data, signed, publicKey0);
            }
        };
    }

    public static CryptoProvider ecdsaPrivateKeyProvider(byte[] publicKey, byte[] privateKey) {
        return new CryptoProvider() {
            final ECPublicKey publicKey0 = ECDSASigner.decodePublicKey(publicKey);
            final ECPrivateKey privateKey0 = ECDSASigner.decodePrivateKey(privateKey);

            @Override
            public byte[] encrypt(byte[] original) {
                throw new UnsupportedOperationException("ECDSA cannot support encrypt.");
            }

            @Override
            public byte[] decrypt(byte[] encrypted) {
                throw new UnsupportedOperationException("ECDSA cannot support decrypt.");
            }

            @Override
            public byte[] sign(byte[] data) {
                return ECDSASigner.signSha256(data, privateKey0);
            }

            @Override
            public boolean verify(byte[] data, byte[] signed) {
                return ECDSASigner.verifySha256(data, signed, publicKey0);
            }
        };
    }

}

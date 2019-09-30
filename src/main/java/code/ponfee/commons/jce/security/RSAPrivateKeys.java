package code.ponfee.commons.jce.security;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.util.Base64;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.PKCS8Generator;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.OutputEncryptor;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.pkcs.PKCSException;
import org.bouncycastle.pkcs.jcajce.JcePKCSPBEInputDecryptorProviderBuilder;
import org.bouncycastle.pkcs.jcajce.JcePKCSPBEOutputEncryptorBuilder;
import org.bouncycastle.util.io.pem.PemObject;

import code.ponfee.commons.jce.Providers;
import code.ponfee.commons.jce.cert.X509CertUtils;

/**
 * <pre>
 *   PEM格式：
 *    PKCS#1：RSA PRIVATE KEY
 *    PKCS#8：PRIVATE KEY
 *    PKCS#8加密：ENCRYPTED PRIVATE KEY
 * 
 * @see org.bouncycastle.openssl.PEMParser
 *   ("CERTIFICATE REQUEST",      new PKCS10CertificationRequestParser());
 *   ("NEW CERTIFICATE REQUEST",  new PKCS10CertificationRequestParser());
 *   ("CERTIFICATE",              new X509CertificateParser());
 *   ("TRUSTED CERTIFICATE",      new X509TrustedCertificateParser());
 *   ("X509 CERTIFICATE",         new X509CertificateParser());
 *   ("X509 CRL",                 new X509CRLParser());
 *   ("PKCS7",                    new PKCS7Parser());
 *   ("CMS",                      new PKCS7Parser());
 *   ("ATTRIBUTE CERTIFICATE",    new X509AttributeCertificateParser());
 *   ("EC PARAMETERS",            new ECCurveParamsParser());
 *   ("PUBLIC KEY",               new PublicKeyParser());
 *   ("RSA PUBLIC KEY",           new RSAPublicKeyParser());
 *   ("RSA PRIVATE KEY",          new KeyPairParser(new RSAKeyPairParser()));
 *   ("DSA PRIVATE KEY",          new KeyPairParser(new DSAKeyPairParser()));
 *   ("EC PRIVATE KEY",           new KeyPairParser(new ECDSAKeyPairParser()));
 *   ("ENCRYPTED PRIVATE KEY",    new EncryptedPrivateKeyParser());
 *   ("PRIVATE KEY",              new PrivateKeyParser());
 * </pre>
 * 
 * RSA Private Key Convert
 * 
 * @author Ponfee
 */
public final class RSAPrivateKeys {
    private RSAPrivateKeys() {}

    /**
     * Build RSAPrivateKey with modulus and privateExponent
     *
     * @param modulus
     * @param privateExponent
     * @return the RSAPrivateKey
     */
    public static RSAPrivateKey toRSAPrivateKey(BigInteger modulus, BigInteger privateExponent) {
        try {
            return (RSAPrivateKey) Providers.getKeyFactory(RSACryptor.ALG_RSA).generatePrivate(
                new RSAPrivateKeySpec(modulus, privateExponent) // RSAPrivateCrtKeySpec
            );
        } catch (Exception ex) {
            throw new SecurityException(ex);
        }
    }

    /**
     * 对于某些jdk不支持私钥加密及验签，所以要反转私钥为公钥
     * 私钥伪造公钥来支持加密及验签
     * @param privateKey
     * @return
     */
    public static RSAPublicKey inverse(RSAPrivateKey privateKey) {
        return RSAPublicKeys.toRSAPublicKey(privateKey.getModulus(), 
                                            privateKey.getPrivateExponent());
    }

    // ----------------------------------EXTRACT PUBLIC KEY FROM PRIVATE KEY-----------------------------------
    /**
     * extract public key from private key
     * 
     * @param privateKey
     * @return
     */
    public static RSAPublicKey extractPublicKey(RSAPrivateKey privateKey) {
        if (!(privateKey instanceof RSAPrivateCrtKey)) {
            throw new ClassCastException("The key expect a java.security.interfaces.RSAPrivateCrtKey, "
                                       + "but is " + privateKey.getClass().getCanonicalName());
        }
        RSAPrivateCrtKey key = (RSAPrivateCrtKey) privateKey;
        return RSAPublicKeys.toRSAPublicKey(key.getModulus(), 
                                            key.getPublicExponent());
    }

    // ----------------------------------PRIVATE KEY PKCS1 FORMAT-----------------------------------
    /**
     * MIICXAIBAAKBgQCo20qAU4iyZIInpu2XzNXYHhFv6FVC/N1vsfz4ZrwX3VQaFsXf720QBkuP34Y31jy/6B+OB7DzklDBTnJXltCX2XdHyBY5WQYMX9rsQrfbvUL47u676FD1T8o1/e+cEOGS75mKQIQjyt1zCZOl26Hy6x4TPeBSdVzFNYSr7KNjLQIDAQABAoGALFd51v0YtpACRdtmJSjbNyeeOJ7wVOkGVWCOJ8UCu9mZTkiQqd+76itdCGkQW/VceqDAOJH4e93+auTozeuC1w/srrUuPASUsE/5VLwPBvR90kToC28B59wAdl31nD0KM8COq/9EdrkVkz6XO7KAik9gr3PLHCXu4i7tzf9djlkCQQDhagX7hsjJZ554Pr0uBhXHwMmhiLPOK1b3884Wc1rHTMShVGF3DJH6stJV5hXwzjXBwSA8zCbxGDsqVdmbQBkPAkEAv8Sv4GtdXCucN0GsZcRhvOmGhNkhQU7W3qkPqLaAvBzfCzT/Kty4YEWTlF+sCP1+/Chl7AHf4FQ+3ivNkftoAwJAEZ0YRJQ+okY/gsPcQnllQEuXNdEZw7VtQUjCxMxUvpgIEVcnmobX7VAF0YJ+GmfymWY+36FQNaygCunUbCYxDwJAQaKxS8+Tmbt3cVYyCnbnuP/4wbmLb03rrzQQHv+wGjKLiMtv1pzLInBN7ce9Gyqgbu/oypltpdtP1T0K1D9HPwJBAKskq4+amIGnJ7FxGiPXAi0+Y96QPbAR/WjXiIaLRvwRa4Jwy8U6E6HHfYYTeuuB7h1ga6kyzfB7nUeGyeWSSkI=
     * 
     * convert private key to base64 pkcs1 format
     * @param privateKey
     * @return pkcs1  encoded base64 pkcs1 format private key
     */
    public static String toPkcs1(RSAPrivateKey privateKey) {
        PrivateKeyInfo privKeyInfo = PrivateKeyInfo.getInstance(privateKey.getEncoded());
        try {
            return Base64.getEncoder().encodeToString(
                privKeyInfo.parsePrivateKey().toASN1Primitive().getEncoded()
            );
        } catch (IOException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * parse private key from pkcs1 format
     * @param pkcs1PrivateKey
     * @return RSAPrivateKey
     */
    public static RSAPrivateKey fromPkcs1(String pkcs1PrivateKey) {
        ASN1EncodableVector v1 = new ASN1EncodableVector();
        v1.add(new ASN1ObjectIdentifier(PKCSObjectIdentifiers.rsaEncryption.getId()));
        v1.add(DERNull.INSTANCE);

        ASN1EncodableVector v2 = new ASN1EncodableVector();
        v2.add(new ASN1Integer(0));
        v2.add(new DERSequence(v1));
        v2.add(new DEROctetString(Base64.getDecoder().decode(pkcs1PrivateKey)));
        ASN1Sequence seq = new DERSequence(v2);
        try {
            return fromPkcs8(Base64.getEncoder().encodeToString(seq.getEncoded()));
        } catch (IOException e) {
            throw new SecurityException(e);
        }
    }

    // ----------------------------------PRIVATE KEY PKCS1 PEM FORMAT-----------------------------------
    /**
     * -----BEGIN RSA PRIVATE KEY-----
     * MIICXAIBAAKBgQCo20qAU4iyZIInpu2XzNXYHhFv6FVC/N1vsfz4ZrwX3VQaFsXf
     * 720QBkuP34Y31jy/6B+OB7DzklDBTnJXltCX2XdHyBY5WQYMX9rsQrfbvUL47u67
     * 6FD1T8o1/e+cEOGS75mKQIQjyt1zCZOl26Hy6x4TPeBSdVzFNYSr7KNjLQIDAQAB
     * AoGALFd51v0YtpACRdtmJSjbNyeeOJ7wVOkGVWCOJ8UCu9mZTkiQqd+76itdCGkQ
     * W/VceqDAOJH4e93+auTozeuC1w/srrUuPASUsE/5VLwPBvR90kToC28B59wAdl31
     * nD0KM8COq/9EdrkVkz6XO7KAik9gr3PLHCXu4i7tzf9djlkCQQDhagX7hsjJZ554
     * Pr0uBhXHwMmhiLPOK1b3884Wc1rHTMShVGF3DJH6stJV5hXwzjXBwSA8zCbxGDsq
     * VdmbQBkPAkEAv8Sv4GtdXCucN0GsZcRhvOmGhNkhQU7W3qkPqLaAvBzfCzT/Kty4
     * YEWTlF+sCP1+/Chl7AHf4FQ+3ivNkftoAwJAEZ0YRJQ+okY/gsPcQnllQEuXNdEZ
     * w7VtQUjCxMxUvpgIEVcnmobX7VAF0YJ+GmfymWY+36FQNaygCunUbCYxDwJAQaKx
     * S8+Tmbt3cVYyCnbnuP/4wbmLb03rrzQQHv+wGjKLiMtv1pzLInBN7ce9Gyqgbu/o
     * ypltpdtP1T0K1D9HPwJBAKskq4+amIGnJ7FxGiPXAi0+Y96QPbAR/WjXiIaLRvwR
     * a4Jwy8U6E6HHfYYTeuuB7h1ga6kyzfB7nUeGyeWSSkI=
     * -----END RSA PRIVATE KEY-----
     * <p>
     * 
     * new PemObject("RSA PRIVATE KEY", toPkcs1Encode(privateKey))
     * 
     * convert private key to pem format
     * @param privateKey
     * @return  encoded base64 pkcs1 pem fromat private key
     */
    public static String toPkcs1Pem(RSAPrivateKey privateKey) {
        return X509CertUtils.exportToPem(privateKey);
    }

    /**
     * parse private key from pem format
     * @param pemPrivateKey  encoded pem format private key
     * @return
     */
    public static RSAPrivateKey fromPkcs1Pem(String pemPrivateKey) {
        try (Reader reader = new StringReader(pemPrivateKey); 
             PEMParser pemParser = new PEMParser(reader)
        ) {
            PEMKeyPair keyPair = (PEMKeyPair) pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(Providers.BC);
            //PublicKey publicKey = converter.getPublicKey(keyPair.getPublicKeyInfo());
            return (RSAPrivateKey) converter.getPrivateKey(keyPair.getPrivateKeyInfo());
        } catch (IOException e) {
            throw new SecurityException(e);
        }
    }

    // ----------------------------------PRIVATE KEY PKCS8 FORMAT-----------------------------------
    /**
     * MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAKjbSoBTiLJkgiem7ZfM1dgeEW/oVUL83W+x/PhmvBfdVBoWxd/vbRAGS4/fhjfWPL/oH44HsPOSUMFOcleW0JfZd0fIFjlZBgxf2uxCt9u9Qvju7rvoUPVPyjX975wQ4ZLvmYpAhCPK3XMJk6XbofLrHhM94FJ1XMU1hKvso2MtAgMBAAECgYAsV3nW/Ri2kAJF22YlKNs3J544nvBU6QZVYI4nxQK72ZlOSJCp37vqK10IaRBb9Vx6oMA4kfh73f5q5OjN64LXD+yutS48BJSwT/lUvA8G9H3SROgLbwHn3AB2XfWcPQozwI6r/0R2uRWTPpc7soCKT2Cvc8scJe7iLu3N/12OWQJBAOFqBfuGyMlnnng+vS4GFcfAyaGIs84rVvfzzhZzWsdMxKFUYXcMkfqy0lXmFfDONcHBIDzMJvEYOypV2ZtAGQ8CQQC/xK/ga11cK5w3QaxlxGG86YaE2SFBTtbeqQ+otoC8HN8LNP8q3LhgRZOUX6wI/X78KGXsAd/gVD7eK82R+2gDAkARnRhElD6iRj+Cw9xCeWVAS5c10RnDtW1BSMLEzFS+mAgRVyeahtftUAXRgn4aZ/KZZj7foVA1rKAK6dRsJjEPAkBBorFLz5OZu3dxVjIKdue4//jBuYtvTeuvNBAe/7AaMouIy2/WnMsicE3tx70bKqBu7+jKmW2l20/VPQrUP0c/AkEAqySrj5qYgacnsXEaI9cCLT5j3pA9sBH9aNeIhotG/BFrgnDLxToTocd9hhN664HuHWBrqTLN8HudR4bJ5ZJKQg==
     * 
     * convert private key to pkcs8 format
     * @param privateKey
     * @return pkcs8      base64 pkcs8 format private key
     */
    public static String toPkcs8(RSAPrivateKey privateKey) {
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    /**
     * parse private key from pkcs8 format
     * @param pkcs8PrivateKey   encoded base64 pkcs8 fromat private key
     * @return RSAPrivateKey
     */
    public static RSAPrivateKey fromPkcs8(String pkcs8PrivateKey) {
        byte[] bytes = Base64.getDecoder().decode(pkcs8PrivateKey);
        KeyFactory keyFactory = Providers.getKeyFactory(RSACryptor.ALG_RSA);
        try {
            return (RSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(bytes));
        } catch (InvalidKeySpecException e) {
            throw new SecurityException(e);
        }
    }

    // ----------------------------------Transform PRIVATE KEY ENCRYPTED PKCS8 PEM FORMAT
    /**
     * -----BEGIN ENCRYPTED PRIVATE KEY-----
     * MIICrjAoBgoqhkiG9w0BDAEDMBoEFA4ymXPHyGS9n5BRIibZHRkJ+idqAgIEAASC
     * AoA5TutO4A/F9MX4h3278DBIihEEetPGU7GxbmCySVsJraL8tXueMEZrLSDWC9rl
     * StJR82Umv0H8fpiMKlzYyHQjmqclY7e367+tQ87EqEenZNCCC1uveYLfBM7kZ+4/
     * m276IY6sNuJqYp3k8RrIBfYG9KCCQ7ywWiORuKvGbNAytFW9H+Z9JAyO5E70ysWe
     * pvfeLCFbRgJ0BOkm1aSyk81MN/alZ9d6a3d/UJLnnV42u7dS++mONVi66y6gKoON
     * Y5xVX2ICPRGoIvZmXeeQYzH02XFpTn9+vQ//KG4iGErXjNaWLSWpLyCY2qHQWwQ1
     * YB43aX7xYOiKUN24PMiwGKwjhBaKzakMt4lcmGNd6ZcwPC+ghhkmpPcCu4gSabQk
     * Etv5tmkChBLIjRgxmmnlEYLDl68e8vth5RquJvwB4zBOQkDo9tPcwWnOk4vbvGJP
     * w1WXfwHU4X4oy+FOiOTe7+lOTN6CeXxfx8a91h5zS1tA16bQLAgTA7oJGPD3yHpF
     * aBYXPNzwIOpAUkEqCSbZuuYZ5uidjrm7rV8nSjXu0fkYxzpXbQAps+NBtlKHFXNC
     * JWvj2g6UFtk/RoT4ghgtTx11DZUh+GsKbCjLM52omDDNLK8K+GOEFzElWiZIEWfh
     * yoUAN9KuuCprC3RsqV4K70nQewuiX5NBt9xYcaKVBQ5jRgL0xnVpquyFeFXrY0Ge
     * EDgOZTSUVVxlbNQ+iwBb52cD2cFmPcIszSNpQ85cS8eISdYiwaW42yUa7LYpO98S
     * jyTNnLzOMRt04gcBcp71EOWEheE9Ui6AqweSA6LUHulSbOK4+4oKtdKH5KjdcWYI
     * WDgTEoIGOC3se36z3v5Mlr8h
     * -----END ENCRYPTED PRIVATE KEY-----
     * <p>
     * 
     * convert private key to encrypted pem format
     * @param privateKey    the private key
     * @param outEncryptor  the out encryptor
     * @return
     */
    public static String toEncryptedPkcs8Pem(RSAPrivateKey privateKey,
                                             OutputEncryptor outEncryptor) {
        try (StringWriter stringWriter = new StringWriter(); 
             JcaPEMWriter pemWriter = new JcaPEMWriter(stringWriter)
        ) {
            PrivateKeyInfo privKeyInfo = PrivateKeyInfo.getInstance(privateKey.getEncoded());
            pemWriter.writeObject(new PKCS8Generator(privKeyInfo, outEncryptor));
            pemWriter.flush();
            return stringWriter.toString();
        } catch (IOException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * convert private key to encrypted pem format
     * default {@link PKCSObjectIdentifiers#pbeWithSHAAnd3_KeyTripleDES_CBC}
     * algorithm for encrypt
     * 
     * @param privateKey
     * @param password
     * @return private key pkcs8 pem format
     */
    public static String toEncryptedPkcs8Pem(RSAPrivateKey privateKey, String password) {
        JcePKCSPBEOutputEncryptorBuilder builder = new JcePKCSPBEOutputEncryptorBuilder(
            PKCSObjectIdentifiers.pbeWithSHAAnd3_KeyTripleDES_CBC
        );
        try {
            return toEncryptedPkcs8Pem(privateKey, builder.build(password.toCharArray()));
        } catch (OperatorCreationException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * Encrypts private key to pkcs#8 format
     * 
     * @param privateKey the private key
     * @param password   the password
     * @return a string of encrypted private key pkcs#8 format
     */
    public static String toEncryptedPkcs8(RSAPrivateKey privateKey, String password) {
        JcePKCSPBEOutputEncryptorBuilder builder = new JcePKCSPBEOutputEncryptorBuilder(
            PKCSObjectIdentifiers.pbeWithSHAAnd3_KeyTripleDES_CBC
        );
        try {
            return toEncryptedPkcs8(privateKey, builder.build(password.toCharArray()));
        } catch (OperatorCreationException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * Encrypts private key to pkcs#8 format
     * 
     * @param privateKey the private key
     * @param outEncryptor the OutputEncryptor
     * @return a string of encrypted private key pkcs#8 format
     */
    public static String toEncryptedPkcs8(RSAPrivateKey privateKey, OutputEncryptor outEncryptor) {
        try {
            PrivateKeyInfo privKeyInfo = PrivateKeyInfo.getInstance(privateKey.getEncoded());
            PemObject pem = new PKCS8Generator(privKeyInfo, outEncryptor).generate();
            return Base64.getEncoder().encodeToString(pem.getContent());
        } catch (IOException e) {
            throw new SecurityException(e);
        }
    }

    // --------------------------------------------------Parse PRIVATE KEY ENCRYPTED PKCS8 PEM FORMAT
    /**
     * Parse private key from encrypted pem format
     * 
     * @param encryptedPem he encrypted pem
     * @param inputDecryptor the InputDecryptorProvider
     * @return RSAPrivateKey
     */
    public static RSAPrivateKey fromEncryptedPkcs8Pem(String encryptedPem, 
                                                      InputDecryptorProvider inputDecryptor) {
        try (Reader reader = new StringReader(encryptedPem); 
             PEMParser pemParser = new PEMParser(reader)
        ) {
            PKCS8EncryptedPrivateKeyInfo encrypted = (PKCS8EncryptedPrivateKeyInfo) pemParser.readObject();
            PrivateKeyInfo pkInfo = encrypted.decryptPrivateKeyInfo(inputDecryptor);
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(Providers.BC);
            return (RSAPrivateKey) converter.getPrivateKey(pkInfo);
        } catch (IOException | PKCSException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * Parse private key from encrypted pem format <p>
     * default {@link JcePKCSPBEInputDecryptorProviderBuilder} input decryptor<p>
     * 
     * @param encryptedPem the encrypted pem
     * @param password     the password
     * @return RSAPrivateKey
     */
    public static RSAPrivateKey fromEncryptedPkcs8Pem(String encryptedPem, String password) {
        JcePKCSPBEInputDecryptorProviderBuilder builder = new JcePKCSPBEInputDecryptorProviderBuilder();
        return fromEncryptedPkcs8Pem(encryptedPem, builder.build(password.toCharArray()));
    }

    public static RSAPrivateKey fromEncryptedPkcs8(String encryptedPrivateKey, String password) {
        return fromEncryptedPkcs8(Base64.getDecoder().decode(encryptedPrivateKey), password);
    }

    /**
     * Parse private key from encrypted format
     * 
     * @param encryptedPrivateKey the encryptedPrivateKey
     * @param password  the password
     * @return RSAPrivateKey
     */
    public static RSAPrivateKey fromEncryptedPkcs8(byte[] encryptedPrivateKey, String password) {
        JcePKCSPBEInputDecryptorProviderBuilder builder = new JcePKCSPBEInputDecryptorProviderBuilder();
        return fromEncryptedPkcs8(encryptedPrivateKey, builder.build(password.toCharArray()));
    }

    public static RSAPrivateKey fromEncryptedPkcs8(String encryptedPrivateKey,
                                                   InputDecryptorProvider inputDecryptor) {
        return fromEncryptedPkcs8(Base64.getDecoder().decode(encryptedPrivateKey), inputDecryptor);
    }

    /**
     * Parse private key from encrypted format
     * 
     * @param encryptedPrivateKey the encryptedPrivateKey
     * @param inputDecryptor the InputDecryptorProvider
     * @return RSAPrivateKey
     */
    public static RSAPrivateKey fromEncryptedPkcs8(byte[] encryptedPrivateKey,
                                                   InputDecryptorProvider inputDecryptor) {
        try {
            PKCS8EncryptedPrivateKeyInfo encrypted = new PKCS8EncryptedPrivateKeyInfo(encryptedPrivateKey);
            PrivateKeyInfo pkInfo = encrypted.decryptPrivateKeyInfo(inputDecryptor);
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(Providers.BC);
            return (RSAPrivateKey) converter.getPrivateKey(pkInfo);
        } catch (IOException | PKCSException e) {
            throw new SecurityException(e);
        }
    }

    // --------------------------------------------------Get the rsa key bit length
    /**
     * Gets the rsa key length
     * 
     * @param privateKey the privateKey
     * @return rsa key bit length
     */
    public static int getKeyLength(RSAPrivateKey privateKey) {
        return privateKey.getModulus().bitLength();
    }
}

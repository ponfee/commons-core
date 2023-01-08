/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.jce.security;

import cn.ponfee.commons.jce.Providers;
import cn.ponfee.commons.jce.cert.X509CertUtils;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.openssl.PEMParser;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.cert.Certificate;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * PKCS#8 PEM：PUBLIC KEY
 * 
 * RSA Public Key convert
 * 
 * @author Ponfee
 */
public final class RSAPublicKeys {
    private RSAPublicKeys() {}

    /**
     * Build RSAPublicKey with modulus and publicExponent
     *
     * @param modulus
     * @param publicExponent
     * @return the RSAPublicKey
     */
    public static RSAPublicKey toRSAPublicKey(BigInteger modulus, BigInteger publicExponent) {
        try {
            return (RSAPublicKey) Providers.getKeyFactory(RSACryptor.ALG_RSA).generatePublic(
                new RSAPublicKeySpec(modulus, publicExponent)
            );
        } catch (Exception ex) {
            throw new SecurityException(ex);
        }
    }
  
    /**
     * 证书中获取公钥
     * @param cert
     * @return
     */
    public static RSAPublicKey getPublicKey(Certificate cert) {
        return (RSAPublicKey) cert.getPublicKey();
    }

    /**
     * 对于某些jdk不支持公钥解密及签名，所以要反转公钥为私钥
     * 公钥伪造成私钥来支持解密及签名
     * 
     * @param publicKey
     * @return
     */
    public static RSAPrivateKey inverse(RSAPublicKey publicKey) {
        return RSAPrivateKeys.toRSAPrivateKey(
            publicKey.getModulus(), publicKey.getPublicExponent()
        );
    }

    // ------------------------------------------------------------PUBLIC KEY PKCS1 FORMAT
    /**
     * MIGJAoGBAKVpbo/Wum3G5ciustuKNGvPX/rgkdZw33QGqBR5UOKUoD5/h/IeQlS7ladX+oa+ciVCXyP854Zq+0RVQ7x87DfAohLmyXlIGOJ7KLJZkUWDYSG0WsPbnTOEmxQcRzqEV5g9pVHIjgPH6N/j6HHKRs5xDEd3pVpoRBZKEncbZ85xAgMBAAE=
     * <p>
     * 
     * ASN1 Encode
     * 
     * The RSA Public key PEM file is specific for RSA keys.<p>
     * convert public key to pkcs1 format<p>
     * @param publicKey
     * @return
     */
    public static String toPkcs1(RSAPublicKey publicKey) {
        SubjectPublicKeyInfo spkInfo = SubjectPublicKeyInfo.getInstance(publicKey.getEncoded());
        try {
            return Base64.getEncoder().encodeToString(spkInfo.parsePublicKey().getEncoded());
        } catch (IOException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * parse public key from pkcs1 format
     * @param pkcs1PublicKey  encoded base64 pkcs1 public key
     * @return
     */
    public static RSAPublicKey fromPkcs1(String pkcs1PublicKey) {
        byte[] bytes = Base64.getDecoder().decode(pkcs1PublicKey);
        try {
            org.bouncycastle.asn1.pkcs.RSAPublicKey pk = org.bouncycastle.asn1.pkcs.RSAPublicKey.getInstance(bytes);
            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(pk.getModulus(), pk.getPublicExponent());
            return (RSAPublicKey) Providers.getKeyFactory(RSACryptor.ALG_RSA).generatePublic(keySpec);
        } catch (InvalidKeySpecException e) {
            throw new SecurityException(e);
        }
    }

    // ------------------------------------------------------------PUBLIC KEY X509 PKCS8 FORMAT
    /**
     * MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQClaW6P1rptxuXIrrLbijRrz1/64JHWcN90BqgUeVDilKA+f4fyHkJUu5WnV/qGvnIlQl8j/OeGavtEVUO8fOw3wKIS5sl5SBjieyiyWZFFg2EhtFrD250zhJsUHEc6hFeYPaVRyI4Dx+jf4+hxykbOcQxHd6VaaEQWShJ3G2fOcQIDAQAB
     * 
     * DER Encode
     * 
     * convert public key to x509 pkcs8 fromat
     * @param publicKey
     * @return
     */
    public static String toPkcs8(RSAPublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    /**
     * parse public key from base64 X509 pkcs8 fromat
     * @param pkcs8PublicKey  encoded base64 x509 pkcs8 fromat
     * @return RSAPublicKey
     */
    public static RSAPublicKey fromPkcs8(String pkcs8PublicKey) {
        byte[] keyBytes = Base64.getDecoder().decode(pkcs8PublicKey);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = Providers.getKeyFactory(RSACryptor.ALG_RSA);
        try {
            return (RSAPublicKey) keyFactory.generatePublic(x509KeySpec);
        } catch (InvalidKeySpecException e) {
            throw new SecurityException(e);
        }
    }

    // ------------------------------------------------------------PUBLIC KEY X509 PKCS8 PEM FORMAT
    /**
     * -----BEGIN PUBLIC KEY-----
     * MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQClaW6P1rptxuXIrrLbijRrz1/6
     * 4JHWcN90BqgUeVDilKA+f4fyHkJUu5WnV/qGvnIlQl8j/OeGavtEVUO8fOw3wKIS
     * 5sl5SBjieyiyWZFFg2EhtFrD250zhJsUHEc6hFeYPaVRyI4Dx+jf4+hxykbOcQxH
     * d6VaaEQWShJ3G2fOcQIDAQAB
     * -----END PUBLIC KEY-----
     * <p>
     * 
     * new PemObject("RSA PUBLIC KEY", toPkcs8Encode(publicKey))
     * 
     * convert public key to pem fromat (pkcs8)
     * @param publicKey
     * @return
     */
    public static String toPkcs8Pem(RSAPublicKey publicKey) {
        return X509CertUtils.exportToPem(publicKey);
    }

    /**
     * parse public key from pem format
     * @param pemPublicKey  encoded pem public key
     * @return
     */
    public static RSAPublicKey fromPkcs8Pem(String pemPublicKey) {
        try (Reader reader = new StringReader(pemPublicKey); 
             PEMParser pemParser = new PEMParser(reader)
        ) {
            SubjectPublicKeyInfo subPkInfo = (SubjectPublicKeyInfo) pemParser.readObject();
            RSAKeyParameters param = (RSAKeyParameters) PublicKeyFactory.createKey(subPkInfo);
            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(param.getModulus(), param.getExponent());
            return (RSAPublicKey) Providers.getKeyFactory(RSACryptor.ALG_RSA).generatePublic(keySpec);
        } catch (IOException | InvalidKeySpecException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * Gets the rsa key length
     * 
     * @param rsaKey the rsa key
     * @return a int number of key bit length
     */
    public static int getKeyLength(RSAKey rsaKey) {
        return rsaKey.getModulus().bitLength();
    }
}

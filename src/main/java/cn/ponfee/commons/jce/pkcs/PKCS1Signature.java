/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.jce.pkcs;

import cn.ponfee.commons.jce.Providers;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.X509Certificate;

/**
 * pkcs1方式的签名/验签工具类
 * 
 * @author Ponfee
 */
public class PKCS1Signature {

    /**
     * 签名
     * @param data        the byte array data to sign
     * @param privateKey  the private key
     * @param cert        the certificate
     * @return the signature of private key signed
     */
    public static byte[] sign(byte[] data, PrivateKey privateKey, X509Certificate cert) {
        Signature signature = Providers.getSignature(cert.getSigAlgName());
        try {
            signature.initSign(privateKey);
            signature.update(data);
            return signature.sign();
        } catch (GeneralSecurityException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * 验签
     * @param data      the origin byte array data
     * @param signed    the byte array of signature
     * @param cert      the certificate
     * @return {@code true} is success
     */
    public static boolean verify(byte[] data, byte[] signed, X509Certificate cert) {
        Signature sign = Providers.getSignature(cert.getSigAlgName());
        try {
            sign.initVerify(cert.getPublicKey());
            sign.update(data);
            return sign.verify(signed);
        } catch (GeneralSecurityException e) {
            throw new SecurityException(e);
        }
    }

}

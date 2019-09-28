package code.ponfee.commons.jce.cert;

import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import code.ponfee.commons.jce.pkcs.PKCS7Signature;
import sun.security.pkcs.PKCS7;
import sun.security.pkcs.SignerInfo;

/**
 * pkcs7方式验签
 * 
 * @author Ponfee
 */
@SuppressWarnings("restriction")
public class CertPKCS7Verifier extends CertSignedVerifier {

    private final PKCS7 pkcs7;

    /**
     * 不附原文的多人pkcs7签名
     * @param rootCert  the root ca cert
     * @param crl       the cert revoke list
     * @param pkcs7Data the pkcs7 byte array data
     * @param info      the origin byte array data
     */
    public CertPKCS7Verifier(X509Certificate rootCert, X509CRL crl, 
                             byte[] pkcs7Data, byte[] info) {
        this(rootCert, crl, PKCS7Signature.getPkcs7(pkcs7Data), info);
    }

    /**
     * 附原文的多人pkcs7签名
     * @param rootCert  the root ca cert
     * @param crl       the cert revoke list
     * @param pkcs7Data the pkcs7 byte array data, attached origin byte array data
     */
    public CertPKCS7Verifier(X509Certificate rootCert, X509CRL crl, byte[] pkcs7Data) {
        this(rootCert, crl, PKCS7Signature.getPkcs7(pkcs7Data));
    }

    /**
     * 附原文的多人pkcs7签名
     * @param rootCert  the root ca cert
     * @param crl       the cert revoke list
     * @param pkcs7     the pkcs7
     */
    public CertPKCS7Verifier(X509Certificate rootCert, X509CRL crl, PKCS7 pkcs7) {
        this(rootCert, crl, pkcs7, PKCS7Signature.getContent(pkcs7));
    }

    /**
     * 附原文的多人pkcs7签名
     * @param rootCert the root ca cert
     * @param crl      the cert revoke list
     * @param pkcs7    the pkck7
     * @param info     the origin byte array data
     */
    public CertPKCS7Verifier(X509Certificate rootCert, X509CRL crl, 
                             PKCS7 pkcs7, byte[] info) {
        super(rootCert, crl, info);

        this.pkcs7 = pkcs7;

        SignerInfo[] signs = pkcs7.getSignerInfos();
        Map<BigInteger, X509Certificate> certs = new HashMap<>(signs.length << 1);
        for (X509Certificate cert : pkcs7.getCertificates()) {
            certs.put(cert.getSerialNumber(), cert);
        }

        this.subjects = new X509Certificate[signs.length];
        for (int i = 0; i < signs.length; i++) {
            X509Certificate cert = certs.get(signs[i].getCertificateSerialNumber());
            if (cert == null) {
                throw new IllegalArgumentException("cannot found the sign cert: " 
                                         + signs[i].getCertificateSerialNumber());
            } else {
                this.subjects[i++] = cert;
                this.signedInfos.add(signs[i].getEncryptedDigest());
            }
        }
    }

    @Override
    public void verifySigned() {
        String subjectCN = null;
        try {
            for (SignerInfo signer : pkcs7.getSignerInfos()) {
                subjectCN = X509CertUtils.getCertInfo(signer.getCertificate(pkcs7), 
                                                      X509CertInfo.SUBJECT_CN);
                if (pkcs7.verify(signer, this.info) == null) {
                    throw new SecurityException("[" + subjectCN + "]验签不通过");
                }
            }
        } catch (SignatureException e) {
            throw new SecurityException("[" + subjectCN + "]签名信息错误", e);
        } catch (IOException e) {
            throw new SecurityException("获取证书主题异常", e);
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException("证书验签出错", e);
        }
    }

}

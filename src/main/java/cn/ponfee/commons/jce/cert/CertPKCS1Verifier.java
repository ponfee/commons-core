/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.jce.cert;

import cn.ponfee.commons.jce.Providers;

import java.security.InvalidKeyException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;

/**
 * pkcs1 signature verifier
 * 
 * @author Ponfee
 */
public class CertPKCS1Verifier extends CertSignedVerifier {

    /**
     * the signature of pkcs1 verifer
     * @param rootCert the ca root cert
     * @param crl      the cert revoke list
     * @param subject  the subject cert
     * @param info     origin data info
     * @param signed   signed info
     */
    public CertPKCS1Verifier(X509Certificate rootCert, X509CRL crl, 
                             X509Certificate subject, byte[] info, byte[] signed) {
        super(rootCert, crl, info);
        this.subjects = new X509Certificate[] { subject };
        this.signedInfos.add(signed);
    }

    @Override
    public void verifySigned() {
        String subjectCN = null;
        Signature sign = Providers.getSignature(this.subjects[0].getSigAlgName());
        try {
            subjectCN = X509CertUtils.getCertInfo(this.subjects[0], X509CertInfo.SUBJECT_CN);
            sign.initVerify(this.subjects[0].getPublicKey());
            sign.update(this.info);

            if (!sign.verify(this.signedInfos.get(0))) {
                throw new SecurityException("[" + subjectCN + "]验签不通过");
            }
        } catch (SignatureException e) {
            throw new SecurityException("[" + subjectCN + "]证书签名信息错误", e);
        } catch (InvalidKeyException e) {
            throw new SecurityException("证书验签出错", e);
        }
    }

}

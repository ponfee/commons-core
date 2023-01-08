/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.jce.cert;

import java.security.SignatureException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 证书验签（template method patterns）
 * 
 * @author Ponfee
 */
public abstract class CertSignedVerifier {

    protected final X509Certificate rootCert; // 根证书
    protected final X509CRL crl; // 吊销列表
    protected final byte[] info; // 原文信息
    protected final List<byte[]> signedInfos = new ArrayList<>(); // 签名数据

    protected X509Certificate[] subjects; // 多人签名证书
    private boolean verifySigned = true;

    protected CertSignedVerifier(X509Certificate rootCert, X509CRL crl, byte[] info) {
        this.rootCert = rootCert;
        this.crl = crl;
        this.info = info;
    }

    /**
     * 根据加载的根证进行证书验证
     */
    public final void verify() {
        for (X509Certificate subject : subjects) {
            String subjectCN = X509CertUtils.getCertInfo(subject, X509CertInfo.SUBJECT_CN);

            // 获取根证书
            if (rootCert == null) {
                throw new SecurityException("[" + subjectCN + "]的根证未受信任");
            }

            // 校验
            verifyCertDate(subject);
            verifyIssuingSign(subject, rootCert);
            if (crl != null) {
                verifyCrlRevoke(subject, crl);
            }
        }

        // 签名验证
        if (verifySigned) {
            verifySigned();
        }
    }

    /**
     * 验证签名
     */
    public abstract void verifySigned();

    /**
     * 校验证书是否过期
     * @param subject
     */
    public static void verifyCertDate(X509Certificate subject) {
        String subjectCN = null;
        try {
            subjectCN = X509CertUtils.getCertInfo(subject, X509CertInfo.SUBJECT_CN);
            subject.checkValidity(new Date());
        } catch (CertificateExpiredException e) {
            throw new SecurityException("[" + subjectCN + "]已过期", e);
        } catch (CertificateNotYetValidException e) {
            throw new SecurityException("[" + subjectCN + "]尚未生效", e);
        }
    }

    /**
     * 校验是否由指定根证签发
     * @param subject
     * @param root
     */
    public static void verifyIssuingSign(X509Certificate subject, X509Certificate root) {
        String subjectCN = null;
        try {
            subjectCN = X509CertUtils.getCertInfo(subject, X509CertInfo.SUBJECT_CN);
            subject.verify(root.getPublicKey());
        } catch (SignatureException e) {
            throw new SecurityException("[" + subjectCN + "]的根证未受信任", e);
        } catch (Exception e) {
            throw new SecurityException("根证验签出错", e);
        }

    }

    /**
     * 校验是否已被吊销
     * @param subject
     * @param crl
     */
    public static void verifyCrlRevoke(X509Certificate subject, X509CRL crl) {
        String subjectCN = X509CertUtils.getCertInfo(subject, X509CertInfo.SUBJECT_CN);
        if (crl.isRevoked(subject)) {
            throw new SecurityException("[" + subjectCN + "]已被吊销");
        }
    }

    public X509Certificate[] getSubjects() {
        return this.subjects;
    }

    public byte[] getInfo() {
        return this.info;
    }

    public List<byte[]> getSignedInfo() {
        return this.signedInfos;
    }

    public void setVerifySigned(boolean verifySigned) {
        this.verifySigned = verifySigned;
    }

}

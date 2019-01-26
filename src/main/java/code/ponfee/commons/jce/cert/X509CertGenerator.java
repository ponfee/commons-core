package code.ponfee.commons.jce.cert;

import java.io.IOException;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Vector;

import javax.annotation.Nullable;

import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import code.ponfee.commons.jce.Providers;
import code.ponfee.commons.jce.RSASignAlgorithms;
import code.ponfee.commons.util.ObjectUtils;
import sun.security.pkcs10.PKCS10;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.AlgorithmId;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateExtensions;
import sun.security.x509.CertificateIssuerName;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateValidity;
import sun.security.x509.CertificateVersion;
import sun.security.x509.CertificateX509Key;
import sun.security.x509.ExtendedKeyUsageExtension;
import sun.security.x509.KeyUsageExtension;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

/**
 * 证书生成工具类
 * @author fupf
 */
@SuppressWarnings({ "restriction" })
public class X509CertGenerator {

    // ------------------------create root ca cert of self sign -----------------------------
    public static X509Certificate createRootCert(String issuer, RSASignAlgorithms sigAlg, PrivateKey privateKey, 
                                                 PublicKey publicKey,  Date notBefore, Date notAfter) {
        return createRootCert(null, issuer, sigAlg, privateKey, publicKey, notBefore, notAfter);
    }

    /**
     * 创建CA根证书（自签名）
     * @param sn
     * @param issuer
     * @param sigAlg
     * @param privateKey
     * @param publicKey
     * @param notBefore
     * @param notAfter
     * @return
     */
    public static X509Certificate createRootCert(BigInteger sn, String issuer, RSASignAlgorithms sigAlg, PrivateKey privateKey, 
                                                 PublicKey publicKey, Date notBefore, Date notAfter) {
        PKCS10 pkcs10 = createPkcs10(issuer, privateKey, publicKey, sigAlg);
        X509CertInfo certInfo = createCertInfo(sn, pkcs10, notBefore, notAfter, createExtensions(true));
        return selfSign(privateKey, certInfo);
    }

    // ---------------------------------create subject cert of ca sign ------------------------------
    public static X509Certificate createSubjectCert(X509Certificate caCert, PrivateKey caKey, String subject,
                                                    RSASignAlgorithms sigAlg, PrivateKey privateKey, 
                                                    PublicKey publicKey, Date notBefore, Date notAfter) {
        return createSubjectCert(caCert, caKey, null, subject, sigAlg, privateKey, publicKey, notBefore, notAfter);
    }

    /**
     * 创建证书并用根证签发
     * @param caCert
     * @param caKey
     * @param sn
     * @param subject
     * @param sigAlg
     * @param privateKey
     * @param publicKey
     * @param notBefore
     * @param notAfter
     * @return
     */
    public static X509Certificate createSubjectCert(X509Certificate caCert, PrivateKey caKey, BigInteger sn,
                                                    String subject, RSASignAlgorithms sigAlg, PrivateKey privateKey,
                                                    PublicKey publicKey, Date notBefore, Date notAfter) {
        PKCS10 pkcs10 = createPkcs10(subject, privateKey, publicKey, sigAlg);
        X509CertInfo certInfo = createCertInfo(sn, pkcs10, notBefore, notAfter, createExtensions(false));
        return caSign(caCert, caKey, certInfo);
    }

    public static X509Certificate createSubjectCert(X509Certificate caCert, PrivateKey caKey, 
                                                    PKCS10 pkcs10, Date notBefore, Date notAfter) {
        return createSubjectCert(caCert, caKey, null, pkcs10, notBefore, notAfter);
    }

    /**
     * pkcs10请求CA签发证书
     * @param caCert
     * @param caKey
     * @param sn
     * @param pkcs10
     * @param notBefore
     * @param notAfter
     * @return
     */
    public static X509Certificate createSubjectCert(X509Certificate caCert, PrivateKey caKey, BigInteger sn,
                                                    PKCS10 pkcs10, Date notBefore, Date notAfter) {
        X509CertInfo certInfo = createCertInfo(sn, pkcs10, notBefore, notAfter, createExtensions(false));
        return caSign(caCert, caKey, certInfo);
    }

    // -------------------------------------------create pkcs10 ------------------------------------------
    /**
     * 创建pkcs10
     * @param subject
     * @param privateKey
     * @param publicKey
     * @param sigAlg
     * @return
     */
    public static PKCS10 createPkcs10(String subject, PrivateKey privateKey,
                                      PublicKey publicKey, RSASignAlgorithms sigAlg) {
        try {
            PKCS10 pkcs10 = new PKCS10(publicKey);
            Signature signature = Signature.getInstance(sigAlg.name());
            signature.initSign(privateKey);
            pkcs10.encodeAndSign(new X500Name(subject), signature);
            return pkcs10;
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }

    // -------------------------------------------create cert ext-----------------------------------------
    /**
     * 创建默认的扩展信息
     * @param isCA {@code true} is create CA cert
     *             {@code false} is create subject cert
     * @return
     */
    public static CertificateExtensions createExtensions(boolean isCA) {
        try {
            CertificateExtensions extensions = new CertificateExtensions();
            //byte[] userData;

            // 密钥用法
            KeyUsageExtension keyUsage = new KeyUsageExtension();
            keyUsage.set(KeyUsageExtension.DIGITAL_SIGNATURE, true); // 支持数据签名
            if (isCA) {
                //userData = "Digital Signature, Certificate Signing, Off-line CRL Signing, CRL Signing (86)".getBytes();

                keyUsage.set(KeyUsageExtension.KEY_ENCIPHERMENT, true); // 支持密钥加密
                keyUsage.set(KeyUsageExtension.KEY_AGREEMENT, true); // 支持密钥协议
                keyUsage.set(KeyUsageExtension.KEY_CERTSIGN, true); // 支持证书签名
                keyUsage.set(KeyUsageExtension.CRL_SIGN, true); // 支持吊销列表签名
            } else {
                //userData = "Digital Signature, Data Encipherment (90)".getBytes();

                keyUsage.set(KeyUsageExtension.DATA_ENCIPHERMENT, true); // 支持数据加密

                // 增强密钥用法
                Vector<ObjectIdentifier> extendedKeyUsage = new Vector<>();
                extendedKeyUsage.add(new ObjectIdentifier(new int[] { 1, 3, 6, 1, 5, 5, 7, 3, 3 })); // 代码签名
                extensions.set(ExtendedKeyUsageExtension.NAME, new ExtendedKeyUsageExtension(extendedKeyUsage));
            }
            extensions.set(KeyUsageExtension.NAME, keyUsage);

            /*// 版本号：v1、v2、v3，此扩展信息必须是v3版本，生成一个extension对象参数分别为oid，是否关键扩展，byte[]型的内容值
            ObjectIdentifier oid = new ObjectIdentifier(new int[] { 1, 22 }); // 扩展域:第1位最大为2，第2位最大为39，后续不明
            userData = ObjectUtils.concat(new byte[] { 0x04, (byte) userData.length }, userData); // flag,data length, data
            // PKCS7Signature验证签名会报错：java.security.SignatureException: Certificate has unsupported critical extension(s)
            extensions.set("UserData", new sun.security.x509.Extension(oid, true, userData)); */

            return extensions;
        } catch (IOException e) {
            throw new SecurityException(e);
        }
    }

    // -------------------------------------------private methods----------------------------------------
    /**
     * 根据pkcs10创建证书
     * @param sn
     * @param pkcs10
     * @param notBefore
     * @param notAfter
     * @param extensions
     * @return
     */
    private static X509CertInfo createCertInfo(@Nullable BigInteger sn, PKCS10 pkcs10, Date notBefore, 
                                               Date notAfter, CertificateExtensions extensions) {
        if (sn == null) {
            //sn = BigInteger.valueOf(ThreadLocalRandom.current().nextLong() & Long.MAX_VALUE);
            sn = new BigInteger(1, ObjectUtils.uuid());
        }
        try {
            // 验证pkcs10
            PKCS10CertificationRequest req = new PKCS10CertificationRequest(pkcs10.getEncoded());
            JcaContentVerifierProviderBuilder builder = new JcaContentVerifierProviderBuilder();
            builder.setProvider(Providers.BC);
            if (!req.isSignatureValid(builder.build(req.getSubjectPublicKeyInfo()))) {
                throw new SecurityException("Invalid pkcs10 signature data.");
            }

            /*org.bouncycastle.jce.PKCS10CertificationRequest req = 
            new org.bouncycastle.jce.PKCS10CertificationRequest(pkcs10.getEncoded());
            if (!req.verify()) {
                throw new SecurityException("Invalid pkcs10 signature data.");
            }*/

            AlgorithmId signAlg = AlgorithmId.get(req.getSignatureAlgorithm().getAlgorithm().getId());
            X509CertInfo x509certInfo = new X509CertInfo();
            x509certInfo.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
            x509certInfo.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(sn));
            x509certInfo.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(signAlg));
            x509certInfo.set(X509CertInfo.SUBJECT, pkcs10.getSubjectName());
            x509certInfo.set(X509CertInfo.KEY, new CertificateX509Key(pkcs10.getSubjectPublicKeyInfo()));
            x509certInfo.set(X509CertInfo.VALIDITY, new CertificateValidity(notBefore, notAfter));
            if (extensions != null) {
                x509certInfo.set(X509CertInfo.EXTENSIONS, extensions);
            }
            return x509certInfo;
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }

    /**
     * 自签名证书（根证书）
     * @param caKey
     * @param caCertInfo
     * @return
     */
    private static X509Certificate selfSign(PrivateKey caKey, X509CertInfo caCertInfo) {
        try {
            CertificateAlgorithmId algId = (CertificateAlgorithmId) caCertInfo.get(X509CertInfo.ALGORITHM_ID);
            caCertInfo.set(X509CertInfo.ISSUER, caCertInfo.get(X509CertInfo.SUBJECT));
            X509CertImpl signedCert = new X509CertImpl(caCertInfo);
            signedCert.sign(caKey, algId.get(CertificateAlgorithmId.ALGORITHM).getName()); // 签名
            return signedCert;
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }

    /**
     * CA签名证书
     * @param caCert
     * @param caKey
     * @param subjectCertInfo
     * @return
     */
    private static X509Certificate caSign(X509Certificate caCert, PrivateKey caKey, X509CertInfo subjectCertInfo) {
        try {
            // 从CA的证书中提取签发者的信息
            X509CertImpl caCertImpl = new X509CertImpl(caCert.getEncoded());

            // 获取X509CertInfo对象
            X509CertInfo caCertInfo = (X509CertInfo) caCertImpl.get(X509CertImpl.NAME + "." + X509CertImpl.INFO);

            // 获取X509Name类型的签发者信息
            X500Name issuer = (X500Name) caCertInfo.get(X509CertInfo.SUBJECT + "." + CertificateIssuerName.DN_NAME);

            subjectCertInfo.set(X509CertInfo.ISSUER, issuer);
            X509CertImpl signedCert = new X509CertImpl(subjectCertInfo);
            signedCert.sign(caKey, caCert.getSigAlgName()); // 使用CA私钥对其签名
            return signedCert;
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }

}

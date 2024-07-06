package test.jce.cert;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Date;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

import cn.ponfee.commons.jce.RSASignAlgorithms;
import cn.ponfee.commons.jce.cert.CertSignedVerifier;
import cn.ponfee.commons.jce.cert.X509CertGenerator;
import cn.ponfee.commons.jce.cert.X509CertInfo;
import cn.ponfee.commons.jce.cert.X509CertUtils;
import cn.ponfee.commons.jce.pkcs.PKCS1Signature;
import cn.ponfee.commons.jce.security.KeyStoreResolver;
import cn.ponfee.commons.jce.security.KeyStoreResolver.KeyStoreType;
import cn.ponfee.commons.jce.security.RSACryptor;
import cn.ponfee.commons.jce.security.RSACryptor.RSAKeyPair;
import cn.ponfee.commons.jce.security.RSAPrivateKeys;
import cn.ponfee.commons.jce.security.RSAPublicKeys;
import cn.ponfee.commons.resource.ResourceLoaderFacade;
import cn.ponfee.commons.util.Bytes;
import cn.ponfee.commons.date.Dates;

public class KeyStoreResolverTester {

    public @Test void testLoad() {
        KeyStoreResolver resolver = new KeyStoreResolver(KeyStoreType.PKCS12, ResourceLoaderFacade.getResource("cas_test.pfx").getStream(), "1234");
        String alias = resolver.listAlias().get(0);
        test0((RSAPrivateKey)resolver.getPrivateKey(alias, "1234"), (RSAPublicKey)resolver.getCertificate(alias).getPublicKey());

        String pem = X509CertUtils.exportToPem(resolver.getX509CertChain()[0]);
        resolver = new KeyStoreResolver(KeyStoreType.JKS);
        resolver.setCertificateEntry("pem", X509CertUtils.loadPemCert(pem));
        System.out.println(resolver.getKeyStore()); 
    }

    public @Test void testCreateCert() throws Exception {
        Date before = Dates.toDate("2017-03-01 00:00:00"), after = Dates.toDate("2027-08-01 00:00:00");
        RSAKeyPair p1 = RSACryptor.generateKeyPair(2048), p2 = RSACryptor.generateKeyPair(2048);
        RSASignAlgorithms alg = RSASignAlgorithms.SHA256withRSA;
        String caPwd = "1234", subjectPwd = "123456";
        String _issuer = "CN=ca,OU=hackwp,O=wp,L=BJ,S=BJ,C=CN";
        String _subject = "CN=subject,OU=hackwp,O=wp,L=BJ,S=BJ,C=CN";

        // --------------------------------------------------
        X509Certificate ccert = X509CertGenerator.createRootCert(null, _issuer, alg, p1.getPrivateKey(), p1.getPublicKey(), before, after);
        KeyStoreResolver ca = new KeyStoreResolver(KeyStoreType.PKCS12);
        ca.setKeyEntry(X509CertUtils.getCertInfo(ccert, X509CertInfo.SUBJECT_CN), p1.getPrivateKey(), caPwd, new X509Certificate[] { ccert });
        test0((RSAPrivateKey)ca.getPrivateKey("1234"), (RSAPublicKey)ca.getCertificate().getPublicKey());

        System.out.println("\n\n------------------------------------------------------------\n\n");

        // --------------------------------------------------
        X509Certificate scert = X509CertGenerator.createSubjectCert(ccert, p1.getPrivateKey(), null, _subject, alg, p2.getPrivateKey(), p2.getPublicKey(), before, after);
        KeyStoreResolver subject = new KeyStoreResolver(KeyStoreType.PKCS12);
        String scn = X509CertUtils.getCertInfo(scert, X509CertInfo.SUBJECT_CN);
        subject.setKeyEntry(scn, p2.getPrivateKey(), subjectPwd, new X509Certificate[] { scert, ccert });
        test0((RSAPrivateKey)subject.getPrivateKey(subjectPwd), (RSAPublicKey)subject.getCertificate().getPublicKey());

        // --------------------------------------------------
        ca.export(new FileOutputStream("d:/test/ca.pfx"), caPwd);
        subject.export(new FileOutputStream("d:/test/subject.pfx"), subjectPwd);
    }

    public @Test void testVerify() throws Exception {
        X509Certificate root = new KeyStoreResolver(KeyStoreType.PKCS12, new FileInputStream("d:/test/ca.pfx"), "1234").getX509CertChain()[0];
        X509Certificate[] subjectChain = new KeyStoreResolver(KeyStoreType.PKCS12, new FileInputStream("d:/test/subject.pfx"), "123456").getX509CertChain();

        // 方法一：获取证书的签名内容
        System.out.println(PKCS1Signature.verify(root.getTBSCertificate(), root.getSignature(), root));
        System.out.println(PKCS1Signature.verify(subjectChain[0].getTBSCertificate(), subjectChain[0].getSignature(), subjectChain[1]));

        // 方法二：通过证书接口验证cert.verify(publicKey);
        CertSignedVerifier.verifyIssuingSign(root, root);
        CertSignedVerifier.verifyIssuingSign(subjectChain[0], subjectChain[1]);
    }

    public @Test void test1() throws Exception {
        Date before = Dates.toDate("2017-03-01 00:00:00"), after = Dates.toDate("2027-08-01 00:00:00");
        RSAKeyPair p1 = RSACryptor.generateKeyPair(2048), p2 = RSACryptor.generateKeyPair(2048);
        RSASignAlgorithms alg = RSASignAlgorithms.SHA256withRSA;
        String caPwd = "1234", subjectPwd = "123456";
        String _issuer = "CN=ca,OU=hackwp,O=wp,L=BJ,S=BJ,C=CN";
        String _subject = "CN=subject,OU=hackwp,O=wp,L=BJ,S=BJ,C=CN";

        // --------------------------------------------------
        X509Certificate ccert = X509CertGenerator.createRootCert(null, _issuer, alg, p1.getPrivateKey(), p1.getPublicKey(), before, after);
        KeyStoreResolver ca = new KeyStoreResolver(KeyStoreType.PKCS12);

        byte[] pkcs8Key = Base64.getDecoder().decode(RSAPrivateKeys.toEncryptedPkcs8(p1.getPrivateKey(), caPwd));
        ca.setKeyEntry(X509CertUtils.getCertInfo(ccert, X509CertInfo.SUBJECT_CN), pkcs8Key, new X509Certificate[] { ccert });
        //ca.setKeyEntry(X509CertUtils.getCertInfo(ccert, X509CertInfo.SUBJECT_CN), p1.getPrivateKey(), caPwd, new X509Certificate[] { ccert });
        test0((RSAPrivateKey) ca.getPrivateKey("1234"), (RSAPublicKey) ca.getCertificate().getPublicKey());

        System.out.println("\n\n------------------------------------------------------------\n\n");

        // --------------------------------------------------
        X509Certificate scert =
            X509CertGenerator.createSubjectCert(ccert, p1.getPrivateKey(), null, _subject, alg, p2.getPrivateKey(), p2.getPublicKey(), before, after);
        KeyStoreResolver subject = new KeyStoreResolver(KeyStoreType.PKCS12);
        String scn = X509CertUtils.getCertInfo(scert, X509CertInfo.SUBJECT_CN);
        pkcs8Key = Base64.getDecoder().decode(RSAPrivateKeys.toEncryptedPkcs8(p2.getPrivateKey(), subjectPwd));
        subject.setKeyEntry(scn, pkcs8Key, new X509Certificate[] { scert, ccert });
        //subject.setKeyEntry(scn, p2.getPrivateKey(), subjectPwd, new X509Certificate[] { scert, ccert });
        test0((RSAPrivateKey) subject.getPrivateKey(subjectPwd), (RSAPublicKey) subject.getCertificate().getPublicKey());

        // --------------------------------------------------
        ca.export(new FileOutputStream("d:/test/ca.pfx"), caPwd);
        subject.export(new FileOutputStream("d:/test/subject.pfx"), subjectPwd);
    }

    public @Test void test2() throws Exception {
        RSAKeyPair p1 = RSACryptor.generateKeyPair(2048);
        String caPwd = "1234";
        System.out.println(RSAPrivateKeys.fromEncryptedPkcs8(RSAPrivateKeys.toEncryptedPkcs8(p1.getPrivateKey(), caPwd), caPwd));
        System.out.println(RSAPrivateKeys.fromEncryptedPkcs8Pem(RSAPrivateKeys.toEncryptedPkcs8Pem(p1.getPrivateKey(), caPwd), caPwd));
        System.out.println(RSAPrivateKeys.fromPkcs1(RSAPrivateKeys.toPkcs1(p1.getPrivateKey())));
        System.out.println(RSAPrivateKeys.fromPkcs8(RSAPrivateKeys.toPkcs8(p1.getPrivateKey())));

        System.out.println(RSAPublicKeys.fromPkcs1(RSAPublicKeys.toPkcs1(p1.getPublicKey())));
        System.out.println(RSAPublicKeys.fromPkcs8(RSAPublicKeys.toPkcs8(p1.getPublicKey())));
        System.out.println(RSAPublicKeys.fromPkcs8Pem(RSAPublicKeys.toPkcs8Pem(p1.getPublicKey())));
    }

    // -----------------------------------------------------------------------------------
    private static void test0(RSAPrivateKey privateKey, RSAPublicKey publicKey) {
        try {
            System.out.println("=============================加密测试==============================");
            //byte[] data = "加解密测试".getBytes();
            byte[] data = IOUtils.toByteArray(ResourceLoaderFacade.getResource("2.png").getStream());
            System.out.println("加密前：");
            System.out.println(Bytes.dumpHex(ArrayUtils.subarray(data, 0, 100)));
            byte[] encodedData = RSACryptor.encrypt(data, publicKey);
            System.out.println("加密后：");
            System.out.println(Bytes.dumpHex(ArrayUtils.subarray(encodedData, 0, 100)));
            System.out.println("解密后：");
            System.out.println(Bytes.dumpHex(ArrayUtils.subarray(RSACryptor.decrypt(encodedData, privateKey), 0, 100)));

            System.out.println("\n\n===========================签名测试=========================");
            data = Base64.getDecoder().decode("");
            byte[] signed = RSACryptor.signSha1(data, privateKey);
            String hex = Hex.encodeHexString(signed);
            System.out.println("签名结果：" + hex.length() + " --> " + hex);
            System.out.println("验签结果：" + RSACryptor.verifySha1(data, publicKey, signed));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package test.jce.cert;

import java.io.FileInputStream;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;

import org.junit.Test;

import code.ponfee.commons.http.Http;
import code.ponfee.commons.jce.pkcs.CryptoMessageSyntax;
import code.ponfee.commons.jce.pkcs.PKCS7Envelope;
import code.ponfee.commons.jce.pkcs.PKCS7Envelope.AlgorithmMapping;
import code.ponfee.commons.jce.security.KeyStoreResolver;
import code.ponfee.commons.jce.security.KeyStoreResolver.KeyStoreType;
import code.ponfee.commons.jce.security.RSACryptor;

public class PKCS7EnvelopeTester {

    public @Test void testEnvelop() throws Exception {
        KeyStoreResolver resolver = new KeyStoreResolver(KeyStoreType.PKCS12, new FileInputStream("d:/test/subject.pfx"), "123456");
        X509Certificate cert = resolver.getX509CertChain()[0];
        PrivateKey privateKey = resolver.getPrivateKey("123456");
        //byte[] data = Streams.file2bytes(MavenProjects.getTestJavaFile(EnvelopeTester.class));
        byte[] data = Http.get("http://www.baidu.com").download();
        long start = System.currentTimeMillis();
        //System.out.println(Bytes.hexDump(data));
        System.out.println("origin len------------" + data.length);
        System.out.println("===============================================");

        byte[] enveloped = PKCS7Envelope.envelop(data, cert, AlgorithmMapping.DESede_CBC);
        System.out.println("cost"+(System.currentTimeMillis()-start));
        start = System.currentTimeMillis();
        //System.out.println(Bytes.hexDump(enveloped));
        System.out.println("enveloped len------------" + enveloped.length);
        System.out.println("===============================================");

        byte[] unveloped = PKCS7Envelope.unenvelop(enveloped, cert, privateKey);
        System.out.println(new String(unveloped));
        //System.out.println("===============================================");

        unveloped = CryptoMessageSyntax.unenvelop(enveloped, cert, privateKey);
        System.out.println(new String(unveloped));

        byte[] encrypted = RSACryptor.encrypt(data, (RSAPrivateKey)privateKey);
        //System.out.println(By`tes.hexDump(encrypted));
        System.out.println("cost"+(System.currentTimeMillis()-start));
        start = System.currentTimeMillis();
        System.out.println("encrypted len------------" + encrypted.length);
    }

}

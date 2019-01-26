package test.jce.cert;

import java.io.IOException;
import java.io.StringReader;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.util.Base64;

import javax.crypto.Cipher;

import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.junit.Test;

import code.ponfee.commons.jce.Providers;

public class TestPem {

    public @Test void testPEMKeyPair() throws IOException {
        Security.addProvider(Providers.BC);

        String privateKeyString = "-----BEGIN RSA PRIVATE KEY-----\n" 
            + "MIICXQIBAAKBgQDKQtJAyCu5FHwDncK2LB/J5ClJhulGggyc7vwtji6TJHtSJfgD\n"
            + "4TLpHRIHh/cHqf3brhpQtYB9yjKlwogji/OzedY2mdTdSOP8O6suJYu3QENN2xG/\n" 
            + "HvT8UiYK3feVLbJtukhJm7eSuwfMDsjHh4AK7g11fVs6EmY+foh3mjoKLQIDAQAB\n"
            + "AoGAR8N/wDaFtOx8t/fAv0xWlxaaQ5lXqYm5GfF9jlhVVCXsj5AjOJUtsCJ9ZCis\n" 
            + "0I5TIR/b/Gj5xyf34nJsRViBxbnf6XdLGyXmzsNxWZoWbM70JaqU3iQKm605/EnD\n"
            + "vPgrI0AMfc/h6Kog0zLrKWKkna+wE5839yMmm7WPqgvxSc0CQQDoud5e3yZu/1e+\n" 
            + "7piFZZl6StAecl+k10Wq5kzJeVQRffDB3JCca65H/W1EZIzEh76pUNr7SYAIIcbK\n"
            + "jzOdbj1vAkEA3n0AudM3mBzklLEUSHs1ZSqFkUMNP9MNIikwkZ/9Z2AlhW5gnwiv\n" 
            + "dgeXonTqlTFux4e7uyKZoJpJcKAgmMicIwJBAIMl206TalE6y/Po+UKTUr470rSV\n"
            + "t5hpR/Va+wK+wMVqt3ZIGaZMeFZRVnYoQ7us06EO05iwftoWTrRvpqKdMTkCQBkE\n"
            + "QzWhy0l+TjFt69Luj6Vtb5FS0cWQbJSfvwdQzwR1qiJjs9eN+XSzC9jHfq0B3uvu\n" 
            + "lixHirClSIayapfjTrMCQQCM8d97py4u9hCdCpsHBDt54dXkHsDA2abNzaPri/YA\n"
            + "pNFZGrfXKVGSLFOfsuf7Wj+yL7ew6ZVKOMYdJ+zb9Wwv\n" 
            + "-----END RSA PRIVATE KEY-----"; // 128

        PEMParser privatePemParser = new PEMParser(new StringReader(privateKeyString));
        Object privateObject = privatePemParser.readObject();
        System.out.println("PEMParser.readObject(): " + privateObject.getClass());
        if (privateObject instanceof PEMKeyPair) {
            PEMKeyPair pemKeyPair = (PEMKeyPair) privateObject;
            System.out.println("private: " + pemKeyPair.getPrivateKeyInfo());
            System.out.println("public: " + pemKeyPair.getPublicKeyInfo());

            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(Providers.BC);
            PublicKey publicKey = converter.getPublicKey(pemKeyPair.getPublicKeyInfo());
            PrivateKey privateKey = converter.getPrivateKey(pemKeyPair.getPrivateKeyInfo());

            String message = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";

            System.out.println("\npublicKey key encrypt.");
            byte[] encripted = encrypt(publicKey, message.getBytes());
            System.out.println("encripted: " + Base64.getEncoder().encodeToString(encripted));
            byte[] decrypted = decrypt(privateKey, encripted);
            System.out.println("decrypted: " + new String(decrypted));

            System.out.println("\nprivate key encrypt.");
            encripted = encrypt(privateKey, message.getBytes());
            System.out.println("encripted: " + Base64.getEncoder().encodeToString(encripted));
            decrypted = decrypt(publicKey, encripted);
            System.out.println("decrypted: " + new String(decrypted));
        }
        privatePemParser.close();
    }

    public @Test void testSubjectPublicKeyInfo() throws IOException {
        String publicKeyString = "-----BEGIN PUBLIC KEY-----\n" 
                               + "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDKQtJAyCu5FHwDncK2LB/J5ClJ\n"
                               + "hulGggyc7vwtji6TJHtSJfgD4TLpHRIHh/cHqf3brhpQtYB9yjKlwogji/OzedY2\n" 
                               + "mdTdSOP8O6suJYu3QENN2xG/HvT8UiYK3feVLbJtukhJm7eSuwfMDsjHh4AK7g11\n"
                               + "fVs6EmY+foh3mjoKLQIDAQAB\n" 
                               + "-----END PUBLIC KEY-----";

        PEMParser publicPemParser = new PEMParser(new StringReader(publicKeyString));
        Object publicObject = publicPemParser.readObject();
        System.out.println("PEMParser.readObject(): " + publicObject.getClass());
        if (publicObject instanceof SubjectPublicKeyInfo) {
            SubjectPublicKeyInfo publicSubjectPublicKeyInfo = (SubjectPublicKeyInfo) publicObject;
            System.out.println("public: " + publicSubjectPublicKeyInfo);
        }
        publicPemParser.close();
    }

    private static byte[] encrypt(Key pubkey, byte[] data) {
        try {
            Cipher rsa;
            rsa = Cipher.getInstance("RSA", "BC");
            rsa.init(Cipher.ENCRYPT_MODE, pubkey);
            return rsa.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static byte[] decrypt(Key decryptionKey, byte[] encrypted) {
        try {
            Cipher rsa;
            rsa = Cipher.getInstance("RSA", "BC");
            rsa.init(Cipher.DECRYPT_MODE, decryptionKey);
            return rsa.doFinal(encrypted);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}

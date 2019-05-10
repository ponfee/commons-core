package code.ponfee.commons.jce.security;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.junit.Test;

import code.ponfee.commons.jce.security.RSACryptor.RSAKeyPair;

public class RSAPrivateKeysTest {

    @Test
    public void test1() {
        RSAKeyPair keyPair = RSACryptor.generateKeyPair(2018);
        RSAPrivateKey privKey = keyPair.getPrivateKey();

        System.out.println("\n================================================toPkcs1");
        System.out.println(RSAPrivateKeys.toPkcs1(privKey));

        System.out.println("\n================================================toPkcs1Pem");
        System.out.println(RSAPrivateKeys.toPkcs1Pem(privKey));

        System.out.println("\n================================================toPkcs8");
        System.out.println(RSAPrivateKeys.toPkcs8(privKey));

        System.out.println("\n================================================toEncryptedPkcs8");
        System.out.println(RSAPrivateKeys.toEncryptedPkcs8(privKey, "123456"));

        System.out.println("\n================================================toEncryptedPkcs8Pem");
        System.out.println(RSAPrivateKeys.toEncryptedPkcs8Pem(privKey, "123456"));
    }

    @Test
    public void test2() {
        RSAKeyPair keyPair = RSACryptor.generateKeyPair(2018);
        RSAPublicKey pubKey = keyPair.getPublicKey();

        System.out.println("\n================================================toPkcs1");
        System.out.println(RSAPublicKeys.toPkcs1(pubKey));

        System.out.println("\n================================================toPkcs8");
        System.out.println(RSAPublicKeys.toPkcs8(pubKey));

        System.out.println("\n================================================toPkcs8Pem");
        System.out.println(RSAPublicKeys.toPkcs8Pem(pubKey));
    }
}

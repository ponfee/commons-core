package test.jce.rsa;

import code.ponfee.commons.jce.security.RSACryptor;
import code.ponfee.commons.jce.security.RSACryptor.RSAKeyPair;
import code.ponfee.commons.jce.security.RSAPrivateKeys;
import code.ponfee.commons.jce.security.RSAPublicKeys;

public class RSAKeyTest {

    public static void main(String[] args) {
        RSAKeyPair keyPair = RSACryptor.generateKeyPair(2048);

        System.out.println("pkcs1 pub: " + keyPair.toPkcs1PublicKey());
        System.out.println("pkcs8 pub: " + keyPair.toPkcs8PublicKey());
        System.out.println("\n");
        System.out.println("pkcs1 pri: " + keyPair.toPkcs1PrivateKey());
        System.out.println("pkcs8 pri: " + keyPair.toPkcs8PrivateKey());
        System.out.println("\n");
        System.out.println("pkcs1 pub: " + RSAPublicKeys.fromPkcs1(keyPair.toPkcs1PublicKey()));
        System.out.println("pkcs8 pub: " + RSAPublicKeys.fromPkcs8(keyPair.toPkcs8PublicKey()));
        System.out.println("\n");
        System.out.println("pkcs1 pri: " + RSAPrivateKeys.fromPkcs1(keyPair.toPkcs1PrivateKey()));
        System.out.println("pkcs8 pri: " + RSAPrivateKeys.fromPkcs8(keyPair.toPkcs8PrivateKey()));
    }

}

package cn.ponfee.commons.jce.security;

import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;

import org.apache.commons.lang3.tuple.Pair;

public class ECDSASignerTest {

    public static void main(String[] args) {
        Pair<ECPublicKey, ECPrivateKey> keyPair = ECDSASigner.generateKeyPair(571);
        byte[] data = "123456".getBytes();
        byte[] signed = ECDSASigner.signSha512(data, keyPair.getRight());
        System.out.println(signed.length);
        System.out.println(ECDSASigner.verifySha512(data, signed, keyPair.getLeft()));

        /*byte[] encrypted = encrypt(data, getPublicKey(keyMap));
        byte[] decrypted = decrypt(encrypted, getPrivateKey(keyMap));
        System.out.println(new String(decrypted));*/
    }
}

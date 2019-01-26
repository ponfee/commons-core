package code.ponfee.commons.jce.security;

import java.security.interfaces.ECKey;
import java.util.Map;

public class ECDSASignerTest {

    public static void main(String[] args) {
        Map<String, ECKey> keyMap = ECDSASigner.generateKeyPair(571);
        byte[] data = "123456".getBytes();
        byte[] signed = ECDSASigner.signSha512(data, ECDSASigner.getPrivateKey(keyMap));
        System.out.println(signed.length);
        System.out.println(ECDSASigner.verifySha512(data, signed, ECDSASigner.getPublicKey(keyMap)));

        /*byte[] encrypted = encrypt(data, getPublicKey(keyMap));
        byte[] decrypted = decrypt(encrypted, getPrivateKey(keyMap));
        System.out.println(new String(decrypted));*/
    }
}

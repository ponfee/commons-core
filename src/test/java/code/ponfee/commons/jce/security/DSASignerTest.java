package code.ponfee.commons.jce.security;

import java.security.interfaces.DSAKey;
import java.util.Map;

public class DSASignerTest {

    public static void main(String[] args) {
        Map<String, DSAKey> key = DSASigner.initKey();
        byte[] data = "123456".getBytes();
        byte[] signed = DSASigner.sign(data, DSASigner.getPrivateKey(key));
        boolean flag = DSASigner.verify(data, DSASigner.getPublicKey(key), signed);
        System.out.println(flag);
    }
}

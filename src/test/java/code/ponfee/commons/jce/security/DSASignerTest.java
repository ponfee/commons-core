package code.ponfee.commons.jce.security;

import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;

import org.apache.commons.lang3.tuple.Pair;

import code.ponfee.commons.jce.Providers;

public class DSASignerTest {

    public static void main(String[] args) {
        Providers.set(Providers.BC);
        Pair<DSAPublicKey, DSAPrivateKey> keyPair = DSASigner.initKey();
        byte[] data = "123456".getBytes();
        byte[] signed = DSASigner.sign(data, keyPair.getRight());
        boolean flag = DSASigner.verify(data, keyPair.getLeft(), signed);
        System.out.println(flag);
    }
}

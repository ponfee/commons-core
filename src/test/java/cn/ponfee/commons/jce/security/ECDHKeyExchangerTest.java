package cn.ponfee.commons.jce.security;

import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;

import org.apache.commons.lang3.tuple.Pair;

import cn.ponfee.commons.jce.Providers;

public class ECDHKeyExchangerTest {

    public static void main(String[] args) {
        Providers.set(Providers.BC);
        Pair<ECPublicKey, ECPrivateKey> partA = ECDHKeyExchanger.initPartAKey(192);
        Pair<ECPublicKey, ECPrivateKey> partB = ECDHKeyExchanger.initPartBKey(partA.getLeft());
        byte[] data = "123456".getBytes();

        // 乙方加密甲方解密
        byte[] encrypted = ECDHKeyExchanger.encrypt(data, ECDHKeyExchanger.genSecretKey(partB.getRight(), partA.getLeft()));
        byte[] decrypted = ECDHKeyExchanger.decrypt(encrypted, ECDHKeyExchanger.genSecretKey(partA.getRight(), partB.getLeft()));
        System.out.println(new String(decrypted));

        // 甲方加密乙方解密
        encrypted = ECDHKeyExchanger.encrypt(data, ECDHKeyExchanger.genSecretKey(partA.getRight(), partB.getLeft()));
        decrypted = ECDHKeyExchanger.decrypt(encrypted, ECDHKeyExchanger.genSecretKey(partB.getRight(), partA.getLeft()));
        System.out.println(new String(decrypted));
    }
}

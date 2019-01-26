package code.ponfee.commons.jce.security;

import java.util.Map;

public class ECDHKeyExchangerTest {

    public static void main(String[] args) {
        Map<String, byte[]> partA = ECDHKeyExchanger.initPartAKey(192);
        Map<String, byte[]> partB = ECDHKeyExchanger.initPartBKey(ECDHKeyExchanger.getPublicKey(partA));
        byte[] data = "123456".getBytes();

        // 乙方加密甲方解密
        byte[] encrypted = ECDHKeyExchanger.encrypt(data, ECDHKeyExchanger.genSecretKey(ECDHKeyExchanger.getPrivateKey(partB), ECDHKeyExchanger.getPublicKey(partA)));
        byte[] decrypted = ECDHKeyExchanger.decrypt(encrypted, ECDHKeyExchanger.genSecretKey(ECDHKeyExchanger.getPrivateKey(partA), ECDHKeyExchanger.getPublicKey(partB)));
        System.out.println(new String(decrypted));

        // 甲方加密乙方解密
        encrypted = ECDHKeyExchanger.encrypt(data, ECDHKeyExchanger.genSecretKey(ECDHKeyExchanger.getPrivateKey(partA), ECDHKeyExchanger.getPublicKey(partB)));
        decrypted = ECDHKeyExchanger.decrypt(encrypted, ECDHKeyExchanger.genSecretKey(ECDHKeyExchanger.getPrivateKey(partB), ECDHKeyExchanger.getPublicKey(partA)));
        System.out.println(new String(decrypted));
    }
}

package code.ponfee.commons.jce.security;

import java.util.Map;

import javax.crypto.interfaces.DHKey;

import code.ponfee.commons.util.MavenProjects;

public class DHKeyExchangerTest {

    public static void main(String[] args) {
        Map<String, DHKey> partA = DHKeyExchanger.initPartAKey(1024);
        Map<String, DHKey> partB = DHKeyExchanger.initPartBKey(DHKeyExchanger.getPublicKey(partA));
        byte[] data = MavenProjects.getMainJavaFileAsByteArray(DHKeyExchanger.class);

        // 乙方加密甲方解密
        byte[] encrypted = DHKeyExchanger.encrypt(data, DHKeyExchanger.genSecretKey(DHKeyExchanger.getPrivateKey(partB), DHKeyExchanger.getPublicKey(partA)));
        byte[] decrypted = DHKeyExchanger.decrypt(encrypted, DHKeyExchanger.genSecretKey(DHKeyExchanger.getPrivateKey(partA), DHKeyExchanger.getPublicKey(partB)));
        System.out.println(new String(decrypted));

        // 甲方加密乙方解密
        encrypted = DHKeyExchanger.encrypt(data, DHKeyExchanger.genSecretKey(DHKeyExchanger.getPrivateKey(partA), DHKeyExchanger.getPublicKey(partB)));
        decrypted = DHKeyExchanger.decrypt(encrypted, DHKeyExchanger.genSecretKey(DHKeyExchanger.getPrivateKey(partB), DHKeyExchanger.getPublicKey(partA)));
        System.out.println(new String(decrypted));
    }
}

package cn.ponfee.commons.jce.security;

import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.interfaces.DHPublicKey;

import org.apache.commons.lang3.tuple.Pair;

import cn.ponfee.commons.jce.Providers;
import cn.ponfee.commons.util.MavenProjects;

public class DHKeyExchangerTest {

    public static void main(String[] args) {
        Providers.set(Providers.BC);
        Pair<DHPublicKey, DHPrivateKey> partA = DHKeyExchanger.initPartAKey(1024);
        Pair<DHPublicKey, DHPrivateKey> partB = DHKeyExchanger.initPartBKey(partA.getLeft());
        byte[] data = MavenProjects.getMainJavaFileAsBytes(DHKeyExchanger.class);

        // 乙方加密甲方解密
        byte[] encrypted = DHKeyExchanger.encrypt(data, DHKeyExchanger.genSecretKey(partB.getRight(), partA.getLeft()));
        byte[] decrypted = DHKeyExchanger.decrypt(encrypted, DHKeyExchanger.genSecretKey(partA.getRight(), partB.getLeft()));
        System.out.println(new String(decrypted));

        // 甲方加密乙方解密
        encrypted = DHKeyExchanger.encrypt(data, DHKeyExchanger.genSecretKey(partA.getRight(), partB.getLeft()));
        decrypted = DHKeyExchanger.decrypt(encrypted, DHKeyExchanger.genSecretKey(partB.getRight(), partA.getLeft()));
        System.out.println(new String(decrypted));
    }
}

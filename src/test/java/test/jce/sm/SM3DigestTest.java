package test.jce.sm;

import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.codec.binary.Hex;
import org.junit.Assert;

import cn.ponfee.commons.jce.sm.SM3Digest;
import cn.ponfee.commons.util.SecureRandoms;

public class SM3DigestTest {

    public static void main(String[] args) {
        String actual = Hex.encodeHexString(SM3Digest.getInstance().doFinal("0123456789".getBytes()));
        if (!"09093b72553f5d9d622d6c62f5ffd916ee959679b1bd4d169c3e12aa8328e743".equals(actual)) {
            System.err.println("sm3 digest error!");
        } else {
            System.out.println("SUCCESS!");
        }

        byte[] data = "0123456789".getBytes();

        byte[] hash = SM3Digest.getInstance().doFinal(data);
        System.out.println(Hex.encodeHexString(hash));

        SM3Digest sm3 = SM3Digest.getInstance();

        hash = sm3.doFinal(data);
        System.out.println(Hex.encodeHexString(hash));

        hash = sm3.doFinal(data);
        System.out.println(Hex.encodeHexString(hash));

        hash = sm3.doFinal(data);
        System.out.println(Hex.encodeHexString(hash));

        SM3Digest sm3_1 = SM3Digest.getInstance();
        org.bouncycastle.crypto.digests.SM3Digest sm3_2 = new org.bouncycastle.crypto.digests.SM3Digest();
        for (int i = 0; i < 100; i++) {
            byte[] data1 = SecureRandoms.nextBytes(ThreadLocalRandom.current().nextInt(65537) + 1);
            byte[] data2 = SecureRandoms.nextBytes(ThreadLocalRandom.current().nextInt(65537) + 1);
            sm3_1.update(data1);
            sm3_2.update(data1, 0, data1.length);
            sm3_2.update(data2, 0, data2.length);
            byte[] dig = new byte[sm3_2.getDigestSize()];
            sm3_2.doFinal(dig, 0);
            Assert.assertArrayEquals(sm3_1.doFinal(data2), dig);
        }
    }
}

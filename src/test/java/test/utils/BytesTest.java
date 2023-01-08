package test.utils;

import cn.ponfee.commons.util.Bytes;
import cn.ponfee.commons.util.ObjectUtils;
import cn.ponfee.commons.util.SecureRandoms;
import com.google.common.base.Stopwatch;
import org.apache.commons.codec.binary.Hex;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;

public class BytesTest {

    @Test
    public void test1() {
        int n = 999999;
        byte[] data = SecureRandoms.nextBytes(ThreadLocalRandom.current().nextInt(9999));
        Stopwatch watch = Stopwatch.createStarted();
        for (int i = 0; i < n; i++) {
            Base64.getEncoder().encodeToString(data);
        }
        System.out.println("Base64.getEncoder().encodeToString " + watch.stop());

        watch.reset().start();
        for (int i = 0; i < n; i++) {
            test.utils.Base64.encodeBytes(data);
        }
        System.out.println("test.utils.Base64.encodeBytes " + watch.stop());

        watch.reset().start();
        for (int i = 0; i < n; i++) {
            Bytes.hexEncode(data);
        }
        System.out.println("hexEncode " + watch.stop());

        watch.reset().start();
        for (int i = 0; i < n; i++) {
            Hex.encodeHexString(data);
        }
        System.out.println("Hex.encodeHexString " + watch.stop());
    }

    @Test
    public void test2() {
        for (int i = 0; i < 10000; i++) {
            byte b = (byte) SecureRandoms.nextInt();
            if (!Integer.toBinaryString((b & 0xFF) + 0x100).equals(Integer.toBinaryString(b & 0xFF | 0x100))) {
                System.err.println(i+"fail");
            }
        }
    }
    
    @Test
    public void test3() {
        for (int i = 0; i < 100000; i++) {
            byte[] data = SecureRandoms.nextBytes(ThreadLocalRandom.current().nextInt(999));
            String hex = Hex.encodeHexString(data);
            Assert.assertArrayEquals(data, Bytes.hexDecode(hex));
        }
    }

    @Test
    public void test4() {
        byte[] uuid = ObjectUtils.uuid();
        long a = System.nanoTime();
        long b = Thread.currentThread().getId();
        long c = System.identityHashCode(SecureRandoms.class);

        ByteBuffer buffer = ByteBuffer.allocate(40);
        buffer.put(uuid);
        buffer.putLong(a);
        buffer.putLong(b);
        buffer.putLong(c);
        buffer.flip();
        Assert.assertEquals(Bytes.hexEncode(buffer.array()), Bytes.hexEncode(uuid) + Bytes.hexEncode(Bytes.toBytes(a)) + Bytes.hexEncode(Bytes.toBytes(b)) + Bytes.hexEncode(Bytes.toBytes(c)));
    }

}

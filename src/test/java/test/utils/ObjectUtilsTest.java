package test.utils;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import cn.ponfee.commons.util.Bytes;

public class ObjectUtilsTest {

    static int n = 100000000;
    UUID uuid = UUID.randomUUID();
    long most  = uuid.getMostSignificantBits(), 
        least = uuid.getLeastSignificantBits();
    
    @Test
    public void test1() {
        char[] chars = new char[32];
        Bytes.encodeHex(chars, 0, (byte) (most >>> 56));
        Bytes.encodeHex(chars, 2, (byte) (most >>> 48));
        Bytes.encodeHex(chars, 4, (byte) (most >>> 40));
        Bytes.encodeHex(chars, 6, (byte) (most >>> 32));
        Bytes.encodeHex(chars, 8, (byte) (most >>> 24));
        Bytes.encodeHex(chars, 10, (byte) (most >>> 16));
        Bytes.encodeHex(chars, 12, (byte) (most >>> 8));
        Bytes.encodeHex(chars, 14, (byte) (most));

        Bytes.encodeHex(chars, 16, (byte) (least >>> 56));
        Bytes.encodeHex(chars, 18, (byte) (least >>> 48));
        Bytes.encodeHex(chars, 20, (byte) (least >>> 40));
        Bytes.encodeHex(chars, 22, (byte) (least >>> 32));
        Bytes.encodeHex(chars, 24, (byte) (least >>> 24));
        Bytes.encodeHex(chars, 26, (byte) (least >>> 16));
        Bytes.encodeHex(chars, 28, (byte) (least >>> 8));
        Bytes.encodeHex(chars, 30, (byte) (least));
        String s1 = new String(chars);

        System.out.println(s1);
        byte[] bytes = new byte[] {
            (byte) (most >>> 56), (byte) (most >>> 48),
            (byte) (most >>> 40), (byte) (most >>> 32),
            (byte) (most >>> 24), (byte) (most >>> 16),
            (byte) (most >>> 8), (byte) (most),

            (byte) (least >>> 56), (byte) (least >>> 48),
            (byte) (least >>> 40), (byte) (least >>> 32),
            (byte) (least >>> 24), (byte) (least >>> 16),
            (byte) (least >>> 8), (byte) (least)
        };
        String s2 = Bytes.encodeHex(bytes);
        System.out.println(s2);

        Assert.assertEquals(s1, s2);
    }

    @Test
    public  void test2() {
        for (int i= 0; i < n; i++) {
            char[] chars = new char[32];
            Bytes.encodeHex(chars,  0, (byte) (most  >>> 56));
            Bytes.encodeHex(chars,  2, (byte) (most  >>> 48));
            Bytes.encodeHex(chars,  4, (byte) (most  >>> 40));
            Bytes.encodeHex(chars,  6, (byte) (most  >>> 32));
            Bytes.encodeHex(chars,  8, (byte) (most  >>> 24));
            Bytes.encodeHex(chars, 10, (byte) (most  >>> 16));
            Bytes.encodeHex(chars, 12, (byte) (most  >>>  8));
            Bytes.encodeHex(chars, 14, (byte) (most        ));

            Bytes.encodeHex(chars, 16, (byte) (least >>> 56));
            Bytes.encodeHex(chars, 18, (byte) (least >>> 48));
            Bytes.encodeHex(chars, 20, (byte) (least >>> 40));
            Bytes.encodeHex(chars, 22, (byte) (least >>> 32));
            Bytes.encodeHex(chars, 24, (byte) (least >>> 24));
            Bytes.encodeHex(chars, 26, (byte) (least >>> 16));
            Bytes.encodeHex(chars, 28, (byte) (least >>>  8));
            Bytes.encodeHex(chars, 30, (byte) (least       ));
            new String(chars);
        }
    }
    
    @Test
    public  void test3() {

        for (int i= 0; i < n; i++) {
            byte[] bytes = new byte[] {
                (byte) (most  >>> 56), (byte) (most  >>> 48),
                (byte) (most  >>> 40), (byte) (most  >>> 32),
                (byte) (most  >>> 24), (byte) (most  >>> 16),
                (byte) (most  >>>  8), (byte) (most        ),

                (byte) (least >>> 56), (byte) (least >>> 48),
                (byte) (least >>> 40), (byte) (least >>> 32),
                (byte) (least >>> 24), (byte) (least >>> 16),
                (byte) (least >>>  8), (byte) (least       )
            };
            Bytes.encodeHex(bytes);
        }
    }
}

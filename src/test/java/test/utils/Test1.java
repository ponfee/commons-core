package test.utils;

import code.ponfee.commons.util.SecureRandoms;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;
import org.xbill.DNS.utils.base16;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class Test1 {
    private int age;

    private class Nested {

    }

    public boolean compare(Test1 t) {
        return this.age > t.age;
    }

    public static void main(String[] args) {
        Test1 t1 = new Test1();
        Test1 t2 = new Test1();
        t1.compare(t2);

        Nested n = t1.new Nested();

        System.out.println(new ArrayList<>().equals(null));
        System.out.println(3&0x01);
        System.out.println(2&0x01);
        System.out.println(1&0x01);
        System.out.println(0&0x01);
        System.out.println(new Double((double)Long.MAX_VALUE).longValue() == Long.MAX_VALUE);
        System.out.println(new Double((double)Long.MIN_VALUE).longValue() == Long.MIN_VALUE);
        byte[] b = SecureRandoms.nextBytes(11);
        System.out.println(new Base32().encodeToString(b));
        System.out.println(base16.toString(b));
        System.out.println(Hex.encodeHexString(b));
        System.out.println(Integer.toBinaryString(tableSizeFor(1 << 30)));
        for (int i = 0; i < 1; i++) {
            System.out.println(Integer.toBinaryString(tableSizeFor(ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE))));
        }
    }

    static final int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= 1 << 30) ? 1 << 30 : n + 1;
    }
}

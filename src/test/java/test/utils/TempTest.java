package test.utils;

import cn.ponfee.commons.jce.digest.DigestUtils;
import cn.ponfee.commons.reflect.ClassUtils;
import cn.ponfee.commons.reflect.Fields;
import cn.ponfee.commons.reflect.GenericUtils;
import cn.ponfee.commons.resource.ResourceScanner;
import cn.ponfee.commons.util.Bytes;
import cn.ponfee.commons.util.ObjectUtils;
import cn.ponfee.commons.util.SecureRandoms;
import com.google.common.base.Stopwatch;
import org.apache.commons.codec.binary.Hex;
import org.joda.time.DateTime;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class TempTest {

    @org.junit.Test
    public void test0() throws IOException {
        //Files.touch("d:/xampp");
        //Files.touch("d:/123.xlsx");
        //Files.touch("d:/1233.xlsx");

        com.google.common.io.Files.touch(new File("d:/12345.xlsx"));
        com.google.common.io.Files.touch(new File("d:/xampp"));
    }

    @org.junit.Test
    public void test2() {
        System.out.println(DigestUtils.sha256Hex(DigestUtils.sha256("cn.ponfee.commons.jce.hash.HashUtils".getBytes())));
    }

    @org.junit.Test
    public void test3() {
        String str = "123";
        String str2 = "123";
        Fields.put(str, "value", "abc".toCharArray());
        System.out.println(str2);
        char[] chars = (char[]) Fields.get(str, "value");
        chars[0] = '1';
        System.out.println(str2);
    }

    @org.junit.Test
    public void test4() {
        for (Class<?> clazz : new ResourceScanner("/cn/ponfee/commons/base/**/*.class").scan4class()) {
            for (Method method : clazz.getMethods()) {
                try {
                    Class<?> c = GenericUtils.getActualArgTypeArgument(method, 0);
                    if (c != Object.class) {
                        System.out.println(clazz.getSimpleName() + "-->" + method.getName() + "-->" + c);
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    @org.junit.Test
    public void test41() {
        //new ResourceScanner("/**/rmid_fr.properties").scan4text().forEach((k, v) -> System.out.println(k + " -> " + v));
        //new ResourceScanner("log4j2.xml.template").scan4text().forEach((k, v) -> System.out.println(k + " -> " + v));
        //new ResourceScanner("/cn/ponfee/commons/jce/*.class").scan4bytes().forEach((k, v) -> System.out.println(k + " -> " + v));
        //new ResourceScanner("/cn/ponfee/commons/jce/**/*.class").scan4bytes().forEach((k, v) -> System.out.println(k + " -> " + v));
        //new ResourceScanner("/*.template").scan4text().forEach((k, v) -> System.out.println(k + " -> " + v));
        //new ResourceScanner("/log4j2.xml.template").scan4text().forEach((k, v) -> System.out.println(k + " -> " + v));
        //new ResourceScanner("log4j2.xml.template").scan4text().forEach((k, v) -> System.out.println(k + " -> " + v));
        //new ResourceScanner("/**/*.xml").scan4text().forEach((k, v) -> System.out.println(k + " -> " + v));
        //new ResourceScanner("/cn/ponfee/commons/base/**/*.class").scan4class().forEach(System.out::println);
        //new ResourceScanner("/cn/ponfee/commons/**/*.class").scan4class(null, new Class[] {Service.class}).forEach(System.out::println);
        //new ResourceScanner("/cn/ponfee/commons/**/*.class").scan4class(null, new Class[] {Component.class}).forEach(System.out::println);
        //new ResourceScanner("/cn/ponfee/commons/**/*.class").scan4class(new Class[]{Tuple.class}, null).forEach(System.out::println);

        //new ResourceScanner("/*.template").scan4text().forEach((k, v) -> System.out.println(k + " -> " + v));
        //new ResourceScanner("/log4j2.xml.template").scan4text().forEach((k, v) -> System.out.println(k + " -> " + v));
        //new ResourceScanner("log4j2.xml.template").scan4text().forEach((k, v) -> System.out.println(k + " -> " + v));
        //new ResourceScanner("/**/tika*.xml").scan4text().forEach((k, v) -> System.out.println(k + " -> " + v));
        //new ResourceScanner("/cn/ponfee/commons/jce/*.class").scan4bytes().forEach((k, v) -> System.out.println(k + " -> " + v));
        //new ResourceScanner("/cn/ponfee/commons/jce/**/*.class").scan4bytes().forEach((k, v) -> System.out.println(k + " -> " + v));

        //new ResourceScanner("/cn/ponfee/commons/base/**/*.class").scan4class().forEach(System.out::println);
        //new ResourceScanner("/cn/ponfee/commons/**/*.class").scan4class(null, new Class[] {Service.class}).forEach(System.out::println);
        //new ResourceScanner("/cn/ponfee/commons/**/*.class").scan4class(null, new Class[] {Component.class}).forEach(System.out::println);
        //new ResourceScanner("/cn/ponfee/commons/**/*.class").scan4class(new Class[]{Tuple.class}, null).forEach(System.out::println);

        //new ResourceScanner("*").scan4bytes().entrySet().stream().filter(e -> e.getKey().endsWith(".xml")).forEach(e -> System.out.println(e.getKey() + " -> " + e.getValue()));
        //new ResourceScanner("/*.xml").scan4bytes().entrySet().stream().filter(e -> e.getKey().endsWith(".xml")).forEach(e -> System.out.println(e.getKey() + " -> " + e.getValue()));
        //new ResourceScanner("**/*.xml").scan4bytes().entrySet().stream().filter(e -> e.getKey().endsWith(".xml")).forEach(e -> System.out.println(e.getKey() + " -> " + e.getValue()));
        new ResourceScanner("/**/*.xml").scan4bytes().entrySet().stream().filter(e -> e.getKey().endsWith(".xml")).forEach(e -> System.out.println(e.getKey() + " -> " + e.getValue()));
    }

    @org.junit.Test
    public void test5() {
        for (Method method : ClassUtils.class.getMethods()) {
            System.out.println(ClassUtils.getMethodSignature(method) + " --> "+method.toGenericString());
        }
    }

    @org.junit.Test
    public void test6() {
        byte[] bytes = SecureRandoms.nextBytes(32);
        BigInteger big = new BigInteger(1, bytes);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            big.toString(16);
        }
        System.out.println(System.currentTimeMillis()-start);
    }

    @org.junit.Test
    public void test7() {
        byte[] bytes = SecureRandoms.nextBytes(32);
        BigInteger big = new BigInteger(1, bytes);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            Hex.encodeHexString(big.toByteArray());
        }
        System.out.println(System.currentTimeMillis()-start);
    }

    @org.junit.Test
    public void test8() {
        System.out.println(new DateTime().millisOfDay().withMinimumValue());
        System.out.println(new DateTime().withTimeAtStartOfDay());

        Calendar calendar = Calendar.getInstance();
        DateTime dateTime = new DateTime(2000, 1, 1, 0, 0, 0, 0);
        System.out.println(dateTime.plusDays(45).plusMonths(1).dayOfWeek()
          .withMaximumValue().toString("E MM/dd/yyyy HH:mm:ss.SSS"));
        calendar.setTime(dateTime.toDate());
        System.out.println(calendar.getTime());
    }

    @org.junit.Test
    public void test9() {
        short svalue;
        for (int i = 0; i < 10000; i++) {
            svalue = (short) SecureRandoms.nextInt();
            Assert.assertArrayEquals(Bytes.toBytes(svalue), fromShort(svalue));
        }
        byte[] bytes;
        for (int i = 0; i < 10000; i++) {
            bytes = SecureRandoms.nextBytes(2);
            Assert.assertEquals(Bytes.toShort(bytes), toShort(bytes, 0));
        }

        int ivalue;
        for (int i = 0; i < 10000; i++) {
            ivalue = SecureRandoms.nextInt();
            Assert.assertArrayEquals(Bytes.toBytes(ivalue), fromInt(ivalue));
        }
        for (int i = 0; i < 10000; i++) {
            bytes = SecureRandoms.nextBytes(4);
            Assert.assertEquals(Bytes.toInt(bytes), toInt(bytes, 0));
        }

        long lvalue;
        for (int i = 0; i < 10000; i++) {
            lvalue = SecureRandoms.nextLong();
            Assert.assertArrayEquals(Bytes.toBytes(lvalue), fromLong(lvalue));
        }
        for (int i = 0; i < 10000; i++) {
            bytes = SecureRandoms.nextBytes(8);
            Assert.assertEquals(Bytes.toLong(bytes), toLong(bytes, 0));
        }
    }

    @org.junit.Test
    public void test10() {
        long value = SecureRandoms.nextLong();
        byte[] bytes = SecureRandoms.nextBytes(8);
        long round = 999999999L;
        Stopwatch watch = Stopwatch.createStarted();
        for (long i = 0; i < round; i++) {
            Bytes.toBytes(value);
        }
        System.out.println("Bytes.fromLong: " + watch.stop());

        watch.reset().start();
        for (long i = 0; i < round; i++) {
            fromLong(value);
        }
        System.out.println("fromLong: " + watch.stop());

        watch.reset().start();
        for (long i = 0; i < round; i++) {
            Bytes.toLong(bytes);
        }
        System.out.println("Bytes.toLong: " + watch.stop());

        watch.reset().start();
        for (long i = 0; i < round; i++) {
            toLong(bytes, 0);
        }
        System.out.println("toLong: " + watch.stop());
    }

    @org.junit.Test
    public void test100() {
        System.out.println(ObjectUtils.uuid32());
        UUID uuid = UUID.randomUUID();
        long most = uuid.getMostSignificantBits();
        long least = uuid.getLeastSignificantBits();
        long round = 99999999L;
        String s;
        Stopwatch watch = Stopwatch.createStarted();
        for (long i = 0; i < round; i++) {
            s = Long.toHexString(most) + Long.toHexString(least);
        }
        System.out.println("Long.toHexString: " + watch.stop());

        watch.reset().start();
        for (long i = 0; i < round; i++) {
            s = Bytes.hexEncode(Bytes.toBytes(most))+Bytes.hexEncode(Bytes.toBytes(least));
        }
        System.out.println("Bytes.hexEncode: " + watch.stop());

        watch.reset().start();
        for (long i = 0; i < round; i++) {
            s = Bytes.hexEncode(new byte[] {
               (byte) (most  >>> 56), (byte) (most  >>> 48),
               (byte) (most  >>> 40), (byte) (most  >>> 32),
               (byte) (most  >>> 24), (byte) (most  >>> 16),
               (byte) (most  >>>  8), (byte) (most        ),

               (byte) (least >>> 56), (byte) (least >>> 48),
               (byte) (least >>> 40), (byte) (least >>> 32),
               (byte) (least >>> 24), (byte) (least >>> 16),
               (byte) (least >>>  8), (byte) (least       )
            });
        }
        System.out.println("uuid32: " + watch.stop());
    }

    @org.junit.Test
    public void test11() {
        int value = tableSizeFor(ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE));
        System.out.println(Integer.toBinaryString(value));
    }

    @org.junit.Test
    public void test12() {
        System.out.println(Bytes.toBinary(Bytes.toBytes(MAXIMUM_CAPACITY)));
        System.out.println(Bytes.toBinary(Bytes.toBytes(Integer.MAX_VALUE)));
    }

    static final int MAXIMUM_CAPACITY = 1 << 30;
    static final int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }


    public static byte[] fromShort(short value) {
        return ByteBuffer.allocate(Short.BYTES).putShort(value).array();
    }

    public static short toShort(byte[] bytes, int fromIdx) {
        ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES);
        buffer.put(bytes, fromIdx, Short.BYTES).flip();
        return buffer.getShort();
    }

    public static byte[] fromInt(int value) {
        return ByteBuffer.allocate(Integer.BYTES).putInt(value).array();
    }

    public static int toInt(byte[] bytes, int fromIdx) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.put(bytes, fromIdx, Integer.BYTES).flip();
        return buffer.getInt();
    }

    public static byte[] fromLong(long number) {
        return ByteBuffer.allocate(Long.BYTES).putLong(number).array();
    }

    public static long toLong(byte[] bytes, int fromIdx) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes, fromIdx, Long.BYTES).flip();
        return buffer.getLong();
    }

}

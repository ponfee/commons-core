package test.utils;

import java.security.SecureRandom;

import org.junit.Test;

import code.ponfee.commons.util.SecureRandoms;

public class TestCost {

    @Test
    public void test1() {
        byte b1 = 127, b2 = 127;
        long start = System.currentTimeMillis();
        for (long i = 0; i < 99999999999L; i++) {
            if (b1 != b2) {
                System.out.println("fail!");
                break;
            }
        }
        System.out.println("test1 cost: " + (System.currentTimeMillis() - start) / 1000);
    }
    
    @Test
    public void test2() {
        byte b1 = 127, b2 = 127;
        long start = System.currentTimeMillis();
        for (long i = 0; i < 99999999999L; i++) {
            if ((b1 ^ b2) != 0) {
                System.out.println("fail!");
                break;
            }
        }
        System.out.println("test2 cost: " + (System.currentTimeMillis() - start) / 1000);
    }
    
    @Test
    public void test3() {
        long start = System.currentTimeMillis();
        for (long i = 0; i < 99999999999L; i++) {
        }
        System.out.println("test2 cost: " + (System.currentTimeMillis() - start) / 1000);
    }
    
    @Test
    public void test4() {
        long start = System.currentTimeMillis();
        for (long i = 0; i != 99999999999L; i++) {
        }
        System.out.println("test2 cost: " + (System.currentTimeMillis() - start) / 1000);
    }
    
    @Test
    public void test6() {
        byte[] b = SecureRandoms.nextBytes(1024);
        long start = System.currentTimeMillis();
        for (long i = 0; i < 999999L; i++) {
            org.apache.commons.codec.binary.Base64.encodeBase64String(b);
            //java.util.Base64.getEncoder().encodeToString(b);
        }
        System.out.println("test6 cost: " + (System.currentTimeMillis() - start) / 1000);
    }

    @Test
    public void test5() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        long start = System.currentTimeMillis();
        for (long i = 0; i != 99999999L; i++) {
            //org.apache.commons.codec.binary.Hex.encodeHex(bytes);
            org.bouncycastle.util.encoders.Hex.toHexString(bytes);
        }
        System.out.println("test2 cost: " + (System.currentTimeMillis() - start) / 1000);
    }

}

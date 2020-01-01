package code.ponfee.commons.jce;

import java.security.GeneralSecurityException;

import org.apache.commons.codec.binary.Hex;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Stopwatch;

import code.ponfee.commons.jce.passwd.BCrypt;
import code.ponfee.commons.jce.passwd.Crypt;
import code.ponfee.commons.jce.passwd.PBKDF2;
import code.ponfee.commons.jce.passwd.SCrypt;
import code.ponfee.commons.util.SecureRandoms;

public class PasswdTest {

    
    @Test
    public void testCrypt() {
        String passwd = "passwd";
        String hashed = Crypt.create(HmacAlgorithms.HmacSHA3_256, passwd, 32, Providers.BC);
        boolean flag = true;
        System.out.println(hashed);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            if (!Crypt.check(passwd, hashed)) {
                flag = false;
                break;
            }
        }
        System.out.println("cost: " + (System.currentTimeMillis() - start));
        if (flag) {
            System.out.println("success!");
        } else {
            System.err.println("fail!");
        }
    }
    
    /**
     * Tests the basic functionality of the PasswordHash class
     * @param args ignored
     * @throws GeneralSecurityException
     */
    @Test
    public void testPBKDF2_1() {
        // Print out 10 hashes
        for (int i = 0; i < 10; i++) {
            System.out.println(PBKDF2.create(HmacAlgorithms.HmacSHA3_256, "p\r\nassw0Rd!".toCharArray(), 16, 65535, 32));
        }
        System.out.println("============================================\n");

        // Test password validation
        HmacAlgorithms alg = HmacAlgorithms.HmacSHA3_256;
        System.out.println("Running tests...");
        String passwd = "password";
        String hashed = PBKDF2.create(alg, passwd);
        System.out.println(hashed);
        boolean failure = false;
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) { // 20 seconds
            if (!PBKDF2.check(passwd, hashed)) {
                failure = true;
                break;
            }
        }
        System.out.println("cost: " + (System.currentTimeMillis() - start) / 1000);
        if (failure) {
            System.err.println("TESTS FAILED!");
        } else {
            System.out.println("TESTS PASSED!");
        }
    }
    
    @Test
    public void testScrypt() {
        byte[] pwd = "123456".getBytes();
        byte[] salt = "0123456789123456".getBytes();
        System.out.println("\n=====================PBKDF2=============================");

        System.out.println("\n=====================scrypt cost=============================");
        Stopwatch watch = Stopwatch.createStarted();
        SCrypt.scrypt(HmacAlgorithms.HmacSHA256, "123".getBytes(), "123".getBytes(), 16384, 8, 8, 64); // 推荐参数
        System.out.println("16384, 8, 8, 64 cost: " + watch.stop());

        watch.reset().start();
        SCrypt.scrypt(HmacAlgorithms.HmacSHA256, "123".getBytes(), "123".getBytes(), 2, 2, 2, 32); // 推荐参数
        System.out.println("2, 2, 2, 32 cost: " + watch.stop());

        System.out.println("\n=====================scrypt verify=============================");
        String actual = Hex.encodeHexString(SCrypt.scrypt(HmacAlgorithms.HmacSHA256, pwd, salt, 8, 255, 255, 32));
        if (!"e488217f72b6c850f82911e78427a78d8a64aa7d313cdc9ee6989915d7548df4".equals(actual)) {
            System.err.println("scrypt fail!");
        } else {
            System.out.println("scrypt success!");
        }

        System.out.println("\n=====================Scrypt=============================");
        String password = "passwd";
        String hashed = SCrypt.create(password, 1, 2, 2);
        System.out.println(hashed);
        System.out.println("Test begin...");
        boolean flag = true;
        watch.reset().start();
        for (int i = 0; i < 100000; i++) { // 20 seconds
            if (!SCrypt.check(password, hashed)) {
                flag = false;
                break;
            }
        }
        if (flag) {
            System.out.println("Test success!");
        } else {
            System.err.println("Test fail!");
        }
        System.out.println("cost: " + watch.stop());
    }
    
    @Test
    public void testBcrypt() {
        byte[] pwd = "123456".getBytes();
        byte[] salt = "0123456789123456".getBytes();
        String actual = Hex.encodeHexString(BCrypt.crypt(pwd, salt, 5));
        if (!"ddc41d0b514ecedb8ae12c42e8c2f4419e71e15c519ecd4b".equals(actual)) {
            System.err.println("crypt fail!");
        } else {
            System.out.println("crypt success!");
        }
        System.out.println();

        String password = "passwd";
        System.out.println(BCrypt.create(password, 11));
        System.out.println();

        System.out.println("Test begin...");
        boolean flag = true;
        String hashed = BCrypt.create(password, 5);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) { // 45 seconds
            if (!BCrypt.check(password, hashed)) {
                flag = false;
                break;
            }
        }
        System.out.println("cost: " + (System.currentTimeMillis() - start) / 1000);
        if (flag) {
            System.out.println("Test success!");
        } else {
            System.err.println("Test fail!");
        }
    }

    @Test
    public void testScrypt2() {
        byte[] pwd = SecureRandoms.nextBytes(20);
        byte[] salt = SecureRandoms.nextBytes(16);
        byte[] except = SCrypt.scrypt(HmacAlgorithms.HmacSHA256, pwd, salt, 8, 255, 255, 32);
        byte[] actual = org.bouncycastle.crypto.generators.SCrypt.generate(pwd, salt, 8, 255, 255, 32);
        Assert.assertArrayEquals(except, actual);
    }

    @Test
    public void testBcrypt2() {
        byte[] pwd = SecureRandoms.nextBytes(20);
        byte[] salt = SecureRandoms.nextBytes(16);
        byte[] except = BCrypt.crypt(pwd, salt, 5);
        byte[] actual = org.bouncycastle.crypto.generators.BCrypt.generate(pwd, salt, 5);
        Assert.assertArrayEquals(except, actual);
    }

    @Test
    public void testPBKDF2_2() {
        String pwd = "SecureRandoms.nextBytes(20)";
        int iterationCount = 20;
        int dkLen = 32;
        byte[] salt = SecureRandoms.nextBytes(16);
        byte[] except = PBKDF2.pbkdf2(HmacAlgorithms.HmacSHA3_256, pwd.toCharArray(), salt, iterationCount, dkLen);
        byte[] actual = SCrypt.pbkdf2(HmacAlgorithms.HmacSHA3_256, pwd.getBytes(), salt, iterationCount, dkLen);
        Assert.assertArrayEquals(except, actual);
    }

}

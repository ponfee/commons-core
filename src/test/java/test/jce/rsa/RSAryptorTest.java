package test.jce.rsa;

import java.io.ByteArrayInputStream;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.google.common.base.Stopwatch;

import code.ponfee.commons.jce.implementation.Key;
import code.ponfee.commons.jce.implementation.rsa.AbstractRSACryptor;
import code.ponfee.commons.jce.implementation.rsa.RSAHashCryptor;
import code.ponfee.commons.jce.implementation.rsa.RSAKey;
import code.ponfee.commons.jce.implementation.rsa.RSANoPaddingCryptor;
import code.ponfee.commons.jce.implementation.rsa.RSAPKCS1PaddingCryptor;
import code.ponfee.commons.jce.security.RSACryptor;
import code.ponfee.commons.jce.security.RSAPrivateKeys;
import code.ponfee.commons.jce.security.RSAPublicKeys;
import code.ponfee.commons.util.IdcardResolver;
import code.ponfee.commons.util.MavenProjects;
import code.ponfee.commons.util.SecureRandoms;

public class RSAryptorTest {

    private static byte[] origin = MavenProjects.getMainJavaFileAsByteArray(IdcardResolver.class);
    private static boolean isPrint = false;

    private static void print(String s) {
        if (isPrint) {
            System.out.println(s);
        }
    }

    @Test
    public void testRSANoPadding() {
        System.out.println("\n\ntestRSANoPadding======================================");
        RSAKey dk = new RSAKey(1024);
        Key ek = dk.getPublic();
        RSANoPaddingCryptor cs = new RSANoPaddingCryptor();

        Stopwatch watch = Stopwatch.createStarted();
        byte[] encrypted = cs.encrypt(origin, ek);
        byte[] decrypted = cs.decrypt(encrypted, dk);
        if (!Arrays.equals(origin, decrypted)) {
            System.err.println("FAIL!");
        } else {
            print("\n\n=====RSA1 Decrypted text is: \n" + new String(decrypted));
        }
        System.out.println(watch.stop());

        watch.reset().start();
        /*RSAPublicKey pub = RSAPublicKeys.toRSAPublicKey(dk.n, dk.e);
        encrypted = RSACryptor.encrypt(origin, pub);*/
        RSAPrivateKey pri = RSAPrivateKeys.toRSAPrivateKey(dk.n, dk.d);
        decrypted = RSACryptor.decryptNoPadding(encrypted, pri);
        if (!Arrays.equals(origin, decrypted)) {
            System.err.println("FAIL!");
        } else {
            print("\n\n=====RSA2 Decrypted text is: \n" + new String(decrypted));
        }
        System.out.println(watch.stop());
    }

    @Test
    public void testRSAPKCS1Padding() {
        System.out.println("\n\ntestRSAPKCS1Padding======================================");
        RSAKey dk = new RSAKey(1024);
        Key ek = dk.getPublic();
        AbstractRSACryptor cs = new RSAPKCS1PaddingCryptor();
        RSAPublicKey pub = RSAPublicKeys.toRSAPublicKey(dk.n, dk.e);
        RSAPrivateKey pri = RSAPrivateKeys.toRSAPrivateKey(dk.n, dk.d);

        // ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝公钥加密，私钥解密
        Stopwatch watch = Stopwatch.createStarted();
        byte[] encrypted = cs.encrypt(origin, ek);
        byte[] decrypted = cs.decrypt(encrypted, dk);
        if (!Arrays.equals(origin, decrypted)) {
            System.err.println("FAIL!");
        } else {
            print("\n\n=====RSA1 Decrypted text is: \n" + new String(decrypted));
        }
        System.out.println(watch.stop());

        watch.reset().start();
        decrypted = RSACryptor.decrypt(encrypted, pri);
        if (!Arrays.equals(origin, decrypted)) {
            System.err.println("FAIL!");
        } else {
            print("\n\n=====RSA2 Decrypted text is: \n" + new String(decrypted));
        }
        System.out.println(watch.stop());

        // ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝私钥加密，公钥解密
        watch = Stopwatch.createStarted();
        encrypted = cs.encrypt(origin, dk);
        decrypted = cs.decrypt(encrypted, ek);
        if (!Arrays.equals(origin, decrypted)) {
            System.err.println("FAIL!");
        } else {
            print("\n\n=====RSA1 Decrypted text is: \n" + new String(decrypted));
        }
        System.out.println(watch.stop());

        watch.reset().start();
        decrypted = RSACryptor.decrypt(encrypted, pub);
        if (!Arrays.equals(origin, decrypted)) {
            System.err.println("FAIL!");
        } else {
            print("\n\n=====RSA2 Decrypted text is: \n" + new String(decrypted));
        }
        System.out.println(watch.stop());

        // =======================================加密－解密
        encrypted = RSACryptor.encrypt(origin, pub);
        decrypted = cs.decrypt(encrypted, dk);
        if (!Arrays.equals(origin, decrypted)) {
            System.err.println("FAIL!");
        } else {
            print("\n\n=====RSA1 Decrypted text is: \n" + new String(decrypted));
        }

        // =======================================加密－解密
        encrypted = RSACryptor.encrypt(origin, pri);
        decrypted = cs.decrypt(encrypted, ek);
        if (!Arrays.equals(origin, decrypted)) {
            System.err.println("FAIL!");
        } else {
            print("\n\n=====RSA1 Decrypted text is: \n" + new String(decrypted));
        }
    }

    @Test
    public void testRSAHash() {
        System.out.println("\n\ntestRSAHash======================================");
        for (int i = 0; i < 10; i++) {
            byte[] data = SecureRandoms.nextBytes(ThreadLocalRandom.current().nextInt(99999) + 1);

            RSAKey dk = new RSAKey(1024);
            Key ek = dk.getPublic();
            RSAHashCryptor cs = new RSAHashCryptor();

            byte[] encrypted = cs.encrypt(data, ek);
            byte[] decrypted = cs.decrypt(encrypted, dk);
            if (!Arrays.equals(data, decrypted)) {
                System.err.println("FAIL!");
            } else {
                print("\n\n=====RSAHashCryptor Decrypted text is: \n" + new String(decrypted));
            }
        }
    }

    @Test
    public void testRSARandom() {
        System.out.println("\n\ntestRSARandom======================================");
        RSAKey dk = new RSAKey(1024);
        Key ek = dk.getPublic();
        RSANoPaddingCryptor cs = new RSANoPaddingCryptor();
        RSAPKCS1PaddingCryptor cs2 = new RSAPKCS1PaddingCryptor();
        RSAPublicKey pub = RSAPublicKeys.toRSAPublicKey(dk.n, dk.e);
        RSAPrivateKey pri = RSAPrivateKeys.toRSAPrivateKey(dk.n, dk.d);
        for (int i = 0; i < 1000; i++) {

            /*int length = ThreadLocalRandom.current().nextInt(65537) + 1;
            int offset = ThreadLocalRandom.current().nextInt(origin.length - length);
            System.out.println(length + " -> " + offset);
            byte[] data = Arrays.copyOfRange(origin, offset, offset + length);*/

            //byte[] data = new byte[] { 0, 1, 1, 0, 0 }; // occur wrong

            byte[] data = SecureRandoms.nextBytes(ThreadLocalRandom.current().nextInt(255) + 1);

            // 1
            byte[] encrypted1 = cs.encrypt(data, ek);
            byte[] decrypted1 = cs.decrypt(encrypted1, dk);                   // XXX DATA!=decrypted1    1% 

            // 2
            byte[] encrypted2 = encrypted1;
            byte[] decrypted2 = RSACryptor.decryptNoPadding(encrypted2, pri); // XXX DATA!=decrypted1    1%

            // 3
            byte[] encrypted3 = RSACryptor.encryptNoPadding(data, pub);
            byte[] decrypted3 = RSACryptor.decryptNoPadding(encrypted3, pri); // XXX DATA!=decrypted1    5%

            // 4
            byte[] encrypted4 = RSACryptor.encrypt(data, pub);
            byte[] decrypted4 = RSACryptor.decrypt(encrypted4, pri);

            // 5
            byte[] encrypted5 = cs2.encrypt(data, ek);
            byte[] decrypted5 = cs2.decrypt(encrypted5, dk);
            // -------------------------------------------------------
            if (!Arrays.equals(data, decrypted1)) {
                System.err.println("[" + StringUtils.leftPad(i + "", 4, "0") + "]decrypt1 FAIL!: " + Hex.encodeHexString(data) + " -> "
                    + Hex.encodeHexString(decrypted1));
            }

            if (!Arrays.equals(data, decrypted2)) {
                System.err.println("[" + StringUtils.leftPad(i + "", 4, "0") + "]decrypt2 FAIL!: " + Hex.encodeHexString(data) + " -> "
                    + Hex.encodeHexString(decrypted2));
            }

            if (!Arrays.equals(data, decrypted3)) {
                System.err.println("[" + StringUtils.leftPad(i + "", 4, "0") + "]decrypt3 FAIL!: " + Hex.encodeHexString(data) + " -> "
                    + Hex.encodeHexString(decrypted3));
            }

            if (!Arrays.equals(data, decrypted4)) {
                System.err.println("[" + StringUtils.leftPad(i + "", 4, "0") + "]decrypt4 FAIL!: " + Hex.encodeHexString(data) + " -> "
                    + Hex.encodeHexString(decrypted4));
            }

            if (!Arrays.equals(data, decrypted5)) {
                System.err.println("[" + StringUtils.leftPad(i + "", 4, "0") + "]decrypt5 FAIL!: " + Hex.encodeHexString(data) + " -> "
                    + Hex.encodeHexString(decrypted5));
            }
        }
    }

    @Test
    public void testRSAHashInverseKey() {
        System.out.println("\n\ntestRSAHashInverseKey======================================");
        RSAKey dk = new RSAKey(1024);
        Key ek = dk.getPublic();
        RSAHashCryptor cs = new RSAHashCryptor();
        for (int i = 0; i < 100; i++) {
            byte[] data = SecureRandoms.nextBytes(ThreadLocalRandom.current().nextInt(255) + 1);
            byte[] encrypted1 = cs.encrypt(data, dk); // 私钥加密
            byte[] decrypted1 = cs.decrypt(encrypted1, ek); // 公钥解密
            if (!Arrays.equals(data, decrypted1)) {
                System.err.println("[" + StringUtils.leftPad(i + "", 4, "0") + "]decrypt1 FAIL!: " + Hex.encodeHexString(data) + " -> "
                    + Hex.encodeHexString(decrypted1));
            }
        }
    }

    @Test
    public void testRSANoPaddingInverseKey() {
        System.out.println("\n\ntestRSANoPaddingInverseKey======================================");
        RSAKey dk = new RSAKey(1024);
        Key ek = dk.getPublic();
        RSANoPaddingCryptor cs = new RSANoPaddingCryptor();
        for (int i = 0; i < 1000; i++) {
            byte[] data = SecureRandoms.nextBytes(ThreadLocalRandom.current().nextInt(255) + 1);
            byte[] encrypted1 = cs.encrypt(data, dk); // 私钥加密
            byte[] decrypted1 = cs.decrypt(encrypted1, ek); // 公钥解密
            if (!Arrays.equals(data, decrypted1)) {
                System.err.println("[" + StringUtils.leftPad(i + "", 4, "0") + "]decrypt1 FAIL!: " + Hex.encodeHexString(data) + " -> "
                    + Hex.encodeHexString(decrypted1));
            }
        }
    }
    
    @Test
    public void testRSAPKCS1InverseKey() {
        System.out.println("\n\ntestRSAPKCS1InverseKey======================================");
        RSAKey dk = new RSAKey(1024);
        Key ek = dk.getPublic();
        RSAPKCS1PaddingCryptor cs = new RSAPKCS1PaddingCryptor();
        for (int i = 0; i < 100; i++) {
            byte[] data = SecureRandoms.nextBytes(ThreadLocalRandom.current().nextInt(255) + 1);
            byte[] encrypted1 = cs.encrypt(data, dk); // 私钥加密
            byte[] decrypted1 = cs.decrypt(encrypted1, ek); // 公钥解密
            if (!Arrays.equals(data, decrypted1)) {
                System.err.println("[" + StringUtils.leftPad(i + "", 4, "0") + "]decrypt1 FAIL!: " + Hex.encodeHexString(data) + " -> "
                    + Hex.encodeHexString(decrypted1));
            }
        }
    }
    @Test
    public void testRSANoPaddingStream() {
        System.out.println("\n\ntestRSANoPaddingStream======================================");
        RSAKey dk = new RSAKey(2048);
        Key ek = dk.getPublic();
        RSANoPaddingCryptor cs = new RSANoPaddingCryptor();

        ByteArrayInputStream input = new ByteArrayInputStream(origin);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Stopwatch watch = Stopwatch.createStarted();
        cs.encrypt(input, ek, output);
        byte[] encrypted = output.toByteArray();

        input = new ByteArrayInputStream(encrypted);
        output = new ByteArrayOutputStream();
        cs.decrypt(input, dk, output);
        byte[] decrypted = output.toByteArray();
        if (!Arrays.equals(origin, decrypted)) {
            System.err.println("FAIL!");
        } else {
            print("\n\n=====RSAStream Decrypted text is: \n" + new String(decrypted));
        }
        System.out.println(watch.stop());

        watch.reset().start();
        input = new ByteArrayInputStream(encrypted);
        output = new ByteArrayOutputStream();
        RSAPrivateKey pri = RSAPrivateKeys.toRSAPrivateKey(dk.n, dk.d);
        RSACryptor.decryptNoPadding(input, pri, output);
        decrypted = output.toByteArray();
        if (!Arrays.equals(origin, decrypted)) {
            System.err.println("FAIL!");
        } else {
            print("\n\n=====RSAStream Decrypted text is: \n" + new String(decrypted));
        }
        System.out.println(watch.stop());
    }

    @Test
    public void testRSAPKCS1PaddingStream() {
        System.out.println("\n\ntestRSAPKCS1PaddingStream======================================");
        RSAKey dk = new RSAKey(1024);
        Key ek = dk.getPublic();
        AbstractRSACryptor cs = new RSAPKCS1PaddingCryptor();

        ByteArrayInputStream input = new ByteArrayInputStream(origin);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Stopwatch watch = Stopwatch.createStarted();
        cs.encrypt(input, ek, output);
        byte[] encrypted = output.toByteArray();

        input = new ByteArrayInputStream(encrypted);
        output = new ByteArrayOutputStream();
        cs.decrypt(input, dk, output);
        byte[] decrypted = output.toByteArray();
        if (!Arrays.equals(origin, decrypted)) {
            System.err.println("FAIL!");
        } else {
            print("\n\n=====RSAStream Decrypted text is: \n" + new String(decrypted));
        }
        System.out.println(watch.stop());

        watch.reset().start();
        input = new ByteArrayInputStream(encrypted);
        output = new ByteArrayOutputStream();
        RSAPrivateKey pri = RSAPrivateKeys.toRSAPrivateKey(dk.n, dk.d);
        RSACryptor.decrypt(input, pri, output);
        decrypted = output.toByteArray();
        if (!Arrays.equals(origin, decrypted)) {
            System.err.println("FAIL!");
        } else {
            print("\n\n=====RSAStream Decrypted text is: \n" + new String(decrypted));
        }
        System.out.println(watch.stop());
    }

    @Test
    public void testRSAHashStream() {
        System.out.println("\n\ntestRSAHashStream======================================");
        RSAKey dk = new RSAKey(2048);
        Key ek = dk.getPublic();
        RSAHashCryptor cs = new RSAHashCryptor();

        ByteArrayInputStream input = new ByteArrayInputStream(origin);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Stopwatch watch = Stopwatch.createStarted();
        cs.encrypt(input, ek, output);
        byte[] encrypted = output.toByteArray();
        print("encrypted len: " + encrypted.length + ",  origin len: " + origin.length);

        input = new ByteArrayInputStream(encrypted);
        output = new ByteArrayOutputStream();
        cs.decrypt(input, dk, output);
        byte[] decrypted = output.toByteArray();
        if (!Arrays.equals(origin, decrypted)) {
            System.err.println("FAIL!");
        } else {
            print("\n\n=====RSAStream Decrypted text is: \n" + new String(decrypted));
        }
        System.out.println(watch.stop());
    }

}

package cn.ponfee.commons.jce;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.codec.binary.Hex;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Stopwatch;

import cn.ponfee.commons.jce.digest.DigestUtils;
import cn.ponfee.commons.jce.digest.HmacUtils;
import cn.ponfee.commons.jce.implementation.digest.RipeMD160Digest;
import cn.ponfee.commons.jce.implementation.digest.SHA1Digest;
import cn.ponfee.commons.jce.implementation.rsa.RSAKey;
import cn.ponfee.commons.jce.implementation.symmetric.RC4;
import cn.ponfee.commons.jce.symmetric.Algorithm;
import cn.ponfee.commons.jce.symmetric.SymmetricCryptor;
import cn.ponfee.commons.jce.symmetric.SymmetricCryptorBuilder;
import cn.ponfee.commons.util.MavenProjects;
import cn.ponfee.commons.util.SecureRandoms;

public class DesgitTest {

    @Test
    public void test() {
        byte[] data = "1234567890".getBytes();
        RipeMD160Digest md = RipeMD160Digest.getInstance();
        String actual = Hex.encodeHexString(md.doFinal(data));
        if(!"9d752daa3fb4df29837088e1e5a1acf74932e074".equals(actual)) {
            System.err.println("fail");
        } else {
            System.out.println("success");
        }
        System.out.println(Hex.encodeHexString(md.doFinal(data)));
        System.out.println(Hex.encodeHexString(md.doFinal(data)));
        md.update(data);
        System.out.println(Hex.encodeHexString(md.doFinal()));
    }
    
    @Test
    public void test2() {
        System.out.println(Hex.encodeHexString(SHA1Digest.getInstance().doFinal()));
        System.out.println(DigestUtils.sha1Hex(new byte[] {}));

        byte[] data = MavenProjects.getMainJavaFileAsByteArray(SHA1Digest.class);

        SHA1Digest sha1 = SHA1Digest.getInstance();
        System.out.println(Hex.encodeHexString(sha1.doFinal(data)));
        System.out.println(DigestUtils.sha1Hex(data));

        for (int i = 0; i < 1000; i++) {
            byte[] data1 = SecureRandoms.nextBytes(ThreadLocalRandom.current().nextInt(65537) + 1);
            byte[] data2 = SecureRandoms.nextBytes(ThreadLocalRandom.current().nextInt(65537) + 1);
            byte[] data3 = SecureRandoms.nextBytes(ThreadLocalRandom.current().nextInt(65537) + 1);
            sha1.reset();
            sha1.update(data1);
            sha1.update(data2);
            sha1.update(data3);
            byte[] expect = DigestUtils.digest(DigestAlgorithms.SHA1, data1, data2, data3);
            if (!Arrays.equals(expect, sha1.doFinal())) {
                System.err.println("FAIL" + " --> " + data.length);
            }
        }
    }
    public static final int RSA_F4 = 65537;
    
    @Test
    public void test3() {
        Stopwatch watch = Stopwatch.createStarted();
        RSAKey.generateKey(4096, RSA_F4);
        System.out.println("generateKey1: " + watch.stop());

        watch.reset().start();
        RSAKey.generateKey(4096, RSA_F4);
        System.out.println("generateKey2: " + watch.stop());
    }
    
    @Test
    public void test4() {
        byte[] key = "0123456789123456".getBytes();
        byte[] data = MavenProjects.getMainJavaFileAsByteArray(RC4.class);
        RC4 rc4 = new RC4(key);
        byte[] encrypted1 = rc4.encrypt(data);
        byte[] encrypted2 = rc4.encrypt(data);

        Assert.assertArrayEquals(encrypted1, encrypted2);

        if (   !Arrays.equals(rc4.decrypt(encrypted1), data)
            && !Arrays.equals(rc4.decrypt(encrypted1), data)) {
            System.err.println("rc4 crypt fail!");
        } else {
            //System.out.println(new String(rc4.crypt(encrypted)));
        }

        SymmetricCryptor rc = SymmetricCryptorBuilder.newBuilder(Algorithm.RC4, key).build();
        if (   !Arrays.equals(rc.decrypt(encrypted1), data)
            && !Arrays.equals(rc.decrypt(encrypted1), data)) {
            System.err.println("rc4 crypt fail!");
        } else {
            //System.out.println(new String(rc4.crypt(encrypted)));
        }

        encrypted1 = rc.encrypt(data);
        encrypted2 = rc.encrypt(data);
        if (   !Arrays.equals(rc4.decrypt(encrypted1), data)
            && !Arrays.equals(rc4.decrypt(encrypted2), data)) {
            System.err.println("rc4 crypt fail!");
        } else {
            //System.out.println(new String(rc4.decrypt(encrypted)));
        }
    }
    
    @Test
    public void test5() throws Exception {
        System.out.println(DigestUtils.sha224Hex("1".getBytes()));
        System.out.println(DigestUtils.ripeMD160Hex("1234567890".getBytes()));
        //System.out.println(ObjectUtils.toString(shortText("http://www.manong5.com/102542001/")));
        long start = System.currentTimeMillis();
        System.out.println(DigestUtils.sha1Hex(new FileInputStream("E:\\tools\\develop\\linux\\CentOS-6.6-x86_64-bin-DVD1.iso")));
        System.out.println((System.currentTimeMillis() - start) / 1000);
    }
    
    @Test
    public void test6() throws FileNotFoundException {
        byte[] key = SecureRandoms.nextBytes(16);
        System.out.println(HmacUtils.sha1Hex(key, new FileInputStream(MavenProjects.getMainJavaFile(HmacUtils.class))));
        System.out.println(HmacUtils.sha1Hex(key, new FileInputStream(MavenProjects.getMainJavaFile(HmacUtils.class))));
        System.out.println(HmacUtils.ripeMD128Hex(key, "abc".getBytes()));
        System.out.println(HmacUtils.ripeMD160Hex(key, "abc".getBytes()));
        System.out.println(HmacUtils.ripeMD256Hex(key, "abc".getBytes()));
        System.out.println(HmacUtils.ripeMD320Hex(key, "abc".getBytes()));
    }
}

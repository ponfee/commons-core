package test.jce.sha1;

import java.io.File;

import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

import cn.ponfee.commons.io.Files;
import cn.ponfee.commons.jce.DigestAlgorithms;
import cn.ponfee.commons.jce.HmacAlgorithms;
import cn.ponfee.commons.jce.Providers;
import cn.ponfee.commons.jce.digest.DigestUtils;
import cn.ponfee.commons.jce.digest.HmacUtils;
import cn.ponfee.commons.jce.implementation.digest.SHA1Digest;
import cn.ponfee.commons.jce.sm.SM3Digest;
import cn.ponfee.commons.util.MavenProjects;

public class SHA1BrokenTest {

    public @Test void test1() {
        byte[] pdf1 = Files.toByteArray(new File(MavenProjects.getTestJavaPath("test.jce.sha1", "shattered-1.pdf")));
        byte[] pdf2 = Files.toByteArray(new File(MavenProjects.getTestJavaPath("test.jce.sha1", "shattered-2.pdf")));
        System.out.println(Hex.encodeHexString(SHA1Digest.getInstance().doFinal(pdf1)));
        System.out.println(Hex.encodeHexString(SHA1Digest.getInstance().doFinal(pdf2)));
        System.out.println(Hex.encodeHexString(DigestUtils.sha1(pdf1)));
        System.out.println(Hex.encodeHexString(DigestUtils.sha1(pdf2)));

        System.out.println(Hex.encodeHexString(DigestUtils.sha256(pdf1)));
        System.out.println(Hex.encodeHexString(DigestUtils.sha256(pdf2)));
    }

    public @Test void test2() {
        byte[] pdf2 = Files.toByteArray(new File(MavenProjects.getTestJavaPath("test.jce.sha1", "shattered-2.pdf")));

        System.out.println(Hex.encodeHexString(DigestUtils.digest(DigestAlgorithms.SHA3_256, Providers.BC, pdf2)));
        System.out.println(Hex.encodeHexString(DigestUtils.digest(DigestAlgorithms.KECCAK256, Providers.BC, pdf2)));
        System.out.println();

        System.out.println(Hex.encodeHexString(DigestUtils.digest(DigestAlgorithms.SHA3_512, Providers.BC, pdf2)));
        System.out.println(Hex.encodeHexString(DigestUtils.digest(DigestAlgorithms.KECCAK512, Providers.BC, pdf2)));
        System.out.println(Hex.encodeHexString(DigestUtils.digest(DigestAlgorithms.SM3, Providers.BC, pdf2)));
        System.out.println(Hex.encodeHexString(SM3Digest.getInstance().doFinal(pdf2)));
        System.out.println();

        System.out.println(Hex.encodeHexString(HmacUtils.crypt("1234".getBytes(), pdf2, HmacAlgorithms.HmacSHA3_256)));
        System.out.println(Hex.encodeHexString(HmacUtils.crypt("1234".getBytes(), pdf2, HmacAlgorithms.HmacKECCAK256)));
        System.out.println();

        System.out.println(Hex.encodeHexString(HmacUtils.crypt("1234".getBytes(), pdf2, HmacAlgorithms.HmacSHA3_512)));
        System.out.println(Hex.encodeHexString(HmacUtils.crypt("1234".getBytes(), pdf2, HmacAlgorithms.HmacKECCAK512)));
        System.out.println();
    }

}

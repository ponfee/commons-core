package code.ponfee.commons.boolm;

import java.security.DigestException;
import java.security.MessageDigest;

import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

import code.ponfee.commons.jce.DigestAlgorithms;
import code.ponfee.commons.jce.digest.DigestUtils;

public class RedisBloomFilterTest {

    private static final DigestAlgorithms DIGEST = DigestAlgorithms.MD5;

    @Test
    public void test1() throws DigestException {
        MessageDigest md = DigestUtils.getMessageDigest(DIGEST, null);
        md.update("123".getBytes());
        md.update("abc".getBytes());
        System.out.println(Hex.encodeHexString(md.digest()));

        md = DigestUtils.getMessageDigest(DIGEST, null);
        md.update("123".getBytes());
        System.out.println(Hex.encodeHexString(md.digest("abc".getBytes())));

        md = DigestUtils.getMessageDigest(DIGEST, null);
        md.update("123".getBytes());
        md.update("abc".getBytes());
        byte[] output = new byte[DIGEST.byteSize()];
        md.digest(output, 0, output.length);
        System.out.println(Hex.encodeHexString(output));
    }

}

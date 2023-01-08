package test.utils;

import java.math.BigInteger;
import java.util.Arrays;

import org.junit.Test;

import cn.ponfee.commons.util.Base58;
import junit.framework.TestCase;

/**
 * https://blog.csdn.net/qq_41185868/article/details/80806532
 * 
 * @author Ponfee
 */
public class Base58Test extends TestCase {
    @Test
    public void testEncode() {
        byte[] testbytes = "Hello World".getBytes();
        assertEquals("JxF12TrwUP45BMd", Base58.encode(testbytes));

        BigInteger bi = BigInteger.valueOf(3471844090L);
        assertEquals("16Ho7Hs", Base58.encode(bi.toByteArray()));

        byte[] zeroBytes1 = new byte[1];
        assertEquals("1", Base58.encode(zeroBytes1));

        byte[] zeroBytes7 = new byte[7];
        assertEquals("1111111", Base58.encode(zeroBytes7));

        // test empty encode
        assertEquals("", Base58.encode(new byte[0]));
    }

    @Test
    public void testDecode() {
        byte[] testbytes = "Hello World".getBytes();
        byte[] actualbytes = Base58.decode("JxF12TrwUP45BMd");
        assertTrue(new String(actualbytes), Arrays.equals(testbytes, actualbytes));

        assertTrue("1", Arrays.equals(Base58.decode("1"), new byte[1]));
        assertTrue("1111", Arrays.equals(Base58.decode("1111"), new byte[4]));

        try {
            Base58.decode("This isn't valid base58");
            fail();
        } catch (Exception e) {
            // expected
        }

        Base58.decodeChecked("4stwEBjT6FYyVV");

        // Checksum should fail.
        try {
            Base58.decodeChecked("4stwEBjT6FYyVW");
            fail();
        } catch (Exception e) {
            // expected
        }

        // Input is too short.
        try {
            Base58.decodeChecked("4s");
            fail();
        } catch (Exception e) {
            // expected
        }

        // Test decode of empty String.
        assertEquals(0, Base58.decode("").length);

        // Now check we can correctly decode the case where the high bit of the first byte is not zero, so BigInteger
        // sign extends. Fix for a bug that stopped us parsing keys exported using sipas patch.
        Base58.decodeChecked("93VYUMzRG9DdbRP72uQXjaWibbQwygnvaCu9DumcqDjGybD864T");
        Base58.decodeChecked("1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa");
        Base58.decodeChecked("1XPTgDRhN8RFnzniWCddobD9iKZatrvH4");
        Base58.decodeChecked("14rE7Jqy4a6P27qWCCsngkUfBxtevZhPHB");
    }

    @Test
    public void testDecodeToBigInteger() {
        byte[] input = Base58.decode("129");
        assertEquals(new BigInteger(1, input), Base58.decodeToBigInteger("129"));
    }
}

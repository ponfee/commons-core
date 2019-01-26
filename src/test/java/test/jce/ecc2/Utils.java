package test.jce.ecc2;
import org.bouncycastle.crypto.digests.GeneralDigest;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class Utils {
    /**
     * Construct a fresh GeneralDigest from an existing one
     *
     * @param hashDigest A general hash digest to make a new instance of
     * @return A new GeneralDigest with the same class as the input
     * @throws SecurityException Thrown if a fresh hash digest cannot be constructed from the digest handed as an argument
     */
    public static GeneralDigest freshDigestFromDigest(GeneralDigest hashDigest) throws SecurityException {
        GeneralDigest freshHashDigest;
        try {
            freshHashDigest = hashDigest.getClass().getConstructor().newInstance();
        } catch (Throwable e) {
            throw new SecurityException(e);
        }
        return freshHashDigest;
    }

    /**
     * Convert a UTF8 encoded string as bytes
     *
     * @param string The byte to convert
     * @return An array of bytes that correspond to the UTF8 encoded string
     * @throws SecurityException Thrown if the string cannot be validly converted to a UTF-8 string
     */
    public static byte[] stringToUTF8Bytes(String string) throws SecurityException {
        try {
            return string.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * Convert a long to an array of bytes
     *
     * @param x Long to convert
     * @return An array of bytes corresponding to the converted long
     */
    public static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    /**
     * Convert an array of bytes to a long
     *
     * @param bytes Bytes of a long
     * @return The long represented by the bytes
     */
    public static long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip();
        return buffer.getLong();
    }

    public static int countLeadingZeroBytes(byte[] bytes) {
        int i = -1;
        //noinspection StatementWithEmptyBody
        while (++i < bytes.length && bytes[i] == 0) {
        }
        return i;
    }
}

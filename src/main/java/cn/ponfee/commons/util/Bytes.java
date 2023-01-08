/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.util;

import cn.ponfee.commons.math.Numbers;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Formatter;
import java.util.Objects;
import java.util.zip.CRC32;

/**
 * byte[]
 * <pre>
 * 转hex：new BigInteger(1, bytes).toString(16);
 * Padding4位：(4 - (length & 0x03)) & 0x03
 * 
 * 左移<<:      该数对应的二进制码整体左移，左边超出的部分舍弃，右边补0
 * 右移>>:      该数对应的二进制码整体右移，左边部分以原有标志位填充，右边超出的部分舍弃
 * 无符号右移>>>: 该数对应的二进制码整体右移，左边部分以0填充，右边超出的部分舍弃
 * </pre>
 *
 * @author Ponfee
 */
public final class Bytes {

    private static final char SPACE_CHAR = ' ';
    private static final char[] HEX_LOWER_CODES = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
    private static final char[] HEX_UPPER_CODES = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

    /**
     * Dump byte array, like as these 
     * {@link org.apache.commons.io.HexDump#dump(byte[], long, java.io.OutputStream, int)}, 
     * {@link sun.misc.HexDumpEncoder#encode(byte[], java.io.OutputStream);}
     * 
     * @param data   字节数组
     * @param chunk  每行块数
     * @param block  每块大小
     * @return Dump the byte array as hex string
     */
    public static String hexDump(byte[] data, int chunk, int block) {
        Formatter fmt = new Formatter(), text;

        String lineNumberFormat = "%0" + (Integer.toHexString((data.length + 15) / 16).length() + 1) + "x: ";
        for (int i, j = 0, wid = block * chunk; j * wid < data.length; j++) {
            fmt.format(lineNumberFormat, j * wid); // 输出行号：“00000: ”

            text = new Formatter(); // 右边文本
            for (i = 0; i < wid && (i + j * wid) < data.length; i++) {
                byte b = data[i + j * wid];
                fmt.format("%02X ", b); // 输出hex：“B1 ”
                if ((i + 1) % block == 0 || i + 1 == wid) {
                    fmt.format("%s", SPACE_CHAR); // block与block间加一个空格
                }
                if (b > 0x1F && b < 0x7F) {
                    text.format("%c", b);
                } else {
                    text.format("%c", '.'); // 非ascii码则输出“.”
                }
            }

            if (i < wid) { // 最后一行空格补全：i为该行的byte数
                fmt.format("%s", StringUtils.repeat(SPACE_CHAR, (wid - i) * 3)); // 补全byte位
                for (int k = i + 1; k <= wid; k += block) {
                    fmt.format("%s", SPACE_CHAR); // 补全block与block间的空格
                }
            }

            fmt.format("%s", SPACE_CHAR); // block与text间加一个空格
            fmt.format("%s", text); // 输出text：“..@.s.UwH...b{.U”
            fmt.format("%s", "\n"); // 输出换行
            text.close();
        }

        fmt.flush();
        try {
            return fmt.toString();
        } finally {
            fmt.close();
        }
    }

    public static String hexDump(byte[] data) {
        return hexDump(data, 2, 8);
    }

    /**
     * convert the byte array to binary string
     * byte:
     *    -1: 11111111
     *     0: 00000000
     *   127: 01111111
     *  -128: 10000000
     * 
     * @param array
     * @return
     */
    public static String toBinary(byte... array) {
        if (array == null || array.length == 0) {
            return null;
        }

        StringBuilder builder = new StringBuilder(array.length << 3);
        String binary;
        for (byte b : array) {
            // byte & 0xFF ：byte转int保留bit位
            // byte | 0x100：对于正数保留八位，保证未尾8位为原byte的bit位，即1xxxxxxxx
            //               正数会有前缀0，如果不加，转binary string时前面的0会被舍去
            // 也可以用 “byte + 0x100”或者“leftPad(binaryString, 8, '0')”
            binary = Integer.toBinaryString((b & 0xFF) | 0x100);
            builder.append(binary, 1, binary.length());
        }
        return builder.toString();
    }

    // -----------------------------------------------------------------hexEncode/hexDecode
    public static void hexEncode(char[] charArray, int i, byte b) {
        charArray[  i] = HEX_LOWER_CODES[(0xF0 & b) >>> 4];
        charArray[++i] = HEX_LOWER_CODES[ 0x0F & b       ];
    }

    public static String hexEncode(byte b, boolean lowercase) {
        char[] codes = lowercase ? HEX_LOWER_CODES : HEX_UPPER_CODES;
        return new String(new char[] {
            codes[(0xF0 & b) >>> 4], codes[0x0F & b]
        });
    }

    public static String hexEncode(byte[] bytes) {
        return hexEncode(bytes, true);
    }

    /**
     * encode the byte array the hex string 
     * @param bytes
     * @param lowercase
     * @return
     */
    public static String hexEncode(byte[] bytes, boolean lowercase) {
        //new BigInteger(1, bytes).toString(16);
        int len = bytes.length;
        char[] out = new char[len << 1];

        char[] codes = lowercase ? HEX_LOWER_CODES : HEX_UPPER_CODES;

        // one byte -> two char
        for (int i = 0, j = 0; i < len; i++) {
            out[j++] = codes[(0xF0 & bytes[i]) >>> 4];
            out[j++] = codes[ 0x0F & bytes[i]       ];
        }
        return new String(out);
    }

    /**
     * decode the hex string to byte array 
     * @param hex
     * @return
     */
    public static byte[] hexDecode(String hex) {
        char[] data = hex.toCharArray();
        int len = data.length;
        if ((len & 0x01) == 1) {
            throw new IllegalArgumentException("Invalid hex string.");
        }

        byte[] out = new byte[len >> 1];

        // two char -> one byte
        for (int i = 0, j = 0; j < len; i++, j += 2) {
            out[i] = (byte) (Character.digit(data[j], 16) << 4 
                           | Character.digit(data[j + 1], 16));
        }
        return out;
    }

    // -----------------------------------------------------------------String
    /**
     * Converts byte array to String
     * 
     * @param bytes the byte array
     * @return a string
     */
    public static String toString(byte[] bytes) {
        return new String(bytes);
    }

    /**
     * Converts byte array to String
     * 
     * @param bytes the byte array
     * @param charset the Charset
     * @return a string
     */
    public static String toString(byte[] bytes, Charset charset) {
        return new String(bytes, charset);
    }

    /**
     * Converts String to byte array
     * 
     * @param value the string value
     * @return a byte array
     */
    public static byte[] toBytes(String value) {
        return value.getBytes();
    }

    /**
     * Converts string to byte array
     * 
     * @param value the string value
     * @param charset the charset
     * @return a byte array
     */
    public static byte[] toBytes(String value, Charset charset) {
        return value.getBytes(charset);
    }

    // -----------------------------------------------------------------char array
    /**
     * Converts byte array to char array
     * 
     * @param bytes the byte array
     * @return a char array
     */
    public static char[] toCharArray(byte[] bytes) {
        return toCharArray(bytes, StandardCharsets.US_ASCII);
    }

    /**
     * Converts byte array to char array
     * 
     * @param bytes the byte array
     * @param charset the charset
     * @return a char array
     */
    public static char[] toCharArray(byte[] bytes, Charset charset) {
        //return new String(bytes, charset).toCharArray();
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        buffer.put(bytes);
        buffer.flip();
        return charset.decode(buffer).array();
    }

    /**
     * Converts char array to byte array
     * 
     * @param chars the char array
     * @return a byte array
     */
    public static byte[] toBytes(char[] chars) {
        return toBytes(chars, StandardCharsets.US_ASCII);
    }

    /**
     * Converts char array to byte array
     * 
     * @param chars the char array
     * @param charset the charset
     * @return a byte array
     */
    public static byte[] toBytes(char[] chars, Charset charset) {
        //return new String(chars).getBytes(charset);
        CharBuffer buffer = CharBuffer.allocate(chars.length);
        buffer.put(chars);
        buffer.flip();
        return charset.encode(buffer).array();
    }

    // -----------------------------------------------------------------short
    public static byte[] toBytes(short value) {
        return new byte[] {
            (byte) (value >>> 8), (byte) value
        };
        //return ByteBuffer.allocate(Short.BYTES).putShort(value).array();
    }

    public static short toShort(byte[] bytes) {
        return toShort(bytes, 0);
    }

    public static short toShort(byte[] bytes, int fromIdx) {
        return (short) (
              (bytes[  fromIdx]       ) << 8 
            | (bytes[++fromIdx] & 0xFF) 
        );
        //return ByteBuffer.wrap(bytes, fromIdx, Short.BYTES).getShort();
        //ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES);
        //buffer.put(bytes, fromIdx, Short.BYTES).flip();
        //return buffer.getShort();
    }

    // -----------------------------------------------------------------char
    public static byte[] toBytes(char value) {
        return new byte[] {
            (byte) (value >>> 8), (byte) value
        };
    }

    public static char toChar(byte[] bytes) {
        return toChar(bytes, 0);
    }

    public static char toChar(byte[] bytes, int fromIdx) {
        return (char) (
            (bytes[  fromIdx]       ) << 8
          | (bytes[++fromIdx] & 0xFF)
      );
    }

    // -----------------------------------------------------------------int
    public static byte[] toBytes(int value) {
        return new byte[] {
            (byte) (value >>> 24), (byte) (value >>> 16),
            (byte) (value >>>  8), (byte) (value       )
        };
    }

    public static int toInt(byte[] bytes) {
        return toInt(bytes, 0);
    }

    public static int toInt(byte[] bytes, int fromIdx) {
        return (bytes[  fromIdx]       ) << 24 // 高8位转int后左移24位，刚好剩下原来的8位，故不用&0xFF
             | (bytes[++fromIdx] & 0xFF) << 16 // 其它转int：若为负数，则是其补码表示，故要&0xFF
             | (bytes[++fromIdx] & 0xFF) <<  8
             | (bytes[++fromIdx] & 0xFF);
    }

    // -----------------------------------------------------------------long
    /**
     * convert long value to byte array
     * @param value the long number
     * @return byte array
     */
    public static byte[] toBytes(long value) {
        return new byte[] {
            (byte) (value >>> 56), (byte) (value >>> 48),
            (byte) (value >>> 40), (byte) (value >>> 32),
            (byte) (value >>> 24), (byte) (value >>> 16),
            (byte) (value >>>  8), (byte) (value       )
        };
    }

    /**
     * convert byte array to long number
     * @param bytes  the byte array
     * @param fromIdx the byte array offset
     * @return long number
     */
    public static long toLong(byte[] bytes, int fromIdx) {
        return ((long) bytes[  fromIdx]       ) << 56
             | ((long) bytes[++fromIdx] & 0xFF) << 48
             | ((long) bytes[++fromIdx] & 0xFF) << 40
             | ((long) bytes[++fromIdx] & 0xFF) << 32
             | ((long) bytes[++fromIdx] & 0xFF) << 24
             | ((long) bytes[++fromIdx] & 0xFF) << 16
             | ((long) bytes[++fromIdx] & 0xFF) <<  8
             | ((long) bytes[++fromIdx] & 0xFF);
    }

    /**
     * convert byte array to long number
     * @param bytes the byte array
     * @return
     */
    public static long toLong(byte[] bytes) {
        return toLong(bytes, 0);
    }

    /**
     * Long value to hex string
     *
     * @param value the long value
     * @return String of long value hex string
     */
    public static String toHex(long value) {
        return toHex(value, true);
    }

    /**
     * Long value to hex string
     *
     * @param value     the long value
     * @param lowercase {@code true} if lowercase hex string, else uppercase
     * @return String of long value hex string
     */
    public static String toHex(long value, boolean lowercase) {
        char[] a = lowercase ? HEX_LOWER_CODES : HEX_UPPER_CODES;
        int mask = 0x0F;
        return new String(new char[]{
            a[       (int) (value >>> 60)], a[mask & (int) (value >>> 56)],
            a[mask & (int) (value >>> 52)], a[mask & (int) (value >>> 48)],
            a[mask & (int) (value >>> 44)], a[mask & (int) (value >>> 40)],
            a[mask & (int) (value >>> 36)], a[mask & (int) (value >>> 32)],
            a[mask & (int) (value >>> 28)], a[mask & (int) (value >>> 24)],
            a[mask & (int) (value >>> 20)], a[mask & (int) (value >>> 16)],
            a[mask & (int) (value >>> 12)], a[mask & (int) (value >>>  8)],
            a[mask & (int) (value >>>  4)], a[mask & (int) (value       )]
        });
    }

    // -----------------------------------------------------------------float
    public static byte[] toBytes(float value) {
        return toBytes(Float.floatToIntBits(value));
    }

    public static float toFloat(byte[] bytes) {
        return toFloat(bytes, 0);
    }

    public static float toFloat(byte[] bytes, int fromIdx) {
        return Float.intBitsToFloat(toInt(bytes, fromIdx));
    }

    // -----------------------------------------------------------------double
    public static byte[] toBytes(double value) {
        return toBytes(Double.doubleToLongBits(value));
    }

    public static double toDouble(byte[] bytes) {
        return toDouble(bytes, 0);
    }

    public static double toDouble(byte[] bytes, int fromIdx) {
        return Double.longBitsToDouble(toLong(bytes, fromIdx));
    }

    // ---------------------------------------------------------BigDecimal
    /**
     * Convert a BigDecimal value to a byte array
     *
     * @param val
     * @return the byte array
     */
    public static byte[] toBytes(BigDecimal val) {
        byte[] valueBytes = val.unscaledValue().toByteArray();
        byte[] result = new byte[valueBytes.length + Integer.BYTES];
        int offset = putInt(val.scale(), result, 0);
        System.arraycopy(valueBytes, 0, result, offset, valueBytes.length);
        return result;
    }

    /**
     * Puts a int number to byte array
     * 
     * @param val    the int value
     * @param bytes  the byte array
     * @param offset the byte array start offset
     * @return a int of next offset
     */
    public static int putInt(int val, byte[] bytes, int offset) {
        bytes[  offset] = (byte) (val >>> 24);
        bytes[++offset] = (byte) (val >>> 16);
        bytes[++offset] = (byte) (val >>>  8);
        bytes[++offset] = (byte) (val       );
        return ++offset;
    }

    /**
     * Converts a byte array to a BigDecimal
     *
     * @param bytes
     * @return the char value
     */
    public static BigDecimal toBigDecimal(byte[] bytes) {
        return toBigDecimal(bytes, 0, bytes.length);
    }

    /**
     * Converts a byte array to a BigDecimal value
     *
     * @param bytes
     * @param offset
     * @param length
     * @return the char value
     */
    public static BigDecimal toBigDecimal(byte[] bytes, int offset, final int length) {
        if (bytes == null || length < Integer.BYTES + 1 ||
            (offset + length > bytes.length)) {
            return null;
        }

        int scale = toInt(bytes, offset);
        byte[] tcBytes = new byte[length - Integer.BYTES];
        System.arraycopy(bytes, offset + Integer.BYTES, tcBytes, 0, length - Integer.BYTES);
        return new BigDecimal(new BigInteger(tcBytes), scale);
    }

    // ---------------------------------------------------------BigInteger
    /**
     * Converts byte array to positive BigInteger
     * 
     * @param bytes the byte array
     * @return a positive BigInteger number
     */
    public static BigInteger toBigInteger(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return BigInteger.ZERO;
        }
        return new BigInteger(1, bytes);
    }

    // ----------------------------------------------------------others
    /**
     * merge byte arrays
     * @param first  first byte array of args
     * @param rest   others byte array
     * @return a new byte array of them
     */
    public static byte[] concat(byte[] first, byte[]... rest) {
        Objects.requireNonNull(first, "the first array arg cannot be null");
        if (rest == null || rest.length == 0) {
            return first;
        }

        int totalLength = first.length;
        for (byte[] array : rest) {
            if (array != null) {
                totalLength += array.length;
            }
        }

        byte[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (byte[] array : rest) {
            if (array != null) {
                System.arraycopy(array, 0, result, offset, array.length);
                offset += array.length;
            }
        }
        return result;

        /*
        ByteArrayOutputStream baos = new ByteArrayOutputStream(totalLength);
        baos.write(first, 0, first.length);
        for (byte[] array : rest) {
            if (array != null) {
                baos.write(array, 0, array.length);
            }
        }
        return baos.toByteArray();
        */
    }

    public static void tailCopy(byte[] src, int destLen) {
        tailCopy(src, new byte[destLen]);
    }

    public static void tailCopy(byte[] src, byte[] dest) {
        tailCopy(src, 0, src.length, dest, 0, dest.length);
    }

    /**
     * copy src to dest
     * 从尾部开始拷贝src到dest：
     *   若src数据不足则在dest前面补0
     *   若src数据有多则舍去src前面的数据
     *   
     * @param src
     * @param srcFrom
     * @param srcLen
     * @param dest
     * @param destFrom
     * @param destLen
     */
    public static void tailCopy(byte[] src, int srcFrom, int srcLen,
                                byte[] dest, int destFrom, int destLen) {
        tailCopy(src, srcFrom, srcLen, dest, destFrom, destLen, Numbers.BYTE_ZERO);
    }

    /**
     * copy src to dest
     * 从尾部开始拷贝src到dest：
     *   若src数据不足则在dest前面补heading
     *   若src数据有多则舍去src前面的数据
     *
     * @param src
     * @param srcFrom
     * @param srcLen
     * @param dest
     * @param destFrom
     * @param destLen
     * @param heading
     */
    public static void tailCopy(byte[] src, int srcFrom, int srcLen, 
                                byte[] dest, int destFrom, int destLen, 
                                byte heading) {
        int srcTo = Math.min(src.length, srcFrom + srcLen),
           destTo = Math.min(dest.length, destFrom + destLen);
        for (int i = destTo - 1, j = srcTo - 1; i >= destFrom; i--, j--) {
            dest[i] = (j < srcFrom) ? heading : src[j];
        }
    }

    /**
     * copy src to dest
     * 从首部开始拷贝src到dest：
     *   若src数据不足则在dest后面补tailing
     *   若src数据有多则舍去src后面的数据
     *
     * @param src
     * @param dest
     */
    public static void headCopy(byte[] src, byte[] dest) {
        headCopy(src, 0, src.length, dest, 0, dest.length, Numbers.BYTE_ZERO);
    }

    public static void headCopy(byte[] src, int srcFrom, int srcLen,
                                byte[] dest, int destFrom, int destLen,
                                byte tailing) {
        int srcTo = Math.min(src.length, srcFrom + srcLen),
            destTo = Math.min(dest.length, destFrom + destLen);
        for (int i = destFrom, j = srcFrom; i < destTo; i++, j++) {
            dest[i] = (j < srcTo) ? src[j] : tailing;
        }
    }

    public static long crc32(byte[] bytes) {
        CRC32 crc32 = new CRC32();
        crc32.update(bytes);
        return crc32.getValue();
    }

}

/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.jce.implementation.rsa;

import cn.ponfee.commons.io.Files;
import cn.ponfee.commons.jce.implementation.Cryptor;
import cn.ponfee.commons.jce.implementation.Key;
import cn.ponfee.commons.math.Numbers;
import cn.ponfee.commons.util.SecureRandoms;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Arrays;

/**
 * http://blog.51cto.com/xnuil/1698673
 *
 * RSA Cryptor, Without padding
 * RSA私钥解密证明：费马小定理（欧拉定理特例）
 *  等同证明：c^d ≡ m (mod n)
 *      因为：m^e ≡ c (mod n)
 *  于是，c可以写成：c = m^e - kn
 *  将c代入要我们要证明的那个解密规则：(m^e - kn)^d ≡ m (mod n)
 *  等同证明：m^(ed) ≡ m (mod n)
 *  由于：ed ≡ 1 (mod φ(n))
 *  所以：ed = hφ(n)+1
 *  得出：m^(hφ(n)+1) ≡ m (mod n)
 *
 * @author Ponfee
 */
public abstract class AbstractRSACryptor extends Cryptor {

    private final boolean isPadding;

    public AbstractRSACryptor(boolean isPadding) {
        this.isPadding = isPadding;
    }

    public int getOriginBlockSize(RSAKey rsaKey) {
        // 减一个byte为了防止溢出(byte array less than mod)
        // 此时BigInteger(1, byte[getOriginBlockSize(rsaKey)]) < rsaKey.n
        return rsaKey.n.bitLength() / 8 - 1;
    }

    public int getCipherBlockSize(RSAKey rsaKey) {
        return rsaKey.n.bitLength() / 8;
    }

    // ---------------------------------------------------------------do crypt methods
    @Override
    public byte[] encrypt(byte[] input, int length, Key ek) {
        RSAKey rsaKey = (RSAKey) ek;
        BigInteger exponent = this.getExponent(rsaKey);
        //return new BigInteger(1, input).modPow(exponent, rsaKey.n).toByteArray();

        int originBlockSize = this.getOriginBlockSize(rsaKey), // 加密前原文数据块的大小
            cipherBlockSize = this.getCipherBlockSize(rsaKey); // 加密后密文数据块大小

        ByteArrayOutputStream out = new ByteArrayOutputStream(input.length);
        byte[] origin, encrypted;

        try {
            for (int offset = 0, len = input.length, to; offset < len; offset += originBlockSize) {
                to = Math.min(len, offset + originBlockSize);
                if (isPadding) {
                    // 切割并填充原文数据块
                    origin = encodeBlock(input, offset, to, cipherBlockSize, rsaKey);
                } else {
                    // 切割原文数据块
                    origin = Arrays.copyOfRange(input, offset, to);
                }

                // 加密：encrypted = origin^e mod n
                encrypted = new BigInteger(1, origin).modPow(exponent, rsaKey.n).toByteArray();

                // 固定密文长度
                fixedByteArray(encrypted, cipherBlockSize, out);
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw new SecurityException(e); // cannot happen
        }
    }

    @Override
    public byte[] decrypt(byte[] input, Key dk) {
        RSAKey rsaKey = (RSAKey) dk;
        BigInteger exponent = this.getExponent(rsaKey);
        //return new BigInteger(1, input).modPow(exponent, rsaKey.n).toByteArray();

        int cipherBlockSize = this.getCipherBlockSize(rsaKey),
            originBlockSize = this.getOriginBlockSize(rsaKey);
        ByteArrayOutputStream output = new ByteArrayOutputStream(input.length);
        byte[] encrypted, origin;

        try {
            for (int offset = 0, len = input.length; offset < len; offset += cipherBlockSize) {
                // 切割密文数据块
                encrypted = Arrays.copyOfRange(input, offset, Math.min(len, offset + cipherBlockSize));

                // 解密：origin = encrypted^d mod n
                origin = new BigInteger(1, encrypted).modPow(exponent, rsaKey.n).toByteArray();

                if (isPadding) {
                    // 解码数据块
                    decodeBlock(origin, cipherBlockSize, output);
                } else {
                    if (offset + cipherBlockSize < len) { // 判断是否是最后一轮循环
                        // 固定明文长度
                        fixedByteArray(origin, originBlockSize, output);
                    } else {
                        // 去掉原文前缀0
                        trimByteArray(origin, output);
                    }
                }
            }
            return output.toByteArray();
        } catch (IOException e) {
            throw new SecurityException(e); // cannot happened
        }
    }

    public void encrypt(InputStream input, Key ek, OutputStream output) {
        RSAKey rsaKey = (RSAKey) ek;
        BigInteger exponent = this.getExponent(rsaKey);
        int cipherBlockSize = this.getCipherBlockSize(rsaKey);

        byte[] buffer = new byte[getOriginBlockSize(rsaKey)], origin, encrypted;

        try {
            for (int len; (len = input.read(buffer)) != Files.EOF;) {
                if (isPadding) {
                    // 切割并填充原文数据块
                    origin = encodeBlock(buffer, 0, len, cipherBlockSize, rsaKey);
                } else {
                    // 切割原文数据块
                    origin = Arrays.copyOfRange(buffer, 0, len);
                }

                // 加密：encrypted = origin^e mod n
                encrypted = new BigInteger(1, origin).modPow(exponent, rsaKey.n).toByteArray();

                // 固定密文长度
                fixedByteArray(encrypted, cipherBlockSize, output);
            }
            output.flush();
        } catch (IOException e) {
            throw new SecurityException(e);
        }
    }

    public void decrypt(InputStream input, Key dk, OutputStream output) {
        RSAKey rsaKey = (RSAKey) dk;
        BigInteger exponent = this.getExponent(rsaKey);

        int cipherBlockSize = this.getCipherBlockSize(rsaKey),
            originBlockSize = this.getOriginBlockSize(rsaKey);
        byte[] buffer = new byte[cipherBlockSize], encrypted, origin;

        try {
            int len, offset = 0, inputLen = input.available();
            for (; (len = input.read(buffer)) != Files.EOF; offset += cipherBlockSize) {
                // 切割密文数据块
                encrypted = Arrays.copyOfRange(buffer, 0, len);

                // 解密：origin = encrypted^d mod n
                origin = new BigInteger(1, encrypted).modPow(exponent, rsaKey.n).toByteArray();

                if (isPadding) {
                    // 解码数据块
                    decodeBlock(origin, cipherBlockSize, output);
                } else {
                    if (offset + cipherBlockSize < inputLen) {
                        // 固定明文长度
                        fixedByteArray(origin, originBlockSize, output);
                    } else {
                        // 去掉原文前缀0
                        trimByteArray(origin, output);
                    }
                }
            }
            output.flush();
        } catch (IOException e) {
            throw new SecurityException(e);
        }
    }

    public final BigInteger getExponent(RSAKey rsaKey) {
        return rsaKey.secret ? rsaKey.d : rsaKey.e;
    }

    /**
     * This method generates a new key for the crypto.
     * @return the new key generated
     */
    @Override
    public final Key generateKey() {
        return generateKey(2048);
    }

    public final Key generateKey(int keySize) {
        return new RSAKey(keySize);
    }

    @Override
    public final String toString() {
        return this.getClass().getSimpleName();
    }

    // ---------------------------------------------------------------private methods
    /**
     * When the BigInteger convert to byte array, if head more than two zero
     * then was automatic trim remain one zero, if head has not zreo then automatic
     * add a zreo. So we should manual control handle it, recover the origin byte
     * array of this BigInteger.
     *
     * @param data        the data
     * @param fixedSize   the result of byte array length
     * @param out         the output stream
     * @throws IOException if occur IOException
     * @see cn.ponfee.commons.util.Bytes#toBinary(byte...)
     * @see cn.ponfee.commons.util.Bytes#tailCopy(byte[], int, int, byte[], int, int)
     */
    private static void fixedByteArray(byte[] data, int fixedSize, OutputStream out)
        throws IOException {
        if (data.length < fixedSize) {
            // 当最前面有多个0时，此时会被舍去只留下一个0来充当符号位，所以要加前缀0来补全
            // 加前缀0补全到固定字节数：encryptedBlockSize
            for (int i = 0, heading = fixedSize - data.length; i < heading; i++) {
                out.write(Numbers.ZERO_BYTE);
            }
            out.write(data, 0, data.length);
        } else {
            // 当最前面的位为1时，BigInteger会通过加一个byte 0来充当符号位，此时需要手动舍去
            out.write(data, data.length - fixedSize, fixedSize);
        }
    }

    /**
     * 当最前面的位为1时，BigInteger会通过加一个byte 0来充当符号位，此时需要手动舍去
     * this method is unsafe, will be lose the prefix byte 0(one or more)
     *
     * @param data  the decrypted origin data
     * @param out   the output stream
     * @throws IOException if occur IOException
     * @see cn.ponfee.commons.util.Bytes#toBinary(byte...)
     */
    private static void trimByteArray(byte[] data, OutputStream out)
        throws IOException {
        int i = 0, len = data.length;
        for (; i < len; i++) {
            if (data[i] != Numbers.ZERO_BYTE) {
                break;
            }
        }
        if (i < len) {
            out.write(data, i, len - i);
        }
    }

    /**
     * 原文进行编码填充
     *
     * EB = 00 || BT || PS || 00 || D
     * BT：公钥为0x02；私钥为0x00或0x01
     * PS：BT为0则PS全部为0x00；BT为0x01则全部为0xFF；BT为0x02则为随机数，但不能为0
     *
     * 对于BT为00的，数据D就不能以00字节开头，因为这时候PS填充的也是00，
     * 会分不清哪些是填充数据哪些是明文数据<p>
     *
     * 如果你使用私钥加密，建议你BT使用01，保证了安全性
     * 对于BT为02和01的，PS至少要有8个字节长
     *
     * @param input  数据
     * @param from   开始位置
     * @param to     结束位置
     * @param cipherBlockSize 模字节长（modulo/8）
     * @param rsaKey 密钥
     * @return the encrypting block with pkcs1 padding
     */
    private static byte[] encodeBlock(byte[] input, int from, int to,
                                      int cipherBlockSize, RSAKey rsaKey) {
        int length = to - from;
        if (length > cipherBlockSize) {
            throw new IllegalArgumentException("input data too large");
        } else if (cipherBlockSize - length - 3 < 8) {
            throw new IllegalArgumentException("the padding too small");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream(cipherBlockSize);
        baos.write(Numbers.ZERO_BYTE); // 0x00

        if (rsaKey.secret) {
            // 私钥填充
            baos.write(0x01); // BT
            for (int i = 2, pLen = cipherBlockSize - length - 1; i < pLen; i++) {
                baos.write(0xFF);
            }
        } else {
            // 公钥填充，规定此处至少要8个字节
            baos.write(0x02); // BT
            byte b;
            for (int i = 2, pLen = cipherBlockSize - length - 1; i < pLen; i++) {
                do {
                    b = (byte) SecureRandoms.nextInt();
                } while (b == Numbers.ZERO_BYTE);
                baos.write(b);
            }
        }

        baos.write(Numbers.ZERO_BYTE); // 0x00

        baos.write(input, from, length); // D
        return baos.toByteArray();
    }

    /**
     * 解码原文填充（前缀0被舍去，只有127位）
     * @param input the input
     * @param cipherBlockSize the cipherBlockSize
     * @param out the out
     * @throws IOException if occur IOException
     * @see cn.ponfee.commons.util.Bytes#toBinary(byte...)
     */
    private static void decodeBlock(byte[] input, int cipherBlockSize, OutputStream out)
        throws IOException {
        // BigInteger to byte array will be removed the prefix 0 (one or more)
        // or added one byte 0
        // 0x00 [0x01 | 0x02], so signum is definite on [0x01 | 0x02] then
        // was removed the first 0x00
        int removedZeroLen;
        if (input[0] == Numbers.ZERO_BYTE) {
            removedZeroLen = 0;
        } else {
            removedZeroLen = 1;
        }

        // 输入数据长度必须等于数据块长
        if (input.length != cipherBlockSize - removedZeroLen) {
            throw new IllegalArgumentException("block incorrect size");
        }

        // check BT
        byte type = input[1 - removedZeroLen];
        if (type != 1 && type != 2) {
            throw new IllegalArgumentException("unknown block type");
        }

        // PS
        int start = 2 - removedZeroLen;
        for (; start != input.length; start++) {
            byte pad = input[start];
            if (pad == 0) {
                break;
            }
            if (type == 1 && pad != (byte) 0xff) { // private key padding
                throw new IllegalArgumentException("invalid block padding");
            }
        }

        // get D
        start++; // data should start at the next byte
        if (start > input.length || start < 11 - removedZeroLen) {
            throw new IllegalArgumentException("invalid block data");
        }
        out.write(input, start, input.length - start);
    }

}

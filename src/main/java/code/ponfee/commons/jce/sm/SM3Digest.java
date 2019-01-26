package code.ponfee.commons.jce.sm;

import java.util.Arrays;

/**
 * SM3 digest implementation
 * 
 * reference the internet code and refactor optimization
 * 
 * @author Ponfee
 */
public class SM3Digest {

    /** SM3值的长度 */
    private static final int DIGEST_SIZE = 32;

    /** SM3分组块大小 */
    private static final int BLOCK_SIZE = 64;

    /** 缓冲区长度 */
    private static final int BUFFER_LENGTH = BLOCK_SIZE;

    private final byte[] xBuf = new byte[BUFFER_LENGTH], // 缓冲区
                           iv = new byte[SM3.IV.length]; // 初始向量

    private int xBufOffset, // 缓冲区偏移量
                  cntBlock; // block数量

    private SM3Digest() {
        this.reset();
    }

    private SM3Digest(SM3Digest t) {
        System.arraycopy(t.xBuf, 0, this.xBuf, 0, t.xBuf.length);
        System.arraycopy(t.iv, 0, this.iv, 0, t.iv.length);
        this.xBufOffset = t.xBufOffset;
        this.cntBlock = t.cntBlock;
    }

    public static SM3Digest getInstance() {
        return new SM3Digest();
    }

    public static SM3Digest getInstance(SM3Digest t) {
        return new SM3Digest(t);
    }

    public void update(byte[] in) {
        this.update(in, 0, in.length);
    }

    /**
     * 明文输入
     * @param input 明文输入缓冲区
     * @param inputOffset 缓冲区偏移量
     * @param len 明文长度
     */
    public void update(byte[] input, int inputOffset, int len) {
        int partLen = BUFFER_LENGTH - xBufOffset, 
            dPos = inputOffset;
        if (partLen < len) {
            System.arraycopy(input, dPos, xBuf, xBufOffset, partLen);
            len -= partLen;
            dPos += partLen;
            doUpdate();
            while (len > BUFFER_LENGTH) {
                System.arraycopy(input, dPos, xBuf, 0, BUFFER_LENGTH);
                len -= BUFFER_LENGTH;
                dPos += BUFFER_LENGTH;
                doUpdate();
            }
        }

        System.arraycopy(input, dPos, xBuf, xBufOffset, len);
        xBufOffset += len;
    }

    public void update(byte in) {
        update(new byte[] { in }, 0, 1);
    }

    /**
     * SM3结果输出
     * @param out       保存SM3结构的缓冲区
     * @param outOffset 缓冲区偏移量
     */
    public void doFinal(byte[] out, int outOffset) {
        byte[] tmp = this.doFinal();
        System.arraycopy(tmp, 0, out, outOffset, tmp.length);
    }

    public byte[] doFinal(byte[] in) {
        this.update(in, 0, in.length);
        return this.doFinal();
    }

    public byte[] doFinal() {
        byte[] B = new byte[BLOCK_SIZE];
        byte[] buffer = new byte[xBufOffset];
        System.arraycopy(xBuf, 0, buffer, 0, buffer.length);
        byte[] tmp = SM3.padding(buffer, cntBlock);
        for (int i = 0; i < tmp.length; i += BLOCK_SIZE) {
            System.arraycopy(tmp, i, B, 0, B.length);
            this.doHash(B);
        }
        byte[] v = Arrays.copyOf(iv, iv.length);
        this.reset();
        return v;
    }

    public void reset() {
        xBufOffset = 0;
        cntBlock = 0;
        System.arraycopy(SM3.IV, 0, iv, 0, SM3.IV.length);
    }

    public static int getDigestSize() {
        return DIGEST_SIZE;
    }

    private void doUpdate() {
        byte[] B = new byte[BLOCK_SIZE];
        for (int i = 0; i < BUFFER_LENGTH; i += BLOCK_SIZE) {
            System.arraycopy(xBuf, i, B, 0, B.length);
            doHash(B);
        }
        xBufOffset = 0;
    }

    private void doHash(byte[] B) {
        byte[] tmp = SM3.cf(iv, B);
        System.arraycopy(tmp, 0, iv, 0, iv.length);
        cntBlock++;
    }

    private static class SM3 {
        static final byte[] IV = {
            (byte) 0x73, (byte) 0x80, (byte) 0x16, (byte) 0x6f, (byte) 0x49,
            (byte) 0x14, (byte) 0xb2, (byte) 0xb9, (byte) 0x17, (byte) 0x24,
            (byte) 0x42, (byte) 0xd7, (byte) 0xda, (byte) 0x8a, (byte) 0x06,
            (byte) 0x00, (byte) 0xa9, (byte) 0x6f, (byte) 0x30, (byte) 0xbc,
            (byte) 0x16, (byte) 0x31, (byte) 0x38, (byte) 0xaa, (byte) 0xe3,
            (byte) 0x8d, (byte) 0xee, (byte) 0x4d, (byte) 0xb0, (byte) 0xfb,
            (byte) 0x0e, (byte) 0x4e
        };

        static final int[] T_J = new int[64];
        static {
            for (int i = 0; i < 16; i++) {
                T_J[i] = 0x79cc4519;
            }

            for (int i = 16; i < 64; i++) {
                T_J[i] = 0x7a879d8a;
            }
        }

        static byte[] cf(byte[] V, byte[] B) {
            return convert(cf(convert(V), convert(B)));
        }

        /**
         * 对最后一个分组字节数据padding
         * @param in
         * @param bLen 分组个数
         * @return
         */
        static byte[] padding(byte[] in, int bLen) {
            int k = 448 - (((in.length << 3) + 1) & 0x1FF); // % 512
            if (k < 0) {
                k = 960 - (((in.length << 3) + 1) & 0x1FF);
            }
            k += 1;
            byte[] padd = new byte[k / 8];
            padd[0] = (byte) 0x80;
            long n = (in.length << 3) + (bLen << 9);
            byte[] out = new byte[in.length + k / 8 + 8];
            int pos = 0;
            System.arraycopy(in, 0, out, 0, in.length);
            pos += in.length;
            System.arraycopy(padd, 0, out, pos, padd.length);
            pos += padd.length;
            byte[] tmp = back(longToByteArray(n));
            System.arraycopy(tmp, 0, out, pos, tmp.length);
            return out;
        }

        static int[] convert(byte[] arr) {
            int[] out = new int[arr.length >>> 2];
            byte[] tmp = new byte[4];
            for (int i = 0; i < arr.length; i += 4) {
                System.arraycopy(arr, i, tmp, 0, 4);
                out[i >>> 2] = bigEndianByteToInt(tmp);
            }
            return out;
        }

        static byte[] convert(int[] arr) {
            byte[] out = new byte[arr.length << 2];
            byte[] tmp;
            for (int i = 0; i < arr.length; i++) {
                tmp = bigEndianIntToByte(arr[i]);
                System.arraycopy(tmp, 0, out, i << 2, 4);
            }
            return out;
        }

        static int[] cf(int[] V, int[] B) {
            int a = V[0], b = V[1], c = V[2], d = V[3],
                e = V[4], f = V[5], g = V[6], h = V[7];

            int ss1, ss2, tt1, tt2;
            int[][] arr = expand(B);
            int[] w = arr[0], w1 = arr[1];

            for (int j = 0; j < 64; j++) {
                ss1 = (bitCycleLeft(a, 12) + e + bitCycleLeft(T_J[j], j));
                ss1 = bitCycleLeft(ss1, 7);
                ss2 = ss1 ^ bitCycleLeft(a, 12);
                tt1 = FFj(a, b, c, j) + d + ss2 + w1[j];
                tt2 = GGj(e, f, g, j) + h + ss1 + w[j];
                d = c;
                c = bitCycleLeft(b, 9);
                b = a;
                a = tt1;
                h = g;
                g = bitCycleLeft(f, 19);
                f = e;
                e = P0(tt2);
            }

            int[] out = new int[8];
            out[0] = a ^ V[0];
            out[1] = b ^ V[1];
            out[2] = c ^ V[2];
            out[3] = d ^ V[3];
            out[4] = e ^ V[4];
            out[5] = f ^ V[5];
            out[6] = g ^ V[6];
            out[7] = h ^ V[7];

            return out;
        }

        static int[][] expand(int[] B) {
            int  W[] = new int[68];
            int W1[] = new int[64];
            System.arraycopy(B, 0, W, 0, B.length);

            for (int i = 16; i < 68; i++) {
                W[i] = P1(W[i - 16] 
                     ^ W[i - 9] 
                     ^ bitCycleLeft(W[i - 3], 15))
                     ^ bitCycleLeft(W[i - 13], 7) 
                     ^ W[i - 6];
            }

            for (int i = 0; i < 64; i++) {
                W1[i] = W[i] ^ W[i + 4];
            }

            return new int[][] { W, W1 };
        }

        static byte[] bigEndianIntToByte(int num) {
            return back(intToByteArray(num));
        }

        static int bigEndianByteToInt(byte[] bytes) {
            return byteArrayToInt(back(bytes));
        }

        static int FFj(int X, int Y, int Z, int j) {
            if (j >= 0 && j <= 15) {
                return FF1j(X, Y, Z);
            } else {
                return FF2j(X, Y, Z);
            }
        }

        static int GGj(int X, int Y, int Z, int j) {
            if (j >= 0 && j <= 15) {
                return GG1j(X, Y, Z);
            } else {
                return GG2j(X, Y, Z);
            }
        }

        // 逻辑位运算函数
        static int FF1j(int X, int Y, int Z) {
            return X ^ Y ^ Z;
        }

        static int FF2j(int X, int Y, int Z) {
            return ((X & Y) | (X & Z) | (Y & Z));
        }

        static int GG1j(int X, int Y, int Z) {
            return X ^ Y ^ Z;
        }

        static int GG2j(int X, int Y, int Z) {
            return (X & Y) | (~X & Z);
        }

        static int P0(int X) {
            return X ^ bitCycleLeft(X,  9) ^ bitCycleLeft(X, 17);
        }

        static int P1(int X) {
            return X ^ bitCycleLeft(X, 15) ^ bitCycleLeft(X, 23);
        }

        /**
         * 字节数组逆序
         * 
         * @param in
         * @return
         */
        static byte[] back(byte[] in) {
            byte[] out = new byte[in.length];
            for (int i = 0; i < out.length; i++) {
                out[i] = in[out.length - i - 1];
            }
            return out;
        }

        static int bitCycleLeft(int n, int bitLen) {
            bitLen &= 0x1F; // bitLen %= 32;
            byte[] tmp = bigEndianIntToByte(n);
            int byteLen = bitLen / 8;
            int len = bitLen & 0x07; // bitLen % 8
            if (byteLen > 0) {
                tmp = byteCycleLeft(tmp, byteLen);
            }
            if (len > 0) {
                tmp = bitSmall8CycleLeft(tmp, len);
            }
            return bigEndianByteToInt(tmp);
        }

        static byte[] bitSmall8CycleLeft(byte[] in, int len) {
            byte[] tmp = new byte[in.length];
            int t1, t2, t3;
            for (int i = 0; i < tmp.length; i++) {
                t1 = (byte) ((in[i] & 0x000000ff) << len);
                t2 = (byte) ((in[(i + 1) % tmp.length] & 0x000000ff) >> (8 - len));
                t3 = (byte) (t1 | t2);
                tmp[i] = (byte) t3;
            }
            return tmp;
        }

        static byte[] byteCycleLeft(byte[] in, int byteLen) {
            byte[] tmp = new byte[in.length];
            System.arraycopy(in, byteLen, tmp, 0, in.length - byteLen);
            System.arraycopy(in, 0, tmp, in.length - byteLen, byteLen);
            return tmp;
        }

        /**
         * 整形转换成网络传输的字节流（字节数组）型数据
         * @param num 一个整型数据
         * @return 4个字节的自己数组
         */
        static byte[] intToByteArray(int num) {
            return new byte[] {
                (byte) (num       ), (byte) (num >>>  8),
                (byte) (num >>> 16), (byte) (num >>> 24)
            };
        }

        /**
         * 四个字节的字节数据转换成一个整形数据
         * @param bytes 4个字节的字节数组
         * @return 一个整型数据
         */
        static int byteArrayToInt(byte[] bytes) {
            return (bytes[3]       ) << 24 // 转int后左移24位，刚好剩下原来的8位，故不用&0xFF
                 | (bytes[2] & 0xFF) << 16 // 默认转int
                 | (bytes[1] & 0xFF) <<  8
                 | (bytes[0] & 0xFF);
        }
        
        /**
         * 长整形转换成网络传输的字节流（字节数组）型数据
         * @param value 一个长整型数据
         * @return 4个字节的自己数组
         */
        static byte[] longToByteArray(long value) {
            return new byte[] {
                (byte) (value       ), (byte) (value >>>  8), 
                (byte) (value >>> 16), (byte) (value >>> 24), 
                (byte) (value >>> 32), (byte) (value >>> 40), 
                (byte) (value >>> 48), (byte) (value >>> 56) 
            };
        }
    }

}

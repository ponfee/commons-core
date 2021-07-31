package code.ponfee.commons.jce.implementation.digest;

import code.ponfee.commons.math.Maths;
import code.ponfee.commons.util.Bytes;

import java.util.Arrays;

import static code.ponfee.commons.math.Numbers.BYTE_ZERO;

/**
 * <pre>
 * The SHA-1 digest implementation（maximum 2^64 bit length）
 * 
 * 异或⊕：(A ^ B)
 * 同或⊙：(A ^ B ^ 1)  or  !(A ^ B)
 * A ≡ A ⊕ K ⊕ K
 *
 * https://www.cnblogs.com/scu-cjx/p/6878853.html
 * 
 * 安全性：SHA1所产生的摘要比MD5长32位。若两种散列函数在结构上没有任何问题的话，SHA1比MD5更安全。
 * 速度：两种方法都是主要考虑以32位处理器为基础的系统结构。但SHA1的运算步骤比MD5多了16步，而且SHA1记录单元的长度比MD5多了32位。因此若是以硬件来实现SHA1，其速度大约比MD5慢了25％。
 * 简易性：两种方法都是相当的简单，在实现上不需要很复杂的程序或是大量存储空间。然而总体上来讲，SHA1对每一步骤的操作描述比MD5简单与MD5不同的是SHA1的原始报文长度不能超过2的64次方，另外SHA1的明文长度从低位开始填充<p>
 * 
 * 1、按每512bit（64byte）长度进行分组block，可以划分成L份明文分组，我们用Y0,Y1, ...YL-1表示，对于每一个明文分组，都要重复反复的处理
 * 
 * 2、最后一组先补一个字节1000 0000(-128)，直到长度满足对512取模后余数是448（若已经是56byte即448bit，补后有57byte，
 *   因此还需要补64-57+56=63byte，会多出一组）
 * 
 * 3、最后补8byte即64bit的原始数据长度long值(位长)，此时为448+64=512bit
 * 
 * 4、将512位的明文分组划分为16个子明文分组（sub-block），每个子明文分组为32位，使用W[t]（t=0,1,...,15）来表示这16份子明文分组
 *   W[t]存的是int数据，即4个byte为一组的32位的word字
 * 
 * 5、16份子明文分组扩展为80份，记为W[t]（t=0,1,...,79），扩充的方法：
 *   > W[t] = W[t]，当0≤t≤15
 *   > W[t] = (W[t-3] ⊕ W[t-8] ⊕ W[t-14] ⊕ W [t-16]) << 1，当16≤t≤79
 * 
 * 6、分组处理：接下来，对输入分组进行80个步骤的处理，目的是根据输入分组的信息来改变内部状态，
 *   在对分组处理时，SHA-1中常数Kt如下：
 *   K0 = 0x5A827999    0≤t≤19
 *   K1 = 0x6ED9EBA1   20≤t≤39
 *   K2 = 0x8F1BBCDC   40≤t≤59
 *   K3 = 0xCA62C1D6   60≤t≤79
 * 
 *   5个链变量a,b,c,d,e如下：
 *   a = 0x67452301
 *   b = 0xEFCDAB89
 *   c = 0x98BADCFE
 *   d = 0x10325476
 *   e = 0xC3D2E1F0
 * 
 *   SHA1有4轮运算，每一轮包括20个步骤一共80步，当第1轮运算中的第1步骤开始处理时a、b、c、d、e五个链接变量中的值先赋值到另外
 *   5个记录单元a′、b′、c′、d′、e′中，这5个值将保留，用于在第4轮的最后一个步骤完成之后与链接变量a、b、c、d、e进行求和操作
 * 
 * 7、SHA-1使用了F0,F1,....,F79这样的一个逻辑函数序列，每一个Ft对3个32位双字b,c,d进行操作，产生一个32位双字的输出。
 *   Ft(b,c,d) = (b&c)|((~b)&d)      0≤t≤19
 *   Ft(b,c,d) = b^c^d              20≤t≤39
 *   Ft(b,c,d) = (b&c)|(b&d)|(c&d)  40≤t≤59
 *   Ft(b,c,d) = b^c^d              60≤t≤79
 * 
 * 8、W[0] ~ W[19]处理：（注：S为循环左移位操作）
 *   for (int t=0; t<20; t++) {
 *     tmp=K0+F0(b,c,d)+S(5,a)+e+(sh->W[t]); // 将Kt+Ft(b,c,d)+(a<<5)+e+W[t]的结果赋值给临时变量tmp
 *     e=d;                                  // 将链接变量d初始值赋值给链接变量e
 *     d=c;                                  // 将链接变量c初始值赋值给链接变量d
 *     c=S(30,b);                            // 将链接变量b初始值循环左移30位赋值给链接变量c
 *     b=a; a=tmp;                           // 将链接变量a初始值赋值给链接变量b，再将tmp赋值给a
 *   }
 * 
 *   W[20] ~ W[39]处理：
 *   for (int t=20; t<40; t++) {
 *     tmp=K1+F1(b,c,d)+S(5,a)+e+(sh->W[t]);
 *     e=d; d=c;
 *     c=S(30,b);
 *     b=a; a=tmp;
 *   }
 * 
 *   W[40] ~ W[59]处理：
 *   for (int t=40; t<60; t++) {
 *     tmp=K2+F2(b,c,d)+S(5,a)+e+(sh->W[t]);
 *     e=d; d=c;
 *     c=S(30,b);
 *     b=a; a=tmp;
 *   }
 * 
 *   W[60] ~ W[79]处理：
 *   for (int t=60; t<80; t++) {
 *     tmp=K3+F3(b,c,d)+S(5,a)+e+(sh->W[t]);
 *     e=d; d=c;
 *     c=S(30,b);
 *     b=a; a=tmp;
 *   }
 *   即：Kt+Ft(b,c,d)+S(5,a)+e+Wt, a, S(30,b), c, d  →  a, b, c, d, e
 * 
 * 9、将循环80个步骤后的值a,b,c,d,e与原始链变量a′、b′、c′、d′、e′相加作为下一个明文分组的输入重复进行以上操作
 *   sh->a′+=a; sh->b′+=b; sh->c′+=c; 
 *   sh->d′+=d; sh->e′+=e; 
 * 
 * 10、最后一个分组处理完成后，最终得到的a,b,c,d,e即为160位的消息摘要
 * </pre>
 *
 * @author Ponfee
 */
public class SHA1Digest {

    /** SHA-1分组中每块的大小 */
    private static final int BLOCK_SIZE = 64;

    /** SHA-1摘要byte大小 */
    private static final int DIGEST_SIZE = 20;

    private static final int WORK_SIZE = 80;

    /** 填充的边界 */
    private static final int PADDING_BOUNDS = 448 >>> 3; // 56，long=8byte=64bit

    /** 4个常数K */
    private static final int K0 = 0x5A827999,
                             K1 = 0x6ED9EBA1,
                             K2 = 0x8F1BBCDC,
                             K3 = 0xCA62C1D6;

    // ---------------------------------------------fields
    private final  int[]  work = new int[WORK_SIZE];
    private final byte[] block = new byte[BLOCK_SIZE];

    private int H0, H1, H2, H3, H4, blockOffset;
    private long dataByteCount;

    private SHA1Digest() {
        this.reset();
    }

    private SHA1Digest(SHA1Digest d) {
        this.H0 = d.H0;
        this.H1 = d.H1;
        this.H2 = d.H2;
        this.H3 = d.H3;
        this.H4 = d.H4;

        System.arraycopy(d.block, 0, this.block, 0, BLOCK_SIZE);
        this.blockOffset = d.blockOffset;
        this.dataByteCount = d.dataByteCount;
    }

    public static SHA1Digest getInstance() {
        return new SHA1Digest();
    }

    public static SHA1Digest getInstance(SHA1Digest d) {
        return new SHA1Digest(d);
    }

    public void update(byte input) {
        this.block[this.blockOffset++] = input;
        if (this.blockOffset == BLOCK_SIZE) {
            this.digestBlock(this.block);
            this.blockOffset = 0;
            this.dataByteCount += BLOCK_SIZE;
        }
    }

    public void update(byte[] input) {
        this.update(input, 0, input.length);
    }

    public void update(byte[] input, int offset, int length) {
        length = Math.min(input.length - offset, length);
        for (int i = offset, end = offset + length; i < end; i++) {
            this.update(input[i]);
        }
    }

    public byte[] doFinal(byte[] data) {
        this.update(data, 0, data.length);
        return this.doFinal();
    }

    public byte[] doFinal() {
        this.dataByteCount += this.blockOffset;
        this.block[this.blockOffset++] = -128; // 填充：先补1000 0000
        if (this.blockOffset > PADDING_BOUNDS) {
            Arrays.fill(this.block, this.blockOffset, BLOCK_SIZE, BYTE_ZERO); // 填充0
            this.digestBlock(this.block);

            // reset a empty block, repadding 0x00 and bit length start 0
            this.blockOffset = 0;
        }

        Arrays.fill(this.block, this.blockOffset, PADDING_BOUNDS, BYTE_ZERO);

        long dataLongBitLen = this.dataByteCount << 3; // bitLen=byteCount*8
        // dataLongBitLen value to byte array and padding in block tail
        for (int i = 0, j = (Long.BYTES - 1) << 3; i < Long.BYTES; i++, j -= 8) {
            this.block[PADDING_BOUNDS + i] = (byte) (dataLongBitLen >>> j);
        }

        this.digestBlock(this.block);

        byte[] digest = new byte[DIGEST_SIZE];
        Bytes.putInt(this.H0, digest,  0);
        Bytes.putInt(this.H1, digest,  4);
        Bytes.putInt(this.H2, digest,  8);
        Bytes.putInt(this.H3, digest, 12);
        Bytes.putInt(this.H4, digest, 16);

        this.reset();

        return digest;
    }

    public void reset() {
        this.H0 = 0x67452301;
        this.H1 = 0xEFCDAB89;
        this.H2 = 0x98BADCFE;
        this.H3 = 0x10325476;
        this.H4 = 0xC3D2E1F0;

        this.blockOffset   = 0;
        this.dataByteCount = 0;
    }

    public static int getDigestSize() {
        return DIGEST_SIZE;
    }

    // --------------------------------------------------private methods
    private void digestBlock(byte[] block) {
        int i = 0;

        // sub-block（子明文分组）
        for (int j = 0; i < 16; j += 4) {
            work[i++] = Bytes.toInt(block, j);
        }

        // ext-block（扩展明文分组）
        for (; i < WORK_SIZE; i++) {
            work[i] = Maths.rotateLeft(work[i - 3] ^ work[i - 8] ^ work[i - 14] ^ work[i - 16], 1);
        }

        int A = this.H0, B = this.H1, C = this.H2, D = this.H3, E = this.H4, tmp, t = 0;

        // round first
        for (; t < 20; t++) {
            // temp = Ki + fi(B, C, D) + S5(A) + E + Wt
            tmp = K0 + f0(B, C, D) + Maths.rotateLeft(A, 5) + E + work[t];

            // E = D; D = C; C = S30(B); B = A; A = temp;
            E = D; D = C; C = Maths.rotateLeft(B, 30); B = A; A = tmp;
        }

        // round second
        for (; t < 40; t++) {
            tmp = K1 + f1(B, C, D) + Maths.rotateLeft(A, 5) + E + work[t];
            E = D; D = C; C = Maths.rotateLeft(B, 30); B = A; A = tmp;
        }

        // round third
        for (; t < 60; t++) {
            tmp = K2 + f2(B, C, D) + Maths.rotateLeft(A, 5) + E + work[t];
            E = D; D = C; C = Maths.rotateLeft(B, 30); B = A; A = tmp;
        }

        // round fourth
        for (; t < WORK_SIZE; t++) {
            tmp = K3 + f3(B, C, D) + Maths.rotateLeft(A, 5) + E + work[t];
            E = D; D = C; C = Maths.rotateLeft(B, 30); B = A; A = tmp;
        }

        // add chain variable
        this.H0 += A;
        this.H1 += B;
        this.H2 += C;
        this.H3 += D;
        this.H4 += E;
    }

    private static int f0(int b, int c, int d) {
        return (b & c) | ((~b) & d);
    }

    private static int f1(int b, int c, int d) {
        return b ^ c ^ d;
    }

    private static int f2(int b, int c, int d) {
        return (b & c) | (b & d) | (c & d);
    }

    private static int f3(int b, int c, int d) {
        return f1(b, c, d);
    }

}

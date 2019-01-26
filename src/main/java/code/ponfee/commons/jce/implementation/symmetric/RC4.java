package code.ponfee.commons.jce.implementation.symmetric;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

/**
 * RC4 implementation
 * 
 * 给定一个短的密码，储存在key[MAX]数组里，还有一个数组S[256]，令S[i]=i。
 * 然后利用数组key来对数组S做一个置换，也就是对S数组里的数重新排列，排列算法为
 * KSA：密钥调度算法
 * j := 0
 * for i from 0 to 255
 *   j := (j + S[i] + key[i mod keyLen]) mod sboxLen
 *   swap values of S[i] and S[j]
 * endfor
 * 
 * @author Ponfee
 */
public class RC4 {

    private final static int STATE_LENGTH = 256;

    /** variables to hold the state of the RC4  during encryption and decryption */
    private final byte[] sBox;

    /**
     * Constructs a RC4 Cryptor, input the specified key byte array 
     * and init the sbox in this methods
     * 
     * @param keyBytes
     */
    public RC4(byte[] keyBytes) {
        // KSA：密钥调度算法
        // 生成并填充s-box
        this.sBox = new byte[STATE_LENGTH];
        for (int i = 0; i < STATE_LENGTH; i++) {
            this.sBox[i] = (byte) i;
        }

        // 置换s-box
        for (int i = 0, j = 0, k = 0, keyLen = keyBytes.length; i < STATE_LENGTH; i++) {
            j = (j + sBox[i] + keyBytes[k++]) & 0xFF; // & 0xFF -> modulo sboxLen

            ArrayUtils.swap(this.sBox, i, j);

            if (k == keyLen) {
                k = 0;
            }
        }
    }

    // -----------------------------------------------------------crypt one byte
    public byte encrypt(byte in) {
        byte[] sBox = Arrays.copyOf(this.sBox, this.sBox.length);
        int x = 1;
        int y = sBox[x] & 0xFF;

        ArrayUtils.swap(sBox, x, y);

        // xor
        return (byte) (in ^ sBox[(sBox[x] + sBox[y]) & 0xFF]);
    }

    public byte decrypt(byte in) {
        return this.encrypt(in);
    }

    // -----------------------------------------------------------crypt byte array
    public byte[] encrypt(byte[] in) {
        byte[] out = new byte[in.length];
        this.docrypt(in, 0, in.length, out, 0);
        return out;
    }

    public byte[] decrypt(byte[] in) {
        return this.encrypt(in);
    }

    // -----------------------------------------------------------private methods
    private void docrypt(byte[] in, int inOff, int len, byte[] out, int outOff) {
        byte[] sBox = Arrays.copyOf(this.sBox, this.sBox.length);

        // RPGA：伪随机生成算法，不断的重排S盒来产生任意长度的密钥流
        for (int i = 0, x = 0, y = 0; i < len; i++) {
            x = (x + 1) & 0xFF;
            y = (sBox[x] + y) & 0xFF;

            ArrayUtils.swap(sBox, x, y);

            // xor
            out[i + outOff] = (byte) (in[i + inOff] ^ sBox[(sBox[x] + sBox[y]) & 0xFF]);
        }
    }

}

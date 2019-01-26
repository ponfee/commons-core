package test.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import code.ponfee.commons.util.Bytes;

/**
 * http://blog.csdn.net/yezhubenyue/article/details/7436624
 * http://blog.csdn.net/xieyihua1994/article/details/51659379
 * http://blog.csdn.net/gaoshuang5678/article/details/50554131
 * 
 *  整数部分除2取余，直到商为0停止，从最后的余数读起，一直到最前面的余数
 *  小数部分乘2取整，然后从前往后读
 *
 * Float: 
 *   S:sign符号, 
 *   E:exponent指数（0~2^8-1=255）
 *   e:移码（负数左移，正数右移），e=E-127
 *   M:mantissa尾数（0~2^23-1=8388607）
 *   
 *   SEEE EEEE E[1]MMM MMMM MMMM MMMM MMMM MMMM
 * 
 *   1100 0001 0[1]100 1000 0000 0000 0000 0000   // -12.5
 *   S: 1
 *   E: 100 0001 0  // 130
 *   e: E-127 = 130-127 = 3
 *   M: [1]100 1000 0000 0000 0000 0000   // 即: 1.100 1000 0000 0000 0000 0000
 *   
 *   移码：如果指数e为负数，底数的小数点向左移，如果指数e为正数，底数的小数点向右移
 *   M(1.100 1000 0000 0000 0000 0000)向右移3位 
 *   --> 1100. 1000 0000 0000 0000 0000
 *   
 *   小数点左边的1100 表示为 (1 × 2^3) + (1 × 2^2) + (0 × 2^1) + (0 × 2^0), 其结果为 12
 *   小数点右边的 .100… 表示为 (1 × 2^-1) + (0 × 2^-2) + (0 × 2^-3) + ... ，其结果为.5
 *   以上二值的和为12.5， 由于S 为1，使用为负数，即-12.5 。
 * 
 * 
 *   将浮点数转成二进制：0.5->0.1, 0.25->0.01, 0.125->0.001, 0.0625->0.0001
 *   小数的二进制算法和整数的大致相反，就是不断的拿小数部分乘以2取积的整数部分，然后正序排列，比如求0.9的二进制：
 *        0.9*2=1.8 取 1 
 *        0.8*2=1.6 取 1 
 *        0.6*2=1.2 取 1 
 *        0.2*2=0.4 取 0 
 *        0.4*2=0.8 取 0 
 *        0.8*2=1.6 取 1 
 * 
 *   17.625
 *     转二进制：1 0001.101
 *     偏移：右移N位，直到小数点前只剩一位：1.0001 101（即右移4位），M=1.0001 101
 *     则e=4，即E=127+e=131 ==> E=10000011
 *     S=0
 *     M省略小数点前面的1（向左或向右移后，最左边的数总是1）
 *     则其二进制为：0 10000011 000 1101 0000 0000 0000 0000
 * 
 * 指数全为0（E=0）：
 *     1、尾数全0：则这个数的真值为±0（正负号和数符位有关）
 *     2、尾数不全为0：表明当前的float数是一个非规格化的数
 *                （小于2^-127，所以float的最小值指数位为0000 0001=1-127=-126，尾数最后一位为1，即00000...0001）
 * 
 * 指数全为1（E=255）：
 *     1、尾数全0：值为±∞，+∞：0 11111111 000 0000 0000 0000 0000。-∞：1 11111111 000 0000 0000 0000 0000
 *     2、尾数不全为0：表明为NaN（所以float的最大值的指数位为1111 1110=254-127=127，尾数位全为1）
 * 
 * Double:
 *   SEEE EEEE EEEE [1]MMMM MMMM MMMM MMMM MMMM MMMM MMMM MMMM MMMM MMMM MMMM MMMM MMMM
 *   e = E-1023
 * 
 * 
 *   0                       00000000000000000000000000000000
 *   Float.MIN_VALUE         00000000000000000000000000000001  // 非规格化的最小值
 *   Float.MIN_NORMAL        00000000100000000000000000000000  // 规格化的最小值
 *   Float.MAX_VALUE         01111111011111111111111111111111
 *   Float.NaN               01111111110000000000000000000000
 *   Float.NEGATIVE_INFINITY 11111111100000000000000000000000
 *   Float.POSITIVE_INFINITY 01111111100000000000000000000000
 * 
 * @author Ponfee
 */
public class FloatContent {

    public static void main(String[] args) {
        System.out.println(Bytes.toBinary(Bytes.fromFloat(-12.5f)));
        System.out.println(Bytes.toBinary(ByteBuffer.allocate(4).putFloat(-12.5f).array()));

        System.out.println();
        System.out.println(Bytes.toBinary(Bytes.fromFloat(17.625f)));
        System.out.println(Bytes.toBinary(ByteBuffer.allocate(4).putFloat(17.625f).array()));

        System.out.println();
        System.out.println(Bytes.toBinary(Bytes.fromFloat(6.9f)));
        System.out.println(Bytes.toBinary(Bytes.fromFloat(0.9f)));
        
        System.out.println();
        System.out.println(ByteOrder.nativeOrder());
        
        System.out.println(Bytes.toFloat(new byte[] {
            0, 0, 0, 127, 0, 0, 0, 0
        }));

        System.out.println(0x1.0p-3f); // 0x1.0*power(2,-3)
        System.out.println(0x0.9p-3f); // 0x0.5*power(2,-3)
    }
}

package code.ponfee.commons.math;

import javax.validation.constraints.Min;

/**
 * 数学算术
 * 取模：Modulo Operation
 *
 * @author Ponfee
 */
public class Maths {

    /**
     * 以2为底n的对数
     * 
     * @param n the value
     * @return a value of log(n)/log(2)
     */
    public static strictfp double log2(double n) {
        return log(n, 2);
    }

    /**
     * 求以base为底n的对数
     * {@link java.lang.Math#log10(double) }  求以10为底n的对数（lg）
     * {@link java.lang.Math#log(double)   }  以e为底n的对数（自然对数，ln）
     * {@link java.lang.Math#log1p(double) }  以e为底n+1的对数
     * 
     * @param n     a value
     * @param base  底数
     * @return a double of logarithm
     */
    public static strictfp double log(double n, double base) {
        return Math.log(n) / Math.log(base);
    }

    /**
     * rotate shift left，循环左移位操作：0<=n<=32
     * 
     * @param x the value
     * @param n shift bit len
     * @return a number of rotate left result
     */
    public static int rotateLeft(int x, int n) {
        return (x << n) | (x >>> (32 - n));
    }

    /**
     * Returns a long value for {@code base}<sup>{@code exponent}</sup>.
     * 
     * @param base      the base
     * @param exponent  the exponent
     * @return a long value for {@code base}<sup>{@code exponent}</sup>.
     */
    public static long pow(@Min(1) long base, @Min(0) int exponent) {
        if (exponent == 0) {
            return 1;
        }

        long result = base;
        while (--exponent > 0) {
            result *= base;
        }
        return result;
    }

}

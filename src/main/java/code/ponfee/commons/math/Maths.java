package code.ponfee.commons.math;

import javax.validation.constraints.Max;
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
    public static int rotateLeft(int x, @Min(0) @Max(32) int n) {
        return (x << n) | (x >>> (32 - n));
    }

    /**
     * Returns a long value of bit conut mask
     * calculate the bit counts mask long value
     *   a: (1 << bits) - 1
     *   b: -1L ^ (-1L << bits)
     *   c: Long.MAX_VALUE >>> (63 - bits)
     * 
     * @param bits the bit count
     * @return a long value
     */
    public static long bitsMask(int bits) {
        return (1L << bits) - 1;
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

    public static int abs(int a) {
        // Integer.MIN_VALUE & 0x7FFFFFFF = 0
        return (a == Integer.MIN_VALUE) ? Integer.MAX_VALUE : (a < 0) ? -a : a;
    }

    public static long abs(long a) {
        return (a == Long.MIN_VALUE) ? Long.MAX_VALUE : (a < 0) ? -a : a;
    }

    /**
     * Returns square root of specified double value<p>
     * Use binary search method
     * 
     * @param value the value
     * @return square root
     */
    public static strictfp double sqrtBinary(double value) {
        if (value < 0.0D) {
            return Double.NaN;
        }
        if (value == 0.0D || value == 1.0D) {
            return value;
        }

        double start, end, square, r;
        if (value > 1.0D) {
            start = 1.0D;
            end = value;
        } else {
            start = value;
            end = 1.0D;
        }
        while (!isBorderline(r = start + (end - start) / 2, start, end) && (square = r * r) != value) {
            if (square > value) {
                end = r; // lower
            } else {
                start = r; // upper
            }
        }

        return r; // cannot find a more rounded value
    }

    public static boolean isBorderline(double value, double start, double end) {
        return value == start || value == end;
    }

    /**
     * Returns square root of specified double value<p>
     * Use newton iteration method: X(n+1)=[X(n)+p/Xn]/2
     * 
     * @param value the value
     * @return square root
     */
    public static strictfp double sqrtNewton(double value) {
        if (value < 0) {
            return Double.NaN;
        }
        if (value == 0.0D || value == 1.0D) {
            return value;
        }

        double r = 1.0D;
        while (r != (r = (r + value / r) / 2)) {
            // do nothing
        }
        return r;
    }

    public static boolean isBorderline(int value, int start, int end) {
        return value == start || value == end;
    }

    public static boolean isBorderline(long value, long start, long end) {
        return value == start || value == end;
    }

    // ------------------------------------------------------------------------int plus/minus
    public static int plus(int a, int b) {
        if (a > 0 && b > 0) {
            return Integer.MAX_VALUE - b < a ? Integer.MAX_VALUE : a + b;
        } else if (a < 0 && b < 0) {
            return Integer.MIN_VALUE - b > a ? Integer.MIN_VALUE : a + b;
        } else {
            return a + b;
        }
    }

    public static int minus(int a, int b) {
        if (a > 0 && b < 0) {
            return Integer.MAX_VALUE + b < a ? Integer.MAX_VALUE : a - b;
        } else if (a < 0 && b > 0) {
            return Integer.MIN_VALUE + b > a ? Integer.MIN_VALUE : a - b;
        } else {
            return a - b;
        }
    }

    // ------------------------------------------------------------------------long plus/minus
    public static long plus(long a, long b) {
        if (a > 0 && b > 0) {
            return Long.MAX_VALUE - b < a ? Long.MAX_VALUE : a + b;
        } else if (a < 0 && b < 0) {
            return Long.MIN_VALUE - b > a ? Long.MIN_VALUE : a + b;
        } else {
            return a + b;
        }
    }

    public static long minus(long a, long b) {
        if (a > 0 && b < 0) {
            return Long.MAX_VALUE + b < a ? Long.MAX_VALUE : a - b;
        } else if (a < 0 && b > 0) {
            return Long.MIN_VALUE + b > a ? Long.MIN_VALUE : a - b;
        } else {
            return a - b;
        }
    }

    /**
     * Returns the greatest common divisor
     *
     * @param a the first number
     * @param b the second number
     * @return gcd
     */
    public static int gcd(int a, int b) {
        if (a < 0 || b < 0) {
            throw new ArithmeticException();
        }

        if (a == 0 || b == 0) {
            return Math.abs(a - b);
        }

        for (int c; (c = a % b) != 0;) {
            a = b;
            b = c;
        }
        return b;
    }

    /**
     * Returns the greatest common divisor in array
     *
     * @param array the int array
     * @return gcd
     */
    public static int gcd(int[] array) {
        int result = array[0];
        for (int i = 1; i < array.length; i++) {
            result = gcd(result, array[i]);
        }

        return result;
    }

}

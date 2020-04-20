/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2019, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package code.ponfee.commons.util;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

import java.util.Random;

import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.testng.Assert;

import code.ponfee.commons.math.Maths;
import code.ponfee.commons.math.Numbers;

/**
 * 
 * 
 * @author Ponfee
 */
public class MathsTest {

    @Test
    public void test1() {
        int a1 = 100;
        int b1 = Integer.MAX_VALUE - a1 + 1;
        System.out.println("\n==============正+正");
        MatcherAssert.assertThat(a1 + b1, is(lessThan(0))); // 溢出
        Assert.assertEquals(Maths.plus(a1, b1), Integer.MAX_VALUE);

        int a2 = -100;
        int b2 = Integer.MIN_VALUE - a2 - 1;
        System.out.println("\n==============负+负");
        MatcherAssert.assertThat(a2 + b2, is(greaterThan(0))); // 溢出
        Assert.assertEquals(Maths.plus(a2, b2), Integer.MIN_VALUE);

        System.out.println("\n==============正-负");
        int a3 = Integer.MAX_VALUE - 100;
        int b3 = -1000;
        MatcherAssert.assertThat(a3 - b3, is(lessThan(0))); // 溢出
        Assert.assertEquals(Maths.minus(a3, b3), Integer.MAX_VALUE);

        System.out.println("\n==============负-正");
        int a4 = Integer.MIN_VALUE + 10;
        int b4 = 1000;
        MatcherAssert.assertThat(a4 - b4, is(greaterThan(0))); // 溢出
        Assert.assertEquals(Maths.minus(a4, b4), Integer.MIN_VALUE);
    }

    @Test
    public void test2() {
        Random ran = new Random(SecureRandoms.nextLong());
        for (int i = 0; i < 6000; i++) {
            double num = Math.random() + ran.nextInt(Integer.MAX_VALUE);
            double a = Math.sqrt(num), b = /*Maths.sqrtNewton(num)*/Maths.sqrtBinary(num);
            //String s = "0011110100010000000000000000000000000000000000000000000000000000"; 
            // 0011110110010000000000000000000000000000000000000000000000000000
            if (a != b) {
                //System.out.println(Bytes.toBinary(Bytes.toBytes(Math.abs(a - b))));
                System.err.println(Numbers.format(a, 60) + ", " + Numbers.format(b, 60));
            }

        }
    }

    @Test
    public void test3() {
        double a = 0.00001;
        /*System.out.println(Math.sqrt(0.0D));
        System.out.println(Math.sqrt(1.0D));
        System.out.println(Math.sqrt(a));
        System.out.println(Maths.sqrt(a));*/
        
        a = 4D;
        System.out.println(Math.sqrt(a));
        System.out.println(Maths.sqrtBinary(a));
    }
    
    private static final int ROUND = 9999999;

    @Test
    public void test31() {
        for (int i = 2; i < ROUND; i++) {
            Math.sqrt(i);
        }
    }

    @Test
    public void test32() {
        for (int i = 2; i < ROUND; i++) {
            Maths.sqrtBinary(i);
        }
    }

    @Test
    public void test33() {
        for (int i = 2; i < ROUND; i++) {
            Maths.sqrtNewton(i);
        }
    }

    @Test
    public void test34() {
        System.out.println(Maths.sqrtNewton(0.01));
        System.out.println(Maths.sqrtNewton(4.0));
    }

    @Test
    public void test4() {
        System.out.println(find(98));
        Random ran = new Random();
        for (int i = 1; i < 100; i++) {
            int number = ran.nextInt(Integer.MAX_VALUE / 4);
            int a = find(number);
            /*System.out.println(number);
            System.out.println(a);
            System.out.println(calculate(a));
            System.out.println(calculate(a - 1));*/
        }
    }

    // ------------------------------------------------------------------------------------------
    // 2-99
    public static int find(int value) {
        int start = 1, end = value, number, less = -1, grater = -1;
        while (!Maths.isBorderline(number = (start + end) / 2, start, end)) {
            //System.out.println("-------" + temp);
            int a = calculate(number);
            if (a == value) {
                return number;
            } else if (a > value) {
                grater = number;
                end = Math.max(number - 1, start);
            } else {
                less = number;
                start = Math.min(number + 1, end);
            }
        }

        System.out.println(String.format("[less=%d,start=%d,end=%d,grater=%d]", less, start, end, grater));
        if (grater == -1) {
            return -1;
        }
        if (less == -1) {
            return calculate(start - 1) < value ? start : -1;
        }
        for (int i = grater - 1; i >= less; i--) {
            if (i == less) {
                return i + 1;
            } else if (calculate(i) < value) {
                return i + 1;
            }
        }
        return -1;
    }

    public static int calculate(int n) {
        if (n < 1) {
            throw new IllegalArgumentException();
        }

        int result = 0;
        do {
            result += n;
        } while (--n > 0);
        return result;
    }

    // ------------------------------------------------------------------------------------------
    public static double sqrt(double value) {
        if (value < 0) {
            return Double.NaN;
        }
        if (value == 0) {
            return 0.0D;
        }

        double start = 0.0D, end = value, result;
        for (double factor = value / 2;;) {
            result = factor * factor;
            if (result == value) {
                return factor;
            } else if (result > value) {
                end = factor;
                factor -= (end - start) / 2;
                if (end == factor) { // cannot calculate a more rounded value
                    return factor;
                }
            } else {
                start = factor;
                factor += (end - start) / 2;
                if (start == factor) { // cannot calculate a more rounded value
                    return factor;
                }
            }
        }
    }

}

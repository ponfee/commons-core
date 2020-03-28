/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2019, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package code.ponfee.commons.util;

import static org.hamcrest.Matchers.*;

import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.testng.Assert;

import code.ponfee.commons.math.Maths;

/**
 * 
 * 
 * @author Ponfee
 */
public class MathsTest {

    @Test
    public void test() {
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
}

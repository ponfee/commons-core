/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2019, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package code.ponfee.commons.util;

import org.junit.Test;

/**
 * 
 * 
 * @author Ponfee
 */
public class IdcardResolverTest {

    @Test
    public void test1() {
        for (int i = 0; i < 100; i++) {
            System.out.println(IdcardResolver.generate());
        }
    }

    @Test
    public void test2() {
        long start = Dates.toDate("1950-01-01 00:00:00").getTime();
        for (int i = 0; i < 100; i++) {
            System.out.println(Dates.format(Dates.random(start, System.currentTimeMillis()), "yyyy-MM-dd HH:mm:ss.SSS"));
        }
    }
}

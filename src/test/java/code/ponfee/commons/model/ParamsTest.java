/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2019, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package code.ponfee.commons.model;

import org.junit.Test;

/**
 * 
 * 
 * @author Ponfee
 */
public class ParamsTest {

    @Test
    public void test1() {
        PageParameter params = new PageParameter();
        params.setSort("name,   test   asc");
        params.validateSort("name", "test");
    }
}

/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2019, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package code.ponfee.commons.reflect;

import code.ponfee.commons.model.BaseEntity;

/**
 * 
 * 
 * @author Ponfee
 */
public class FieldsTest {

    public static class ClassA extends BaseEntity<Long, String> {
        private static final long serialVersionUID = -5617457253295566886L;

    }

    public static void main(String[] args) {
        ClassA a = new ClassA();
        System.out.println(a.getId());
        Fields.put(a, "id", 999L);
        System.out.println(a.getId());
    }
}

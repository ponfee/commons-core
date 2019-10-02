/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2018, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package code.ponfee.commons.model;

import java.lang.reflect.Constructor;

/**
 * 
 * The {@code Null} class is representing unable instance object
 * 
 * @author Ponfee
 */
public final class Null {

    public static final Constructor<Null> NONE_CONSTRUCTOR;
    static {
        try {
            NONE_CONSTRUCTOR = Null.class.getDeclaredConstructor();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Null() {
        throw new AssertionError("Null cannot create instance.");
    }

}

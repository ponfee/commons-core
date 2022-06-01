/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2018, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package code.ponfee.commons.model;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * The {@code Null} class is representing unable instance object
 * 
 * @author Ponfee
 */
public final class Null implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final Constructor<Null> BROKEN_CONSTRUCTOR;
    public static final Method BROKEN_METHOD;
    static {
        try {
            BROKEN_CONSTRUCTOR = Null.class.getDeclaredConstructor();
            BROKEN_METHOD = Null.class.getDeclaredMethod("broken");
        } catch (Exception e) {
            // cannot happen
            throw new Error(e);
        }
    }

    private Null() {
        throw new AssertionError("Null cannot create instance.");
    }

    private void broken() {
        throw new AssertionError("Forbid invoke this method.");
    }

}

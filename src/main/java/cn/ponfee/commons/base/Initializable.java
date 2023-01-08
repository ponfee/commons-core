/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.base;

/**
 * Initialize resources
 * 
 * @author Ponfee
 */
@FunctionalInterface
public interface Initializable {

    NoArgMethodInvoker INITIATOR = new NoArgMethodInvoker("open", "init", "initialize");

    void init();

    static void init(Object caller) {
        if (caller == null) {
            return;
        }

        if (caller instanceof Initializable) {
            ((Initializable) caller).init();
        } else {
            INITIATOR.invoke(caller);
        }
    }

}

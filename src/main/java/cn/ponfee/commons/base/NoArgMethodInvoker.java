/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.base;

import cn.ponfee.commons.reflect.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.Arrays;

/**
 * Specifies multiple non-arg method names, find the first and invoke it
 * 
 * @author Ponfee
 */
public final class NoArgMethodInvoker {

    private final String[] methodNames;

    /**
     * @param methodNames the no-arg method list
     */
    public NoArgMethodInvoker(@Nonnull String... methodNames) {
        if (methodNames == null || methodNames.length == 0) {
            throw new IllegalArgumentException("Must be specified least once no-arg method name.");
        }
        this.methodNames = methodNames;
    }

    public void invoke(Object caller) {
        if (caller == null) {
            return;
        }
        if (caller instanceof Class<?>) {
            throw new IllegalArgumentException("Invalid caller object " + caller);
        }

        Arrays.stream(methodNames)
              .filter(StringUtils::isNotBlank)
              .map(name -> ClassUtils.getMethod(caller, name))
              .findAny()
              .ifPresent(method -> ClassUtils.invoke(caller, method));
    }

}

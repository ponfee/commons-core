package code.ponfee.commons.base;

import code.ponfee.commons.reflect.ClassUtils;
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

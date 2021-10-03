package code.ponfee.commons.base;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import code.ponfee.commons.exception.CheckedException;
import code.ponfee.commons.exception.Throwables;

/**
 * Specifies multiple non-arg method names, find the first and invoke it
 * 
 * @author Ponfee
 */
public final class MethodInvoker {

    private static final Object NOT_FOUND_PLACEHOLDER = new Object();

    private final Map<Class<?>, Object> methodMappings = new HashMap<>();
    private final String[] methodNames;

    /**
     * @param methodNames the no-arg method list
     */
    public MethodInvoker(@Nonnull String... methodNames) {
        if (methodNames == null || methodNames.length == 0) {
            throw new IllegalArgumentException("Must be specified least once no-arg method name.");
        }
        this.methodNames = methodNames;
    }

    public void invoke(Object caller) {
        if (caller == null) {
            return;
        }

   
        Method method = findMethod(caller.getClass());
        if (method == null) {
            return;
        }

        try {
            method.invoke(caller);
        } catch (Exception e) {
            throw new CheckedException(
                "Invoke '" + caller.getClass().getName() + "#" + method.getName() + "()' failure.", e
            );
        }
    }

    private Method findMethod(Class<?> type) {
        Object method;
        if ((method = methodMappings.get(type)) == null) {
            synchronized (this) {
                if ((method = methodMappings.get(type)) == null) {
                    Exception reason = null;
                    for (String name : methodNames) {
                        try {
                            Method m = type.getMethod(name); // find the no arg public method
                            if (!Modifier.isStatic(m.getModifiers())) {
                                //m.setAccessible(true);
                                method = m;
                                break; // find an exists method
                            }
                        } catch (Exception e) {
                            if (reason == null) {
                                reason = e; // retention the first exception
                            }
                        }
                    }
                    if (method == null) {
                        method = NOT_FOUND_PLACEHOLDER;
                        if (reason != null) {
                            Throwables.console(reason); // Has not such method 
                        }
                    }
                    methodMappings.put(type, method);
                }
            }
        }

        return NOT_FOUND_PLACEHOLDER == method ? null : (Method) method;
    }

}

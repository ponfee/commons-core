package code.ponfee.commons.data.lookup;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Destroyable;
import javax.sql.DataSource;

import code.ponfee.commons.base.Initializable;
import code.ponfee.commons.exception.CheckedException;
import code.ponfee.commons.exception.Throwables;
import code.ponfee.commons.io.Closeables;

/**
 * Data source utility
 * 
 * @author Ponfee
 */
public class DataSourceUtils {

    private static final Object PLACE_HOLDER = new Object();

    public static void close(DataSource ds) {
        if (ds instanceof AutoCloseable) {
            Closeables.log((AutoCloseable) ds);
        } else if (ds instanceof Destroyable) {
            Closeables.log((Destroyable) ds);
        } else {
            Method close = findCloseMethod(ds.getClass());
            if (close != null) {
                try {
                    close.invoke(ds);
                } catch (Exception e) {
                    throw new CheckedException(
                        "Invoke '" + ds.getClass().getName() + ".close' method occur error.", e
                    );
                }
            }
        }
    }

    public static void init(DataSource ds) {
        if (ds instanceof Initializable) {
            ((Initializable) ds).init();
        } else {
            Method init = findInitMethod(ds.getClass());
            if (init != null) {
                try {
                    init.invoke(ds);
                } catch (Exception e) {
                    throw new CheckedException(
                        "Invoke '" + ds.getClass().getName() + ".init' method occur error.", e
                    );
                }
            }
        }
    }

    private static final Map<Class<?>, Object> CLOSE_MAPPING = new HashMap<>();
    private static <T extends DataSource> Method findCloseMethod(Class<T> type) {
        Object value;
        if ((value = CLOSE_MAPPING.get(type)) == null) {
            synchronized (CLOSE_MAPPING) {
                if ((value = CLOSE_MAPPING.get(type)) == null) {
                    try {
                        value = type.getDeclaredMethod("close");
                    } catch (Exception e) {
                        value = PLACE_HOLDER;
                        Throwables.console(e); // Has not 'close' method 
                    }
                    CLOSE_MAPPING.put(type, value);
                }
            }
        }
        return PLACE_HOLDER == value ? null : (Method) value;
    }

    private static final Map<Class<?>, Object> INIT_MAPPING = new HashMap<>();
    private static <T extends DataSource> Method findInitMethod(Class<T> type) {
        Object value;
        if ((value = INIT_MAPPING.get(type)) == null) {
            synchronized (INIT_MAPPING) {
                if ((value = INIT_MAPPING.get(type)) == null) {
                    try {
                        value = type.getDeclaredMethod("init");
                    } catch (Exception e) {
                        value = PLACE_HOLDER;
                        Throwables.console(e); // Has not 'init' method 
                    }
                    INIT_MAPPING.put(type, value);
                }
            }
        }
        return PLACE_HOLDER == value ? null : (Method) value;
    }

}

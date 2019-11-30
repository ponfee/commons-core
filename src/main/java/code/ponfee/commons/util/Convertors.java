/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2019, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package code.ponfee.commons.util;

import java.util.HashMap;
import java.util.Map;

import code.ponfee.commons.math.Numbers;

/**
 * Converts value to target type value
 * 
 * @author Ponfee
 */
@SuppressWarnings("unchecked")
public enum Convertors {

    BOOLEAN(boolean.class) {
        public @Override <T> T to(Object value) {
            return (T) (Boolean) Numbers.toBoolean(value);
        }
    },
    WRAP_BOOLEAN(Boolean.class) {
        public @Override <T> T to(Object value) {
            return (T) Numbers.toWrapBoolean(value);
        }
    },
    BYTE(byte.class) {
        public @Override <T> T to(Object value) {
            return (T) (Byte) Numbers.toByte(value);
        }
    },
    WRAP_BYTE(Byte.class) {
        public @Override <T> T to(Object value) {
            return (T) Numbers.toWrapByte(value);
        }
    },
    SHORT(short.class) {
        public @Override <T> T to(Object value) {
            return (T) (Short) Numbers.toShort(value);
        }
    },
    WRAP_SHORT(Short.class) {
        public @Override <T> T to(Object value) {
            return (T) Numbers.toWrapShort(value);
        }
    },
    CHAR(char.class) {
        public @Override <T> T to(Object value) {
            return (T) (Character) Numbers.toChar(value);
        }
    },
    WRAP_CHAR(Character.class) {
        public @Override <T> T to(Object value) {
            return (T) Numbers.toWrapChar(value);
        }
    },
    INT(int.class) {
        public @Override <T> T to(Object value) {
            return (T) (Integer) Numbers.toInt(value);
        }
    },
    WRAP_INT(Integer.class) {
        public @Override <T> T to(Object value) {
            return (T) Numbers.toWrapInt(value);
        }
    },
    LONG(long.class) {
        public @Override <T> T to(Object value) {
            return (T) (Long) Numbers.toLong(value);
        }
    },
    WRAP_LONG(Long.class) {
        public @Override <T> T to(Object value) {
            return (T) Numbers.toWrapLong(value);
        }
    },
    FLOAT(float.class) {
        public @Override <T> T to(Object value) {
            return (T) (Float) Numbers.toFloat(value);
        }
    },
    WRAP_FLOAT(Float.class) {
        public @Override <T> T to(Object value) {
            return (T) Numbers.toWrapFloat(value);
        }
    },
    DOUBLE(double.class) {
        public @Override <T> T to(Object value) {
            return (T) (Double) Numbers.toDouble(value);
        }
    },
    WRAP_DOUBLE(Double.class) {
        public @Override <T> T to(Object value) {
            return (T) Numbers.toWrapDouble(value);
        }
    };

    Convertors(Class<?> targetType) {
        Hide.MAPPING.put(targetType, this);
    }

    public abstract <T> T to(Object value);

    public static Convertors of(Class<?> targetType) {
        return Hide.MAPPING.get(targetType);
    }

    private static class Hide {
        private static final Map<Class<?>, Convertors> MAPPING = new HashMap<>();
    }
}

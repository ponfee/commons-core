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
        @Override
        public Boolean to(Object value) {
            return Numbers.toBoolean(value);
        }
    },

    WRAP_BOOLEAN(Boolean.class) {
        @Override
        public Boolean to(Object value) {
            return Numbers.toWrapBoolean(value);
        }
    },

    BYTE(byte.class) {
        @Override
        public Byte to(Object value) {
            return Numbers.toByte(value);
        }
    },

    WRAP_BYTE(Byte.class) {
        @Override
        public Byte to(Object value) {
            return Numbers.toWrapByte(value);
        }
    },

    SHORT(short.class) {
        @Override
        public Short to(Object value) {
            return Numbers.toShort(value);
        }
    },

    WRAP_SHORT(Short.class) {
        @Override
        public Short to(Object value) {
            return Numbers.toWrapShort(value);
        }
    },

    CHAR(char.class) {
        @Override
        public Character to(Object value) {
            return Numbers.toChar(value);
        }
    },

    WRAP_CHAR(Character.class) {
        @Override
        public Character to(Object value) {
            return Numbers.toWrapChar(value);
        }
    },

    INT(int.class) {
        @Override
        public Integer to(Object value) {
            return Numbers.toInt(value);
        }
    },

    WRAP_INT(Integer.class) {
        @Override
        public Integer to(Object value) {
            return Numbers.toWrapInt(value);
        }
    },

    LONG(long.class) {
        @Override
        public Long to(Object value) {
            return Numbers.toLong(value);
        }
    },

    WRAP_LONG(Long.class) {
        @Override
        public Long to(Object value) {
            return Numbers.toWrapLong(value);
        }
    },

    FLOAT(float.class) {
        @Override
        public Float to(Object value) {
            return Numbers.toFloat(value);
        }
    },

    WRAP_FLOAT(Float.class) {
        @Override
        public Float to(Object value) {
            return Numbers.toWrapFloat(value);
        }
    },

    DOUBLE(double.class) {
        @Override
        public Double to(Object value) {
            return Numbers.toDouble(value);
        }
    },

    WRAP_DOUBLE(Double.class) {
        @Override
        public Double to(Object value) {
            return Numbers.toWrapDouble(value);
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

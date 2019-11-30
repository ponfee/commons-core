/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2019, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package code.ponfee.commons.serial;

import static code.ponfee.commons.serial.WrappedSerializer.BOOL_FALSE_BYTE;
import static code.ponfee.commons.serial.WrappedSerializer.BOOL_TRUE_BYTE;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import code.ponfee.commons.collect.ByteArrayWrapper;
import code.ponfee.commons.util.Bytes;

/**
 * 
 * 
 * @author Ponfee
 */
@SuppressWarnings("unchecked")
public enum Serializers {

    BOOLEAN(boolean.class, Boolean.class) {
        public @Override byte[] toBytes(Object value) {
            return new byte[] { (Boolean) value ? BOOL_TRUE_BYTE : BOOL_FALSE_BYTE };
        }

        public @Override <T> T fromBytes(byte[] value) {
            return (T) (Boolean) (value[0] != BOOL_FALSE_BYTE);
        }
    },
    BYTE(byte.class, Byte.class) {
        public @Override byte[] toBytes(Object value) {
            return new byte[] { (Byte) value };
        }

        public @Override <T> T fromBytes(byte[] value) {
            return (T) (Byte) value[0];
        }
    },
    SHORT(short.class, Short.class) {
        public @Override byte[] toBytes(Object value) {
            return Bytes.toBytes((Short) value);
        }

        public @Override <T> T fromBytes(byte[] value) {
            return (T) (Short) Bytes.toShort(value);
        }
    },
    CHAR(char.class, Character.class) {
        public @Override byte[] toBytes(Object value) {
            return Bytes.toBytes((Character) value);
        }

        public @Override <T> T fromBytes(byte[] value) {
            return (T) (Character) Bytes.toChar(value);
        }
    },
    INT(int.class, Integer.class) {
        public @Override byte[] toBytes(Object value) {
            return Bytes.toBytes((Integer) value);
        }

        public @Override <T> T fromBytes(byte[] value) {
            return (T) (Integer) Bytes.toInt(value);
        }
    },
    LONG(long.class, Long.class) {
        public @Override byte[] toBytes(Object value) {
            return Bytes.toBytes((Long) value);
        }

        public @Override <T> T fromBytes(byte[] value) {
            return (T) (Long) Bytes.toLong(value);
        }
    },
    FLOAT(float.class, Float.class) {
        public @Override byte[] toBytes(Object value) {
            return Bytes.toBytes((Float) value);
        }

        public @Override <T> T fromBytes(byte[] value) {
            return (T) (Float) Bytes.toFloat(value);
        }
    },
    DOUBLE(double.class, Double.class) {
        public @Override byte[] toBytes(Object value) {
            return Bytes.toBytes((Double) value);
        }

        public @Override <T> T fromBytes(byte[] value) {
            return (T) (Double) Bytes.toDouble(value);
        }
    },
    PRIMITIVE_BYTES(byte[].class) {
        public @Override byte[] toBytes(Object value) {
            return (byte[]) value;
        }

        public @Override <T> T fromBytes(byte[] value) {
            return (T) value;
        }
    },
    WRAP_BYTES(Byte[].class) {
        public @Override byte[] toBytes(Object value) {
            return ArrayUtils.toPrimitive((Byte[]) value);
        }

        public @Override <T> T fromBytes(byte[] value) {
            return (T) ArrayUtils.toObject(value);
        }
    },
    STRING(String.class) {
        public @Override byte[] toBytes(Object value) {
            return ((String) value).getBytes(UTF_8);
        }

        public @Override <T> T fromBytes(byte[] value) {
            return (T) new String(value, UTF_8);
        }
    },
    DATE(Date.class) {
        public @Override byte[] toBytes(Object value) {
            return Bytes.toBytes(((Date) value).getTime());
        }

        public @Override <T> T fromBytes(byte[] value) {
            return (T) new Date(Bytes.toLong(value));
        }
    },
    BYTE_ARRAY_WRAP(ByteArrayWrapper.class) {
        public @Override byte[] toBytes(Object value) {
            return ((ByteArrayWrapper) value).getArray();
        }

        public @Override <T> T fromBytes(byte[] value) {
            return (T) ByteArrayWrapper.of(value);
        }
    };

    Serializers(Class<?>... types) {
        for (Class<?> type : types) {
            Hide.MAPPING.put(type, this);
        }
    }

    public abstract byte[] toBytes(Object value);

    public abstract <T> T fromBytes(byte[] value);

    public static Serializers of(Class<?> targetType) {
        return Hide.MAPPING.get(targetType);
    }

    private static class Hide {
        private static final Map<Class<?>, Serializers> MAPPING = new HashMap<>();
    }
}

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
 * Build-in data type Serializer
 * 
 * @author Ponfee
 */
@SuppressWarnings("unchecked")
public enum Serializers {

    BOOLEAN(boolean.class, Boolean.class) {
        @Override
        public byte[] toBytes(Object value) {
            return new byte[] { (Boolean) value ? BOOL_TRUE_BYTE : BOOL_FALSE_BYTE };
        }

        @Override
        public Boolean fromBytes(byte[] value) {
            return value[0] != BOOL_FALSE_BYTE;
        }
    },

    BYTE(byte.class, Byte.class) {
        @Override
        public byte[] toBytes(Object value) {
            return new byte[] { (Byte) value };
        }

        @Override
        public Byte fromBytes(byte[] value) {
            return value[0];
        }
    },

    SHORT(short.class, Short.class) {
        @Override
        public byte[] toBytes(Object value) {
            return Bytes.toBytes((Short) value);
        }

        @Override
        public Short fromBytes(byte[] value) {
            return Bytes.toShort(value);
        }
    },

    CHAR(char.class, Character.class) {
        @Override
        public byte[] toBytes(Object value) {
            return Bytes.toBytes((Character) value);
        }

        @Override
        public Character fromBytes(byte[] value) {
            return Bytes.toChar(value);
        }
    },

    INT(int.class, Integer.class) {
        @Override
        public byte[] toBytes(Object value) {
            return Bytes.toBytes((Integer) value);
        }

        @Override
        public Integer fromBytes(byte[] value) {
            return Bytes.toInt(value);
        }
    },

    LONG(long.class, Long.class) {
        @Override
        public byte[] toBytes(Object value) {
            return Bytes.toBytes((Long) value);
        }

        @Override
        public Long fromBytes(byte[] value) {
            return Bytes.toLong(value);
        }
    },

    FLOAT(float.class, Float.class) {
        @Override
        public byte[] toBytes(Object value) {
            return Bytes.toBytes((Float) value);
        }

        @Override
        public Float fromBytes(byte[] value) {
            return Bytes.toFloat(value);
        }
    },

    DOUBLE(double.class, Double.class) {
        @Override
        public byte[] toBytes(Object value) {
            return Bytes.toBytes((Double) value);
        }

        @Override
        public Double fromBytes(byte[] value) {
            return Bytes.toDouble(value);
        }
    },

    PRIMITIVE_BYTES(byte[].class) {
        @Override
        public byte[] toBytes(Object value) {
            return (byte[]) value;
        }

        @Override
        public byte[] fromBytes(byte[] value) {
            return value;
        }
    },

    WRAP_BYTES(Byte[].class) {
        @Override
        public byte[] toBytes(Object value) {
            return ArrayUtils.toPrimitive((Byte[]) value);
        }

        @Override
        public Byte[] fromBytes(byte[] value) {
            return ArrayUtils.toObject(value);
        }
    },

    STRING(String.class) {
        @Override
        public byte[] toBytes(Object value) {
            return ((String) value).getBytes(UTF_8);
        }

        @Override
        public String fromBytes(byte[] value) {
            return new String(value, UTF_8);
        }
    },

    DATE(Date.class) {
        @Override
        public byte[] toBytes(Object value) {
            return Bytes.toBytes(((Date) value).getTime());
        }

        @Override
        public Date fromBytes(byte[] value) {
            return new Date(Bytes.toLong(value));
        }
    },

    BYTE_ARRAY_WRAP(ByteArrayWrapper.class) {
        @Override
        public byte[] toBytes(Object value) {
            return ((ByteArrayWrapper) value).getArray();
        }

        @Override
        public ByteArrayWrapper fromBytes(byte[] value) {
            return ByteArrayWrapper.of(value);
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

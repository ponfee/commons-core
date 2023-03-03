/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.serial;

import cn.ponfee.commons.base.Symbol;
import cn.ponfee.commons.collect.ByteArrayTrait;
import cn.ponfee.commons.collect.ByteArrayWrapper;
import cn.ponfee.commons.io.GzipProcessor;
import cn.ponfee.commons.math.Numbers;
import cn.ponfee.commons.reflect.ClassUtils;
import cn.ponfee.commons.util.Bytes;
import com.google.common.collect.ImmutableMap.Builder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.EnumUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Wrapped other Serializer
 *
 * @author Ponfee
 */
public class WrappedSerializer extends Serializer {

    public static final WrappedSerializer WRAPPED_KRYO_SERIALIZER = new WrappedSerializer(KryoSerializer.INSTANCE);

    public static final WrappedSerializer WRAPPED_TOSTRING_SERIALIZER = new WrappedSerializer(new ToStringSerializer());

    public static final byte BOOL_TRUE_BYTE = (byte) 0xFF;
    public static final byte BOOL_FALSE_BYTE = 0x00;

    private static final Map<Class<?>, Object> PRIMITIVES = new Builder<Class<?>, Object>()
        .put(boolean.class, Boolean.FALSE)
        .put(byte.class,    Numbers.ZERO_BYTE)
        .put(short.class,  (short) 0)
        .put(char.class,   Symbol.Char.ZERO)
        .put(int.class,    Numbers.ZERO_INT)
        .put(long.class,   0L)
        .put(float.class,  0.0F)
        .put(double.class, 0.0D)
        .build();

    private final Serializer wrapper;

    public WrappedSerializer(Serializer wrapped) {
        this.wrapper = wrapped;
    }

    @Override
    protected byte[] serialize0(Object obj, boolean compress) {
        byte[] bytes = serialize0(obj);
        return compress ? GzipProcessor.compress(bytes) : bytes;
    }

    @Override
    protected <T> T deserialize0(byte[] bytes, Class<T> type, boolean compress) {
        if (compress) {
            bytes = GzipProcessor.decompress(bytes);
        }
        return deserialize0(bytes, type);
    }

    // ---------------------------------------------------------------------------primitive&wrapper type
    public byte[] serialize(boolean value) {
        return new byte[]{value ? BOOL_TRUE_BYTE : BOOL_FALSE_BYTE};
    }

    public byte[] serialize(byte value) {
        return new byte[]{value};
    }

    public byte[] serialize(short value) {
        return Bytes.toBytes(value);
    }

    public byte[] serialize(char value) {
        return Bytes.toBytes(value);
    }

    public byte[] serialize(int value) {
        return Bytes.toBytes(value);
    }

    public byte[] serialize(long value) {
        return Bytes.toBytes(value);
    }

    public byte[] serialize(float value) {
        return Bytes.toBytes(value);
    }

    public byte[] serialize(double value) {
        return Bytes.toBytes(value);
    }

    public byte[] serialize(Boolean value) {
        return value == null ? null : serialize((boolean) value);
    }

    public byte[] serialize(Byte value) {
        return value == null ? null : serialize((byte) value);
    }

    public byte[] serialize(Short value) {
        return value == null ? null : serialize((short) value);
    }

    public byte[] serialize(Character value) {
        return value == null ? null : serialize((char) value);
    }

    public byte[] serialize(Integer value) {
        return value == null ? null : serialize((int) value);
    }

    public byte[] serialize(Long value) {
        return value == null ? null : serialize((long) value);
    }

    public byte[] serialize(Float value) {
        return value == null ? null : serialize((float) value);
    }

    public byte[] serialize(Double value) {
        return value == null ? null : serialize((double) value);
    }

    // ---------------------------------------------------------------------------other type
    public byte[] serialize(byte[] value) {
        return value;
    }

    public byte[] serialize(Byte[] value) {
        return value == null ? null : ArrayUtils.toPrimitive(value);
    }

    public byte[] serialize(Date value) {
        return value == null ? null : Bytes.toBytes(value.getTime());
    }

    public byte[] serialize(ByteArrayWrapper value) {
        return value == null ? null : value.getArray();
    }

    public byte[] serialize(ByteArrayTrait value) {
        return value == null ? null : value.toByteArray();
    }

    public byte[] serialize(CharSequence value) {
        return value == null ? null : Serializers.STRING.toBytes(value.toString());
    }

    public byte[] serialize(InputStream value) {
        if (value == null) {
            return null;
        }
        try (InputStream input = value) {
            return IOUtils.toByteArray(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] serialize(ByteBuffer value) {
        return value == null ? null : value.array();
    }

    public byte[] serialize(Enum<?> value) {
        // Bytes.toBytes(value.ordinal())
        return value == null ? null : Serializers.STRING.toBytes(value.name());
    }

    // ---------------------------------------------------------------------------private methods
    /**
     * Returns the serialize byte array data for value
     *
     * @param value the value
     * @return a byte array
     */
    private byte[] serialize0(Object value) {
        if (value == null) {
            return null;
        }

        Serializers serializer = Serializers.of(value.getClass());
        if (serializer != null) {
            return serializer.toBytes(value);
        }

        if (value instanceof CharSequence) {
            return Serializers.STRING.toBytes(value.toString());
        } else if (value instanceof InputStream) {
            return serialize((InputStream) value);
        } else if (value instanceof ByteArrayTrait) {
            return ((ByteArrayTrait) value).toByteArray();
        } else if (value instanceof ByteBuffer) {
            return ((ByteBuffer) value).array();
        } else if (value instanceof Enum) {
            return serialize((Enum<?>) value);
        } else {
            return wrapper.serialize(value);
        }
    }

    /**
     * Returns a object or primitive value from
     * deserialize the byte array
     *
     * @param value the byte array
     * @param type  the target type
     * @return a spec type object
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T> T deserialize0(byte[] value, Class<T> type) {
        if (ArrayUtils.isEmpty(value) && type.isPrimitive()) {
            return (T) PRIMITIVES.get(type); // primitive type use default value
        }

        if (value == null) {
            return null;
        }

        Serializers serializer = Serializers.of(type);
        if (serializer != null) {
            return serializer.fromBytes(value);
        }

        if (CharSequence.class.isAssignableFrom(type)) {
            return ClassUtils.newInstance(type, new Class<?>[]{String.class}, new Object[]{Serializers.STRING.fromBytes(value)});
        } else if (InputStream.class.isAssignableFrom(type)) {
            return (T) new ByteArrayInputStream(value);
        } else if (ByteArrayTrait.class.isAssignableFrom(type)) {
            return (T) ByteArrayTraitSerializer.ofBytes(value, (Class<? extends ByteArrayTrait>) type);
        } else if (ByteBuffer.class.isAssignableFrom(type)) {
            return (T) ByteBuffer.wrap(value);
        } else if (type.isEnum()) {
            //return type.getEnumConstants()[Bytes.toInt(value)];
            return (T) EnumUtils.getEnumIgnoreCase((Class<Enum>) type, Serializers.STRING.fromBytes(value));
        } else {
            return wrapper.deserialize(value, type);
        }
    }

    // ---------------------------------------------------------------------------private class
    @SuppressWarnings("unchecked")
    private enum Serializers {

        BOOLEAN(boolean.class, Boolean.class) {
            @Override
            byte[] to(Object value) {
                return new byte[]{(boolean) value ? BOOL_TRUE_BYTE : BOOL_FALSE_BYTE};
            }

            @Override
            Boolean from(byte[] value) {
                return value[0] != BOOL_FALSE_BYTE;
            }
        },

        BYTE(byte.class, Byte.class) {
            @Override
            byte[] to(Object value) {
                return new byte[]{(byte) value};
            }

            @Override
            Byte from(byte[] value) {
                return value[0];
            }
        },

        SHORT(short.class, Short.class) {
            @Override
            byte[] to(Object value) {
                return Bytes.toBytes((short) value);
            }

            @Override
            Short from(byte[] value) {
                return Bytes.toShort(value);
            }
        },

        CHAR(char.class, Character.class) {
            @Override
            byte[] to(Object value) {
                return Bytes.toBytes((char) value);
            }

            @Override
            Character from(byte[] value) {
                return Bytes.toChar(value);
            }
        },

        INT(int.class, Integer.class) {
            @Override
            byte[] to(Object value) {
                return Bytes.toBytes((int) value);
            }

            @Override
            Integer from(byte[] value) {
                return Bytes.toInt(value);
            }
        },

        LONG(long.class, Long.class) {
            @Override
            byte[] to(Object value) {
                return Bytes.toBytes((long) value);
            }

            @Override
            Long from(byte[] value) {
                return Bytes.toLong(value);
            }
        },

        FLOAT(float.class, Float.class) {
            @Override
            byte[] to(Object value) {
                return Bytes.toBytes((float) value);
            }

            @Override
            Float from(byte[] value) {
                return Bytes.toFloat(value);
            }
        },

        DOUBLE(double.class, Double.class) {
            @Override
            byte[] to(Object value) {
                return Bytes.toBytes((double) value);
            }

            @Override
            Double from(byte[] value) {
                return Bytes.toDouble(value);
            }
        },

        PRIMITIVE_BYTES(byte[].class) {
            @Override
            byte[] to(Object value) {
                return (byte[]) value;
            }

            @Override
            byte[] from(byte[] value) {
                return value;
            }
        },

        WRAP_BYTES(Byte[].class) {
            @Override
            byte[] to(Object value) {
                return ArrayUtils.toPrimitive((Byte[]) value);
            }

            @Override
            Byte[] from(byte[] value) {
                return ArrayUtils.toObject(value);
            }
        },

        STRING(String.class) {
            @Override
            byte[] to(Object value) {
                return ((String) value).getBytes(UTF_8);
            }

            @Override
            String from(byte[] value) {
                return new String(value, UTF_8);
            }
        },

        DATE(Date.class) {
            @Override
            byte[] to(Object value) {
                return Bytes.toBytes(((Date) value).getTime());
            }

            @Override
            Date from(byte[] value) {
                return new Date(Bytes.toLong(value));
            }
        },

        BYTE_ARRAY_WRAP(ByteArrayWrapper.class) {
            @Override
            byte[] to(Object value) {
                return ((ByteArrayWrapper) value).getArray();
            }

            @Override
            ByteArrayWrapper from(byte[] value) {
                return ByteArrayWrapper.of(value);
            }
        };

        Serializers(Class<?>... types) {
            for (Class<?> type : types) {
                Hide.MAPPING.put(type, this);
            }
        }

        final byte[] toBytes(Object value) {
            return value == null ? null : to(value);
        }

        final <T> T fromBytes(byte[] value) {
            return value == null ? null : from(value);
        }

        abstract byte[] to(Object value);

        abstract <T> T from(byte[] value);

        static Serializers of(Class<?> targetType) {
            return Hide.MAPPING.get(targetType);
        }
    }

    private static class Hide {
        private static final Map<Class<?>, Serializers> MAPPING = new HashMap<>();
    }
}

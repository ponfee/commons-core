package code.ponfee.commons.serial;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;

import com.google.common.collect.ImmutableMap.Builder;

import code.ponfee.commons.collect.ByteArrayTrait;
import code.ponfee.commons.collect.ByteArrayWrapper;
import code.ponfee.commons.io.GzipProcessor;
import code.ponfee.commons.math.Numbers;
import code.ponfee.commons.reflect.ClassUtils;
import code.ponfee.commons.util.Bytes;
import code.ponfee.commons.util.Enums;

/**
 * 
 * Wrapped other Serializer
 * 
 * @author Ponfee
 */
public class WrappedSerializer extends Serializer {

    public static final WrappedSerializer WRAPPED_KRYO_SERIALIZER =
        new WrappedSerializer(KryoSerializer.INSTANCE);

    public static final WrappedSerializer WRAPPED_TOSTRING_SERIALIZER =
        new WrappedSerializer(new ToStringSerializer());

    public static final byte BOOL_TRUE_BYTE = (byte) 0xFF;
    public static final byte BOOL_FALSE_BYTE = 0x00;

    private static final Map<Class<?>, Object> PRIMITIVES = new Builder<Class<?>, Object>()
        .put(boolean.class, Boolean.FALSE)
        .put(byte.class, (Byte) (byte) 0)
        .put(short.class, (Short) (short) 0)
        .put(char.class, (Character) Numbers.CHAR_ZERO)
        .put(int.class, (Integer) 0)
        .put(long.class, (Long) 0L)
        .put(float.class, (Float) 0.0F)
        .put(double.class, (Double) 0.0D)
        .build();

    private final Serializer other;

    public WrappedSerializer() {
        this(KryoSerializer.INSTANCE);
    }

    public WrappedSerializer(Serializer other) {
        this.other = other;
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

    public byte[] serialize(boolean value) {
        return new byte[] { value ? BOOL_TRUE_BYTE : BOOL_FALSE_BYTE };
    }

    public byte[] serialize(byte value) {
        return new byte[] { value };
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
        return Serializers.BOOLEAN.toBytes(value);
    }

    public byte[] serialize(Byte value) {
        return Serializers.BYTE.toBytes(value);
    }

    public byte[] serialize(Short value) {
        return Serializers.SHORT.toBytes(value);
    }

    public byte[] serialize(Character value) {
        return Serializers.CHAR.toBytes(value);
    }

    public byte[] serialize(Integer value) {
        return Serializers.INT.toBytes(value);
    }

    public byte[] serialize(Long value) {
        return Serializers.LONG.toBytes(value);
    }

    public byte[] serialize(Float value) {
        return Serializers.FLOAT.toBytes(value);
    }

    public byte[] serialize(Double value) {
        return Serializers.DOUBLE.toBytes(value);
    }

    public byte[] serialize(byte[] value) {
        return Serializers.PRIMITIVE_BYTES.toBytes(value);
    }

    public byte[] serialize(Byte[] value) {
        return Serializers.WRAP_BYTES.toBytes(value);
    }

    public byte[] serialize(Date value) {
        return Serializers.DATE.toBytes(value);
    }

    public byte[] serialize(ByteArrayWrapper value) {
        return Serializers.BYTE_ARRAY_WRAP.toBytes(value);
    }

    public byte[] serialize(CharSequence value) {
        if (value == null) {
            return null;
        }
        return Serializers.STRING.toBytes(value.toString());
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

    public byte[] serialize(ByteArrayTrait value) {
        if (value == null) {
            return null;
        }
        return value.toByteArray();
    }

    public byte[] serialize(ByteBuffer value) {
        if (value == null) {
            return null;
        }
        return value.array();
    }

    public byte[] serialize(Enum<?> value) {
        if (value == null) {
            return null;
        }
        //return Bytes.toBytes(value.ordinal());
        return Serializers.STRING.toBytes(((Enum<?>) value).name());
    }

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
            // return serialize(((Enum<?>) value).ordinal());
            return serialize(((Enum<?>) value).name());
        } else {
            return other.serialize(value);
        }
    }

    /**
     * Returns a object or primitive value from 
     * deserialize the byte array
     * 
     * @param value the byte array
     * @param type the obj type
     * @return a object
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
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
            return ClassUtils.newInstance(type, String.class, Serializers.STRING.fromBytes(value));
        } else if (InputStream.class.isAssignableFrom(type)) {
            return (T) new ByteArrayInputStream(value);
        } else if (ByteArrayTrait.class.isAssignableFrom(type)) {
            return (T) ByteArrayTraitSerializer.ofBytes(value, (Class<? extends ByteArrayTrait>) type);
        } else if (ByteBuffer.class.isAssignableFrom(type)) {
            return (T) ByteBuffer.wrap(value);
        } else if (type.isEnum()) {
            //return type.getEnumConstants()[Bytes.toInt(value)];
            return (T) Enums.ofIgnoreCase((Class<Enum>) type, Serializers.STRING.fromBytes(value));
        } else {
            return other.deserialize(value, type);
        }
    }

}

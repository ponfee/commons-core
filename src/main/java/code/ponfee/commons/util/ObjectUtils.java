package code.ponfee.commons.util;

import code.ponfee.commons.math.Numbers;
import code.ponfee.commons.reflect.ClassUtils;
import code.ponfee.commons.reflect.Fields;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

/**
 * 公用对象工具类
 * @author Ponfee
 */
public final class ObjectUtils {

    private static final char[] URL_SAFE_BASE64_CODES = 
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_".toCharArray();

    /**
     * Returns object toString
     * 
     * @param obj the target object
     * @return the string of object
     */
    public static String toString(Object obj) {
        return toString(obj, "null");
    }

    public static String toString(Object obj, String defaultStr) {
        return (obj == null) 
               ? defaultStr 
               : reflectionToString(obj, ToStringStyle.JSON_STYLE);
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> typeOf(T obj) {
        return obj != null ? (Class<T>) obj.getClass() : null;
    }

    public static <T> Predicate<T> not(Predicate<T> target) {
        return target.negate();
    }

    /**
     * 判断对象是否为空
     * 
     * @param o the object
     * @return {@code true} is empty
     */
    public static boolean isEmpty(Object o) {
        if (o == null) {
            return true;
        } else if (o instanceof CharSequence) {
            return ((CharSequence) o).length() == 0;
        } else if (o instanceof Collection) {
            return ((Collection<?>) o).isEmpty();
        } else if (o.getClass().isArray()) {
            return Array.getLength(o) == 0;
        } else if (o instanceof Map) {
            return ((Map<?, ?>) o).isEmpty();
        } else if (o instanceof Dictionary) {
            return ((Dictionary<?, ?>) o).isEmpty();
        } else {
            return false;
        }
    }

    /**
     * Gets the target's name value
     * 
     * @param obj  the object
     * @param name the field name
     * @return a value
     */
    public static Object getValue(Object obj, String name) {
        if (obj == null) {
            return null;
        } else if (obj instanceof Map) {
            return ((Map<?, ?>) obj).get(name);
        } else if (obj instanceof Dictionary) {
            return ((Dictionary<?, ?>) obj).get(name);
        } else {
            return Fields.get(obj, name);
        }
    }

    /**
     * Returns a object that convert spec value to type
     * 
     * @param value source object
     * @param type  target object type
     * @return a this type object
     *
     * @see com.alibaba.fastjson.util.TypeUtils
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> T convert(Object value, Class<T> type) {
        if (type.isInstance(value)) {
            return (T) value;
        }

        PrimitiveOrWrapperConvertors convertor = PrimitiveOrWrapperConvertors.of(type);
        if (convertor != null) {
            return convertor.to(value);
        }

        if (value == null) {
            return null;
        }

        if (type.isEnum()) {
            return (value instanceof Number)
                 ? type.getEnumConstants()[((Number) value).intValue()]
                 : (T) EnumUtils.getEnumIgnoreCase((Class<Enum>) type, value.toString());
        }

        if (Date.class == type) {
            if (value instanceof Number) {
                return (T) new Date(((Number) value).longValue());
            }
            String text = value.toString();
            if (StringUtils.isNumeric(text) && !RegexUtils.isDatePattern(text)) {
                return (T) new Date(Numbers.toLong(text));
            }
            try {
                return (T) WrappedFastDateFormat.DEFAULT.parse(text);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        return ClassUtils.newInstance(type, value.toString());
    }

    /**
     * uuid byte array
     * @return
     */
    public static byte[] uuid() {
        UUID uuid = UUID.randomUUID();
        long most  = uuid.getMostSignificantBits(), 
             least = uuid.getLeastSignificantBits();
       return new byte[] {
           (byte) (most  >>> 56), (byte) (most  >>> 48),
           (byte) (most  >>> 40), (byte) (most  >>> 32),
           (byte) (most  >>> 24), (byte) (most  >>> 16),
           (byte) (most  >>>  8), (byte) (most        ),

           (byte) (least >>> 56), (byte) (least >>> 48),
           (byte) (least >>> 40), (byte) (least >>> 32),
           (byte) (least >>> 24), (byte) (least >>> 16),
           (byte) (least >>>  8), (byte) (least       )
       };
    }

    /**
     * uuid 32 string
     * @return
     */
    public static String uuid32() {
        //return Bytes.hexEncode(uuid());
        UUID uuid = UUID.randomUUID();
        return Long.toHexString(uuid.getMostSignificantBits()) + Long.toHexString(uuid.getLeastSignificantBits());
    }

    /**
     * 22位uuid
     * @return
     */
    public static String uuid22() {
        return Base64UrlSafe.encode(uuid());
    }

    /**
     * short uuid
     * @param len
     * @return
     */
    public static String shortid(int len) {
        return shortid(len, URL_SAFE_BASE64_CODES);
    }

    /**
     * short uuid
     * should between 3 (inclusive) and 32 (exclusive)
     * 
     * @param len
     * @param chars
     * @return
     */
    public static String shortid(int len, char[] chars) {
        int size = chars.length;
        StringBuilder builder = new StringBuilder(len);
        for (String str : Strings.slice(uuid32(), len)) { // 分成len段
            if (StringUtils.isNotEmpty(str)) {
                builder.append(chars[(int) (Long.parseLong(str, 16) % size)]);
            }
        }
        return builder.toString();
    }

    /**
     * 获取堆栈信息
     * 
     * @param deepPath
     * @return
     */
    public static String getStackTrace(int deepPath) {
        StackTraceElement[] traces = Thread.currentThread().getStackTrace();
        if (traces.length <= deepPath) {
            return "warning: out of stack trace.";
        }

        StackTraceElement trace = traces[deepPath];
        return new StringBuilder()
                      .append(trace.getLineNumber()).append(":")
                      .append(trace.getClassName())
                      .append("#").append(trace.getMethodName())
                      .toString();
    }

    /**
     * Copies source fields value to target fields
     * 
     * @param source the source
     * @param target the target
     * @param fields the fields of String array
     */
    public static <T> void copy(T source, T target, String... fields) {
        Preconditions.checkState(ArrayUtils.isNotEmpty(fields));
        for (String field : fields) {
            Fields.put(target, field, Fields.get(source, field));
        }
    }

    /**
     * Returns a new instance of copy from spec fields
     * 
     * @param source the source
     * @param fields the fields
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T copyFrom(T source, String... fields) {
        Preconditions.checkState(ArrayUtils.isNotEmpty(fields));
        T target = (T) newInstance(source.getClass());
        copy(source, target, fields);
        return target;
    }

    /**
     * Returns a new instance of type
     * 
     * @param type the type class
     * @return a new instance
     */
    @SuppressWarnings("unchecked")
    public static <T> T newInstance(Class<T> type) {
        if (Map.class == type) { // interface
            return (T) new HashMap<>();
        } else if (Set.class == type) { // interface
            return (T) new HashSet<>();
        } else if (Collection.class == type || List.class == type) { // interface
            return (T) new ArrayList<>();
        } else if (Dictionary.class == type) { // abstract
            return (T) new Hashtable<>();
        } else if (type.isPrimitive()) {
            Class<?> wrapper = org.apache.commons.lang3.ClassUtils.primitiveToWrapper(type);
            return (T) ClassUtils.newInstance(wrapper, String.class, "0"); // Boolean: false
        } else if (org.apache.commons.lang3.ClassUtils.isPrimitiveWrapper(type)) {
            return ClassUtils.newInstance(type, String.class, "0");
        } else {
            return ClassUtils.newInstance(type);
        }
    }

    /**
     * Returns the type is not a bean type
     * 
     * @param type the type class
     * @return {@code true} assert is not a bean type
     */
    public static boolean isNotBeanType(Class<?> type) {
        return null == type
            || Object.class == type
            || org.apache.commons.lang3.ClassUtils.isPrimitiveOrWrapper(type)
            || CharSequence.class.isAssignableFrom(type)
            || Map.class.isAssignableFrom(type)
            || Dictionary.class.isAssignableFrom(type)
            || Collection.class.isAssignableFrom(type);
    }

    // -------------------------------------------------------------------------- private class
    private enum PrimitiveOrWrapperConvertors {

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

        PrimitiveOrWrapperConvertors(Class<?> targetType) {
            Hide.MAPPING.put(targetType, this);
        }

        abstract <T> T to(Object value);

        static PrimitiveOrWrapperConvertors of(Class<?> targetType) {
            return Hide.MAPPING.get(targetType);
        }
    }

    private static class Hide {
        private static final Map<Class<?>, PrimitiveOrWrapperConvertors> MAPPING = new HashMap<>();
    }
}

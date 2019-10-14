package code.ponfee.commons.util;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.objenesis.ObjenesisHelper;

import com.google.common.base.Preconditions;

import code.ponfee.commons.math.Numbers;
import code.ponfee.commons.reflect.ClassUtils;
import code.ponfee.commons.reflect.Fields;

/**
 * 公用对象工具类
 * @author Ponfee
 */
public final class ObjectUtils {

    private static final char[] URL_SAFE_BASE64_CODES = 
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_".toCharArray();

    private static final String[] DATE_PATTERN = { 
        "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyyMMdd", "yyyyMMddHHmmss", 
        "yyyyMMddHHmmssSSS", "yyyy-MM-dd HH:mm:ss.SSS", "yyyy-MM-dd HH:mm:ss.SSS" 
    };

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
    public static <T> Predicate<T> not(Predicate<? super T> target) {
        Objects.requireNonNull(target);
        return (Predicate<T>) target.negate();
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
        } else if (CharSequence.class.isInstance(o)) {
            return ((CharSequence) o).length() == 0;
        } else if (Collection.class.isInstance(o)) {
            return ((Collection<?>) o).isEmpty();
        } else if (o.getClass().isArray()) {
            return Array.getLength(o) == 0;
        } else if (Map.class.isInstance(o)) {
            return ((Map<?, ?>) o).isEmpty();
        } else if (Dictionary.class.isInstance(o)) {
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
     * @return a object of type 
     * @throws Exception if occur error
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> T convert(Object value, Class<T> type) {
        if (type.isPrimitive()) {
            // 原始类型
            //type = org.apache.commons.lang3.ClassUtils.primitiveToWrapper(type);
            //org.apache.commons.lang3.ClassUtils.isPrimitiveOrWrapper(type)
            if (boolean.class == type) {
                value = Numbers.toBoolean(value);
            } else if (byte.class == type) {
                value = Numbers.toByte(value);
            } else if (short.class == type) {
                value = Numbers.toShort(value);
            } else if (char.class == type) {
                value = Numbers.toChar(value);
            } else if (int.class == type) {
                value = Numbers.toInt(value);
            } else if (long.class == type) {
                value = Numbers.toLong(value);
            } else if (float.class == type) {
                value = Numbers.toFloat(value);
            } else if (double.class == type) {
                value = Numbers.toDouble(value);
            } else {
                // cannot happened
                throw new UnsupportedOperationException("Unknow primitive type: " + type.toString());
            }
        } else if (value != null && !type.isInstance(value)) { // 类型不一致时
            if (   org.apache.commons.lang3.ClassUtils.isPrimitiveWrapper(type)
                && Strings.isEmpty(value)
            ) {
                value = null; // 原始包装类型且value为空或为空字符串则设置为null
            } else if (Boolean.class == type) {
                value = Numbers.toWrapBoolean(value);
            } else if (Byte.class == type) {
                value = Numbers.toWrapByte(value);
            } else if (Short.class == type) {
                value = Numbers.toWrapShort(value);
            } else if (Character.class == type) {
                value = Numbers.toWrapChar(value);
            } else if (Integer.class == type) {
                value = Numbers.toWrapInt(value);
            } else if (Long.class == type) {
                value = Numbers.toWrapLong(value);
            } else if (Float.class == type) {
                value = Numbers.toWrapFloat(value);
            } else if (Double.class == type) {
                value = Numbers.toWrapDouble(value);
            } else if (type.isEnum()) {
                // enum class
                if (value instanceof Number) {
                    value = type.getEnumConstants()[((Number) value).intValue()];
                } else {
                    value = EnumUtils.getEnumIgnoreCase((Class<Enum>) type, value.toString());
                }
            } else if (Date.class == type) {
                if (value instanceof Number) {
                    value = new Date(((Number) value).longValue());
                } else if (value instanceof CharSequence) {
                    String str = value.toString();
                    if (StringUtils.isNumeric(str) && !RegexUtils.isDatePattern(str)) {
                        value = new Date(Numbers.toLong(str));
                    } else {
                        try {
                            value = DateUtils.parseDate(str, DATE_PATTERN);
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                    }
                } else {
                    throw new IllegalArgumentException("Illegal date time: " + value);
                }
            } else {
                Constructor<T> constructor = ClassUtils.getConstructor(type, String.class);
                if (constructor == null) {
                    throw new ClassCastException(
                        ClassUtils.getClassName(value.getClass()) + " cannot be cast to " + ClassUtils.getClassName(type)
                    );
                }
                try {
                    // e.g. new String(value.toString()) or new StringBuilder(value.toString())
                    return constructor.newInstance(value.toString());
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }
        } // else { Nothing to do: value is null or type.isInstance(value) }
        return (T) value;
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
        return Bytes.hexEncode(uuid());
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
            return ObjenesisHelper.newInstance(type);
        }
    }

    /**
     * Returns the type is a bean type
     * 
     * @param type the type class
     * @return a boolean
     */
    public boolean isBean(Class<?> type) {
        if (type == null) {
            return false;
        }
        return !org.apache.commons.lang3.ClassUtils.isPrimitiveOrWrapper(type)
            && !CharSequence.class.isAssignableFrom(type) 
            && !Map.class.isAssignableFrom(type) 
            && !Dictionary.class.isAssignableFrom(type) 
            && !Collection.class.isAssignableFrom(type);
    }

}

package code.ponfee.commons.json;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The json utility based jackson
 * 
 * @author Ponfee
 */
public final class Jsons {

    /** 标准（不排除任何属性） */
    public static final Jsons NORMAL = new Jsons(null);

    /** 忽略对象中值为 null的属性*/
    public static final Jsons NON_NULL = new Jsons(JsonInclude.Include.NON_NULL);

    /** 忽略对象中值为 null或 ""的属性 */
    public static final Jsons NON_EMPTY = new Jsons(JsonInclude.Include.NON_EMPTY);

    /** 忽略对象中值为默认值的属性（慎用） */
    public static final Jsons NON_DEFAULT = new Jsons(JsonInclude.Include.NON_DEFAULT);

    /** Jackson ObjectMapper(thread safe) */
    private final ObjectMapper mapper = new ObjectMapper();

    private Jsons(JsonInclude.Include include) {
        // 设置序列化时的特性
        if (include != null) {
            mapper.setSerializationInclusion(include);
        }

        // 反序列化时忽略不存在于对象中的属性
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        // 反序列化扩展日期格式支持（经测试无效）
        //mapper.setConfig(mapper.getDeserializationConfig().with(mapper.getDateFormat()));

        // 反序列化扩展日期格式支持
        /*SimpleModule module = new SimpleModule();
        module.addDeserializer(java.util.Date.class, new JacksonDateDeserializer("yyyy-MM-dd"));
        mapper.registerModule(module);*/

        /*mapper.enable(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        mapper.enable(com.fasterxml.jackson.core.JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        //mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy(){
            private static final long serialVersionUID = -3401320843245849044L;
            // do-something
        });
        mapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));*/
    }

    /**
     * Converts a object to json, and wite to output stream
     * 
     * @param output the output stream
     * @param target the target object
     * @throws JsonException if occur exception
     */
    public void write(OutputStream output, Object target) throws JsonException {
        try {
            mapper.writeValue(output, target);
        } catch (IOException e) {
            throw new JsonException(e);
        }
    }

    /**
     * Converts an object(POJO, Array, Collection, ...) to json string
     *
     * @param target target object
     * @return json string
     * @throws JsonException   the exception for json
     */
    public String string(Object target) throws JsonException {
        try {
            return mapper.writeValueAsString(target);
        } catch (IOException e) {
            throw new JsonException(e);
        }
    }

    /**
     * Serialize the byte array of json
     *
     * @param target  object
     * @return byte[] array
     * @throws JsonException   the exception for json
     */
    public byte[] bytes(Object target) throws JsonException {
        try {
            return mapper.writeValueAsBytes(target);
        } catch (IOException e) {
            throw new JsonException(e);
        }
    }

    /**
     * Deserialize a json to target class object
     * {@code mapper.readValue(json, new TypeReference<Map<String, Object>>() {})}
     * 
     * @param json json string
     * @param target target class
     * @return target object
     * @throws JsonException   the exception for json
     */
    public <T> T parse(String json, Class<T> target) throws JsonException {
        if (StringUtils.isEmpty(json)) {
            return null;
        }

        try {
            return mapper.readValue(json, target);
        } catch (IOException e) {
            throw new JsonException(e);
        }
    }

    /**
     * Deserialize a json to target class object
     * {@code mapper.readValue(json, new TypeReference<Map<String, Object>>() {})}
     * 
     * @param json the byte array
     * @param target target class
     * @return target object
     * @throws JsonException   the exception for json
     */
    public <T> T parse(byte[] json, Class<T> target) throws JsonException {
        if (json == null || json.length == 0) {
            return null;
        }

        try {
            return mapper.readValue(json, target);
        } catch (IOException e) {
            throw new JsonException(e);
        }
    }

    /**
     * Deserialize the json string to java object
     * {@code new TypeReference<Map<String, Object>>(){} }
     * 
     * @param json the json string
     * @param type the TypeReference specified java type
     * @return a java object
     * @throws JsonException
     */
    public <T> T parse(String json, TypeReference<T> type) throws JsonException {
        if (StringUtils.isEmpty(json)) {
            return null;
        }

        try {
            return mapper.readValue(json, type);
        } catch (Exception e) {
            throw new JsonException(e);
        }
    }

    /**
     * Deserialize the json string to java object
     * {@code new TypeReference<Map<String, Object>>(){} }
     * 
     * fast json: JSON.parseObject(json, new TypeReference<Map<String,String>>(){})
     * 
     * @param json the json byte array
     * @param type the TypeReference specified java type
     * @return a java object
     * @throws JsonException
     */
    public <T> T parse(byte[] json, TypeReference<T> type) throws JsonException {
        if (json == null || json.length == 0) {
            return null;
        }

        try {
            return mapper.readValue(json, type);
        } catch (Exception e) {
            throw new JsonException(e);
        }
    }

    /**
     * Deserialize the json string, specified collections class and element class
     *
     * eg: parse(json, Map.class, String.class, Object.class);
     *
     * @param json          the json string
     * @param collectClass  the collection class type
     * @param elemClasses   the element class type
     * @return the objects of collection
     * @throws JsonException the exception for json
     */
    public <T> T parse(String json, Class<T> collectClass,
                       Class<?>... elemClasses) throws JsonException {
        return parse(json, createCollectionType(collectClass, elemClasses));
    }

    /**
     * Deserialize the json string to java object
     * 
     * @param json json string
     * @param javaType JavaType
     * @return the javaType's object
     * @throws JsonException the exception for json
     *
     * @see #createCollectionType(Class, Class...)
     */
    public <T> T parse(String json, JavaType javaType) throws JsonException {
        if (StringUtils.isEmpty(json)) {
            return null;
        }

        try {
            return mapper.readValue(json, javaType);
        } catch (Exception e) {
            throw new JsonException(e);
        }
    }

    /**
     * Constructs collection type
     *
     * @param collecClass collection class, such as ArrayList, HashMap, ...
     * @param elemClasses element class
     * @return a JavaType instance
     */
    public <T> JavaType createCollectionType(Class<T> collecClass, 
                                             Class<?>... elemClasses) {
        return mapper.getTypeFactory()
                     .constructParametricType(collecClass, elemClasses);
    }

    // ----------------------------------------------------static methods
    public static String toJson(Object target) {
        return NORMAL.string(target);
    }

    public static byte[] toBytes(Object target) {
        return NORMAL.bytes(target);
    }

    public static <T> T fromJson(String json, Class<T> target) {
        return NORMAL.parse(json, target);
    }

    public static <T> T fromJson(byte[] json, Class<T> target) {
        return NORMAL.parse(json, target);
    }

    public static <T> T fromJson(String json, TypeReference<T> type) {
        return NORMAL.parse(json, type);
    }

    public static <T> T fromJson(byte[] json, TypeReference<T> type) {
        return NORMAL.parse(json, type);
    }

    public static <T> T fromJson(String json, JavaType javaType) {
        return NORMAL.parse(json, javaType);
    }

    public static <T> T fromJson(String json, Class<T> collectClass, 
                                 Class<?>... elemClasses) {
        return NORMAL.parse(json, collectClass, elemClasses);
    }

}

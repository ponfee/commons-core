package code.ponfee.commons.json;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.ArrayUtils;

import com.alibaba.fastjson.serializer.PropertyFilter;

/**
 * Object to json specified fields whether includes or excludes
 * 
 * {@code
 *   Map<String, Object> map = ImmutableMap.of("a", 1, "b", true, "c", "x");
 *   JSONObject.toJSONString(map, new JsonPropertyFilter(FilterType.INCLUDES, "a", "b"))
 *   JSONObject.toJSONString(map, new JsonPropertyFilter(FilterType.EXCLUDES, "a", "b"))
 *   
 *   Or 
 *   
 *   JSONObject.toJSONString(map, new SimplePropertyPreFilter("a", "b"))
 * }
 * 
 * @author Ponfee
 */
public class FastjsonPropertyFilter implements PropertyFilter {

    private final boolean isIncludes;
    private final boolean forceNonNull;
    private final String[] fields;

    public FastjsonPropertyFilter(@Nonnull PropertyFilterType type, @Nonnull String... fields) {
        this(type, false, fields);
    }

    public FastjsonPropertyFilter(@Nonnull PropertyFilterType type, boolean forceNonNull, @Nonnull String... fields) {
        this.isIncludes = type == PropertyFilterType.INCLUDES;
        this.forceNonNull = forceNonNull;
        this.fields = fields;
    }

    @Override
    public boolean apply(Object source, String name, Object value) {
        if (forceNonNull && value != null) {
            return true;
        }

        // 异或：(A ^ B)；同或：(A ^ B) ^ 1
        //return isIncludes ^ ArrayUtils.contains(fields, name) ^ true;
        //return !(isIncludes ^ ArrayUtils.contains(fields, name));
        return isIncludes == ArrayUtils.contains(fields, name);
    }

}

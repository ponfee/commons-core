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
    private final String[] fields;

    public FastjsonPropertyFilter(@Nonnull FilterType type, @Nonnull String... fields) {
        this.isIncludes = type == FilterType.INCLUDES;
        this.fields = fields;
    }

    @Override
    public boolean apply(Object source, String name, Object value) {
        // 异或：(A ^ B)；同或：(A ^ B) ^ 1
        //return isIncludes ^ ArrayUtils.contains(fields, name) ^ true;
        return !(isIncludes ^ ArrayUtils.contains(fields, name));
    }

    public static enum FilterType {
        INCLUDES, EXCLUDES
    }

}

package code.ponfee.commons.json;

import com.alibaba.fastjson.serializer.PropertyFilter;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;

/**
 * <pre>
 * Object to json specified fields whether includes or excludes
 * 
 * {@code
 *   Map<String, Object> map = ImmutableMap.of("a", 1, "b", true, "c", "x");
 *   JSON.toJSONString(map, JsonPropertyFilter.include("a", "b"))
 *   JSON.toJSONString(map, JsonPropertyFilter.exclude("a", "b"))
 *   
 *   OR 
 *   
 *   JSON.toJSONString(map, new SimplePropertyPreFilter("a", "b"))
 * }
 * </pre>
 *
 * @author Ponfee
 */
public class FastjsonPropertyFilter implements PropertyFilter {

    private final boolean isIncludes;
    private final boolean forceNonNull; // if true, then non null field will be serialize
    private final String[] fields;

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

        // 异或：(A ^ B)
        // 同或：(A ^ B ^ 1) or !(A ^ B)
        //return isIncludes ^ ArrayUtils.contains(fields, name) ^ true;
        //return !(isIncludes ^ ArrayUtils.contains(fields, name));
        return isIncludes == ArrayUtils.contains(fields, name);
    }

    // ----------------------------------------------------------------------static methods
    public static FastjsonPropertyFilter exclude(@Nonnull String... fields) {
        return exclude(false, fields);
    }

    public static FastjsonPropertyFilter exclude(boolean forceNonNull, @Nonnull String... fields) {
        return new FastjsonPropertyFilter(PropertyFilterType.EXCLUDES, forceNonNull, fields);
    }

    public static FastjsonPropertyFilter include(@Nonnull String... fields) {
        return include(false, fields);
    }

    public static FastjsonPropertyFilter include(boolean forceNonNull, @Nonnull String... fields) {
        return new FastjsonPropertyFilter(PropertyFilterType.INCLUDES, forceNonNull, fields);
    }

    private enum PropertyFilterType {
        INCLUDES, EXCLUDES
    }

}

package code.ponfee.commons.json;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * The TypeReference holder
 *  
 * @author Ponfee
 */
public final class TypeReferences {
    private TypeReferences() {}

    public static final TypeReference<Map<String, Object>> MAP_NORMAL = new TypeReference<Map<String, Object>>() {};
    public static final TypeReference<Map<String, String>> MAP_STRING = new TypeReference<Map<String, String>>() {};

    public static final TypeReference<List<Object>> LIST_OBJECT = new TypeReference<List<Object>>() {};
    public static final TypeReference<List<String>> LIST_STRING = new TypeReference<List<String>>() {};

    public static final TypeReference<Set<Object>> SET_OBJECT = new TypeReference<Set<Object>>() {};
    public static final TypeReference<Set<String>> SET_STRING = new TypeReference<Set<String>>() {};
}

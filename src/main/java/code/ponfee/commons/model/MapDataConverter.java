package code.ponfee.commons.model;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import code.ponfee.commons.reflect.Fields;

/**
 * Converts model object to map
 * 
 * @param <S> source
 * 
 * @author Ponfee
 */
public class MapDataConverter<S> extends AbstractDataConverter<S, Map<String, Object>> {

    private final String[] keys;

    public MapDataConverter(String... keys) {
        this.keys = keys;
    }

    @Override
    public Map<String, Object> convert(S source) {
        return convert(source, keys);
    }

    @SuppressWarnings("unchecked")
    public static <S> Map<String, Object> convert(S source, String... keys) {
        Function<? super String, ?> vm;
        if (source instanceof Map) {
            vm = ((Map<String, Object>) source)::get;
        } else if (source instanceof Dictionary) {
            vm = ((Dictionary<String, Object>) source)::get;
        } else {
            vm = k -> Fields.get(source, k);
        }
        return Arrays.stream(keys).collect(Collectors.toMap(Function.identity(), vm));
    }

}

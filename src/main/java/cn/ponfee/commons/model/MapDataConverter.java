/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.model;

import cn.ponfee.commons.reflect.Fields;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Converts model object to map, specified source fields
 * 
 * @param <S> source
 * 
 * @author Ponfee
 */
public class MapDataConverter<S> extends AbstractDataConverter<S, Map<String, Object>> {

    private final String[] fields;

    public MapDataConverter(String... fields) {
        this.fields = fields;
    }

    @Override
    public Map<String, Object> convert(S source) {
        return convert(source, fields);
    }

    @SuppressWarnings("unchecked")
    public static <S> Map<String, Object> convert(S source, String... fields) {
        Function<? super String, ?> vm;
        if (source instanceof Map) {
            vm = ((Map<String, Object>) source)::get;
        } else if (source instanceof Dictionary) {
            vm = ((Dictionary<String, Object>) source)::get;
        } else {
            vm = field -> Fields.get(source, field);
        }
        return Arrays.stream(fields).collect(Collectors.toMap(Function.identity(), vm));
    }

    public static <S> List<Map<String, Object>> convert(List<S> list, String... fields) {
        if (list == null) {
            return null;
        }
        return list.stream().map(e -> convert(e, fields)).collect(Collectors.toList());
    }

    public static <S> Page<Map<String, Object>> convert(Page<S> page, String... fields) {
        if (page == null) {
            return null;
        }
        return page.map(x -> convert(x, fields));
    }

    public static <S> Result<Map<String, Object>> convertResultBean(Result<S> result, String... fields) {
        if (result == null) {
            return null;
        }
        return result.from(convert(result.getData(), fields));
    }

    public static <S> Result<List<Map<String, Object>>> convertResultList(Result<List<S>> result, String... fields) {
        if (result == null) {
            return null;
        }
        return result.from(convert(result.getData(), fields));
    }

    public static <S> Result<Page<Map<String, Object>>> convertResultPage(Result<Page<S>> result, String... fields) {
        if (result == null) {
            return null;
        }
        return result.from(convert(result.getData(), fields));
    }

}

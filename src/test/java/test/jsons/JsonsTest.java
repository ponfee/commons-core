package test.jsons;

import java.util.Map;

import org.junit.Test;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.fasterxml.jackson.core.type.TypeReference;

import code.ponfee.commons.collect.Collects;
import code.ponfee.commons.json.JsonPropertyFilter;
import code.ponfee.commons.json.JsonPropertyFilter.FilterType;
import code.ponfee.commons.json.Jsons;
import code.ponfee.commons.model.Result;

@SuppressWarnings("unchecked")
public class JsonsTest {

    @Test
    public void test1() {
        String json = Jsons.NORMAL.string(Collects.toMap("a", "abc", "b", 1));
        System.out.println(json);

        Map<String, Object> map = Jsons.NORMAL.parse(json, Map.class);
        System.out.println("parse(json, target): " + map);

        map = Jsons.NORMAL.parse(json, Map.class, String.class, Object.class);
        System.out.println("parse(json, collectClass, elemClasses): " + map);

        map = Jsons.NORMAL.parse(json, new TypeReference<Map<String, Object>>() {
        });
        System.out.println("parse(json, TypeReference): " + map);
    }

    @Test
    public void test2() {
        Map<?, ?> map = Collects.toMap("a", "xx", "b", 1, "c", 1.2D, "d", null);
        System.out.println(Jsons.toJson(map));

        Result<String> result = Result.success("xx");
        System.out.println(Jsons.toJson(result));
    }

    @Test
    public void test3() {
        Map<?, ?> map = Collects.toMap("a", "xx", "b", 1, "c", 1.2D, "d", null);
        System.out.println(JSONObject.toJSONString(map));
        System.out.println(JSONObject.toJSONString(map, new JsonPropertyFilter(FilterType.INCLUDES, "a", "b")));
        System.out.println(JSONObject.toJSONString(map, new JsonPropertyFilter(FilterType.EXCLUDES, "a", "b")));

        Result<String> result = Result.success("xx");
        System.out.println(JSONObject.toJSONString(result));
        System.out.println(JSONObject.toJSONString(result, new JsonPropertyFilter(FilterType.INCLUDES, "msg")));
        System.out.println(JSONObject.toJSONString(result, new JsonPropertyFilter(FilterType.EXCLUDES, "msg")));
    }

    @Test
    public void test4() {
        Map<?, ?> map = Collects.toMap("a", "xx", "b", 1, "c", 1.2D, "d", null);
        System.out.println(JSONObject.toJSONString(map));
        System.out.println(JSONObject.toJSONString(map, new SimplePropertyPreFilter("a", "b")));

        Result<String> result = Result.success("xx");
        System.out.println(JSONObject.toJSONString(result));
        System.out.println(JSONObject.toJSONString(result, new SimplePropertyPreFilter("msg")));
    }
}

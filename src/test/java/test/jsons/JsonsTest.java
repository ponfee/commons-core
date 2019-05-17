package test.jsons;

import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;

import code.ponfee.commons.collect.Collects;
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
        System.out.println(Jsons.toJson(map, "b", "c"));

        Result<String> result = Result.success("xx");
        System.out.println(Jsons.toJson(result));
        System.out.println(Jsons.toJson(result, "msg"));
    }
}

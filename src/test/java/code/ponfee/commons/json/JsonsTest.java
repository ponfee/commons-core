package code.ponfee.commons.json;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.fasterxml.jackson.core.type.TypeReference;

import code.ponfee.commons.collect.Collects;
import code.ponfee.commons.model.Result;
import code.ponfee.commons.util.ObjectUtils;

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
        System.out.println(JSONObject.toJSONString(map, new FastjsonPropertyFilter(PropertyFilterType.INCLUDES, "a", "b")));
        System.out.println(JSONObject.toJSONString(map, new FastjsonPropertyFilter(PropertyFilterType.EXCLUDES, "a", "b")));

        Result<String> result = Result.success("xx");
        System.out.println(JSONObject.toJSONString(result));
        System.out.println(JSONObject.toJSONString(result, new FastjsonPropertyFilter(PropertyFilterType.INCLUDES, "msg")));
        System.out.println(JSONObject.toJSONString(result, new FastjsonPropertyFilter(PropertyFilterType.EXCLUDES, "msg")));
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
    
    @Test
    public void test5() {
        System.out.println(Jsons.fromJson(Jsons.toJson(new StringBuilder("111111111")), StringBuilder.class));
        System.out.println(Jsons.fromJson(Jsons.toJson(new StringPlain()), StringPlain.class));

        System.out.println(JSON.parseObject(JSON.toJSONString(new StringBuilder("111111111")), StringBuilder.class));
        System.out.println(JSON.parseObject(JSON.toJSONString(new StringPlain()), StringPlain.class));

        System.out.println(JSON.parse(JSON.toJSONString(Arrays.asList(1, 2, 3, 4))));
    }

    public static class StringPlain implements Serializable {
        private static final long serialVersionUID = 1L;
        private StringBuilder sb = new StringBuilder("xxxxxxxxxx");

        public StringBuilder getSb() {
            return sb;
        }

        public void setSb(StringBuilder sb) {
            this.sb = sb;
        }

        @Override
        public String toString() {
            return ObjectUtils.toString(this);
        }
    }
}

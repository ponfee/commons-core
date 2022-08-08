package code.ponfee.commons.json;

import code.ponfee.commons.collect.Maps;
import code.ponfee.commons.model.Result;
import code.ponfee.commons.util.Dates;
import code.ponfee.commons.util.ObjectUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Data;
import org.javamoney.moneta.Money;
import org.junit.Assert;
import org.junit.Test;

import javax.money.Monetary;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

@SuppressWarnings("unchecked")
public class JsonsTest {

    @Test
    public void test1() {
        String json = Jsons.NORMAL.string(Maps.toMap("a", "abc", "b", 1));
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
        Map<?, ?> map = Maps.toMap("a", "xx", "b", 1, "c", 1.2D, "d", null);
        System.out.println(Jsons.toJson(map));

        Result<String> result = Result.success("xx");
        System.out.println(Jsons.toJson(result));
    }

    @Test
    public void test3() {
        Map<?, ?> map = Maps.toMap("a", "xx", "b", 1, "c", 1.2D, "d", null);
        System.out.println(JSON.toJSONString(map));
        System.out.println(JSON.toJSONString(map, FastjsonPropertyFilter.include("a", "b")));
        System.out.println(JSON.toJSONString(map, FastjsonPropertyFilter.exclude("a", "b")));

        Result<String> result = Result.success("xx");
        System.out.println(JSON.toJSONString(result));
        System.out.println(JSON.toJSONString(result, FastjsonPropertyFilter.include("msg")));
        System.out.println(JSON.toJSONString(result, FastjsonPropertyFilter.exclude("msg")));
    }

    @Test
    public void test4() {
        Map<?, ?> map = Maps.toMap("a", "xx", "b", 1, "c", 1.2D, "d", null);
        System.out.println(JSON.toJSONString(map));
        System.out.println(JSON.toJSONString(map, new SimplePropertyPreFilter("a", "b")));

        Result<String> result = Result.success("xx");
        System.out.println(JSON.toJSONString(result));
        System.out.println(JSON.toJSONString(result, new SimplePropertyPreFilter("msg")));
    }
    
    @Test
    public void test5() {
        System.out.println(Jsons.fromJson(Jsons.toJson(new StringBuilder("111111111")), StringBuilder.class));
        System.out.println(Jsons.fromJson(Jsons.toJson(new StringPlain()), StringPlain.class));

        System.out.println(JSON.parseObject(JSON.toJSONString(new StringBuilder("111111111")), StringBuilder.class));
        System.out.println(JSON.parseObject(JSON.toJSONString(new StringPlain()), StringPlain.class));

        System.out.println(JSON.parse(JSON.toJSONString(Arrays.asList(1, 2, 3, 4))));
    }

    @Test
    public void test6() {
        String datestring = "2022-01-01 01:01:01";
        
        Person person1 = new Person();
        person1.setName("tom");
        person1.setBirthday(Dates.toDate(datestring));
        person1.setBalance(Money.ofMinor(Monetary.getCurrency("CNY"), 999));
        String personString = Jsons.toJson(person1);
        Assert.assertEquals("{\"name\":\"tom\",\"birthday\":\"2022-01-01 01:01:01\",\"balance\":{\"currency\":\"CNY\",\"number\":999}}", personString);

        Person person2 = Jsons.fromJson("{\"name\":\"tom\",\"birthday\":\"2022-01-01 01:01:01\",\"balance\":{\"currency\":\"CNY\",\"number\":999},\"birthday2\":null,\"balance2\":null}", Person.class);
        Assert.assertEquals(person1.getBirthday(), person2.getBirthday());
        Assert.assertEquals(person1.getBalance(), person2.getBalance());


        Person person3 = Jsons.fromJson("{\"name\":\"tom\",\"birthday\":\"2022-01-01T01:01:01.0Z\",\"balance\":{\"currency\":\"CNY\",\"number\":999}}", Person.class);
        Assert.assertEquals(person1.getBirthday(), person3.getBirthday());
        Assert.assertEquals(person1.getBalance(), person3.getBalance());

        Person person4 = new Person();
        person4.setName("person4");
        String json4 = Jsons.toJson(person4);
        Assert.assertEquals(json4, "{\"name\":\"person4\"}");
        person4 = Jsons.fromJson(json4, Person.class);
        Assert.assertEquals(person4.getName(), "person4");
        Assert.assertNull(person4.getBalance());
    }

    @Test
    public void test7() {
        Money money1 = Money.ofMinor(Monetary.getCurrency("CNY"), 999);
        String moneyStr = Jsons.toJson(money1);
        System.out.println(moneyStr);
        Assert.assertEquals("{\"currency\":\"CNY\",\"number\":999}", moneyStr);
        Money money2 = Jsons.fromJson(moneyStr, Money.class);
        Assert.assertEquals(money1, money2);
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
    
    @Data
    public static class Person implements java.io.Serializable {
        private String name;
        private Date birthday;
        private Money balance;
        private Date birthday2;
        private Money balance2;
    }
    
}

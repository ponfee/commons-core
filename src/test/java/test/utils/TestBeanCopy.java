package test.utils;

import code.ponfee.commons.json.Jsons;
import code.ponfee.commons.model.Result;
import code.ponfee.commons.reflect.BeanMaps;
import code.ponfee.commons.reflect.BeanCopiers;
import code.ponfee.commons.reflect.Fields;
import org.junit.Test;
import org.springframework.cglib.beans.BeanCopier;

import java.util.Map;

public class TestBeanCopy {

    public static void main(String[] args) {
        Object obj = new Object();
        System.out.println(Fields.addressOf(obj));
        System.out.println(System.identityHashCode(obj));
    }

    static int round = 9999999;

    @Test
    public void test0() {
        Result<Void> result1 = Result.failure(-1, "error");
        Result<Void> result2 = new Result<>();
        for (int i = 0; i < round; i++) {
            org.springframework.beans.BeanUtils.copyProperties(result1, result2);
        }
    }

    @Test
    public void test1() {
        Result<Void> result1 = Result.failure(-1, "error");
        Result<Void> result2 = new Result<>();
        for (int i = 0; i < round; i++) {
            BeanCopiers.copy(result1, result2);
        }
    }

    @Test
    public void test2() {
        Result<Void> result1 = Result.failure(-1, "error");
        Result<Void> result2 = new Result<>();
        for (int i = 0; i < round; i++) {
            BeanCopier.create(Result.class, Result.class, false).copy(result1, result2, null);
        }
    }

    @Test
    public void test3() {
        BeanCopier copier = BeanCopier.create(Result.class, Result.class, false);
        Result<Void> result1 = Result.failure(-1, "error");
        Result<Void> result2 = new Result<>();
        for (int i = 0; i < round; i++) {
            copier.copy(result1, result2, null);
        }
    }

    // ------------------------------------------------------toMap
    @Test
    public void test4() {
        Result<Void> result1 = Result.failure(-1, "error");
        System.out.println(BeanMaps.PROPS.toMap(result1));
        for (int i = 0; i < round; i++) {
            BeanMaps.FIELDS.toMap(result1);
        }
    }

    @Test
    public void test5() {
        Result<Void> result1 = Result.failure(-1, "error");
        System.out.println(Jsons.toJson(BeanMaps.CGLIB.toMap(result1)));
        for (int i = 0; i < round; i++) {
            BeanMaps.CGLIB.toMap(result1);
        }
    }

    @Test
    public void test6() {
        Result<Void> result1 = Result.failure(-1, "error");
        System.out.println(BeanMaps.PROPS.toMap(result1));
        for (int i = 0; i < round; i++) {
            BeanMaps.PROPS.toMap(result1);
        }
    }

    @Test
    public void test7() {
        TestBean bean = new TestBean();
        Map<String, Object> map = BeanMaps.PROPS.toMap(bean);
        map.remove("failure");
        map.remove("success");
        System.out.println(map);
        System.out.println(Jsons.toJson(BeanMaps.PROPS.toBean(map, TestBean.class)));
        for (int i = 0; i < round; i++) {
            BeanMaps.CGLIB.toBean(map, TestBean.class);
        }
    }

    @Test
    public void test8() {
        TestBean bean = new TestBean();
        Map<String, Object> map = BeanMaps.CGLIB.toMap(bean);
        System.out.println(map);
        System.out.println(Jsons.toJson(BeanMaps.CGLIB.toBean(map, TestBean.class)));
        for (int i = 0; i < round; i++) {
            BeanMaps.CGLIB.toBean(map, TestBean.class);
        }
    }
    
    @Test
    public void test9() {
        TestBean bean = new TestBean();
        bean.setAge(20);
        bean.set_id("xxx");
        Map<String, Object> map = BeanMaps.PROPS.toMap(bean);
        System.out.println(map);

        map.put("_ip", "yyyy");

        System.out.println(Jsons.toJson(BeanMaps.CGLIB.toBean(map, TestBean.class)));
    }

    public static class TestBean {
        private int age;
        private String _id;

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String get_id() {
            return _id;
        }

        public void set_id(String _id) {
            this._id = _id;
        }
    }
}

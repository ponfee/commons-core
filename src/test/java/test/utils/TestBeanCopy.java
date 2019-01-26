package test.utils;

import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.cglib.beans.BeanCopier;

import code.ponfee.commons.json.Jsons;
import code.ponfee.commons.model.Result;
import code.ponfee.commons.reflect.CglibUtils;
import code.ponfee.commons.reflect.Fields;
import code.ponfee.commons.util.ObjectUtils;

public class TestBeanCopy {

    public static void main(String[] args) {
        Object obj = new Object();
        System.out.println(Fields.addressOf(obj));
        System.out.println(System.identityHashCode(obj));
    }
    
    static int round = 999999999;
    @Test @Ignore
    public void test0() {
        Result<Void> result1 = Result.failure(-1,  "error");
        Result<Void> result2 = new Result<>();
        for (int i = 0; i < round; i++) {
            org.springframework.beans.BeanUtils.copyProperties(result1, result2);
        }
    }
    
    @Test
    public void test1() {
        Result<Void> result1 = Result.failure(-1,  "error");
        Result<Void> result2 = new Result<>();
        for (int i = 0; i < round; i++) {
            CglibUtils.copyProperties(result1, result2);
        }
    }
    
    @Test
    public void test2() {
        Result<Void> result1 = Result.failure(-1,  "error");
        Result<Void> result2 = new Result<>();
        for (int i = 0; i < round; i++) {
            BeanCopier.create(Result.class, Result.class, false).copy(result1, result2, null);
        }
    }

    @Test
    public void test3() {
        BeanCopier copier = BeanCopier.create(Result.class, Result.class, false);
        Result<Void> result1 = Result.failure(-1,  "error");
        Result<Void> result2 = new Result<>();
        for (int i = 0; i < round; i++) {
            copier.copy(result1, result2, null);
        }
    }
    
    @Test
    public void test4() {
        Result<Void> result1 = Result.failure(-1,  "error");
        System.out.println(ObjectUtils.bean2map(result1));
        for (int i = 0; i < round; i++) {
            ObjectUtils.bean2map(result1);
        }
    }
    
    @Test
    public void test5() {
        Result<Void> result1 = Result.failure(-1,  "error");
        System.out.println(CglibUtils.bean2map(result1));
        for (int i = 0; i < round; i++) {
            CglibUtils.bean2map(result1);
        }
    }
    

    public static class TestBean {
        private int age;

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
        
        
    }

    @Test
    public void test6() {
        TestBean bean = new TestBean();
        Map<String, Object> map = ObjectUtils.bean2map(bean);
        map.remove("failure");
        map.remove("success");
        System.out.println(map);
        System.out.println(Jsons.toJson(ObjectUtils.map2bean(map, TestBean.class)));
        for (int i = 0; i < round; i++) {
            CglibUtils.map2bean(map, TestBean.class);
        }
    }

    @Test
    public void test7() {
        TestBean bean = new TestBean();
        Map<String, Object> map = CglibUtils.bean2map(bean);
        map.remove("failure");
        map.remove("success");
        System.out.println(map);
        System.out.println(Jsons.toJson(CglibUtils.map2bean(map, TestBean.class)));
        for (int i = 0; i < round; i++) {
            CglibUtils.map2bean(map, TestBean.class);
        }
    }
}

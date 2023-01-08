package cn.ponfee.commons.reflect;

import cn.ponfee.commons.model.BaseEntity;

/**
 * 
 * 
 * @author Ponfee
 */
public class FieldsTest {

    public static class ClassA extends BaseEntity.Creator<String> {
        private static final long serialVersionUID = -5617457253295566886L;

    }

    public static void main(String[] args) {
        ClassA a = new ClassA();
        System.out.println(a.getId());
        Fields.put(a, "id", 999L);
        System.out.println(a.getId());
    }
}

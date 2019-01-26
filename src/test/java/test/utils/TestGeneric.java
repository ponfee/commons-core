package test.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import code.ponfee.commons.reflect.ClassUtils;
import code.ponfee.commons.reflect.GenericUtils;
import code.ponfee.commons.util.ObjectUtils;

public class TestGeneric {

    public static class A {
        private int i;
        private String s;

        public int getI() {
            return i;
        }

        public void setI(int i) {
            this.i = i;
        }

        public String getS() {
            return s;
        }

        public void setS(String s) {
            this.s = s;
        }

    }

    public static class B {
        private List<? extends A> list;

        public List<? extends A> getList() {
            return list;
        }

        public void setList(List<? extends A> list) {
            this.list = list;
        }
    }
    
    @SuppressWarnings("restriction")
    public static void main(String[] args) throws Exception {
        Method method = B.class.getDeclaredMethod("setList", List.class);
        
        System.out.println("===="+GenericUtils.getActualTypeArgument(method, 0, 0));
        
        java.lang.reflect.Type type = method.getGenericParameterTypes()[0];
        System.out.println(type.getClass()); // class sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl
        System.out.println(type.getTypeName()); // java.util.List<test.TestGeneric$A>
        System.out.println("--------------------------------");
        sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl ptype = ( sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl)type;
        System.out.println(ptype.getRawType()); // interface java.util.List
        System.out.println(ptype.getActualTypeArguments()[0]);
        System.out.println(ptype.getOwnerType()); // null

        System.out.println("--------------------------------");
        Field field = B.class.getDeclaredField("list");
        System.out.println("===="+GenericUtils.getActualTypeArgument(field, 0));
        type = field.getGenericType();
        System.out.println(type.getTypeName()); // java.util.List<test.TestGeneric$A>
        System.out.println(type.getClass()); // class sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl
        
        System.out.println("--------------------------------");
        method = ClassUtils.class.getDeclaredMethod("getClassGenricType", Class.class, int.class);
        System.out.println(ObjectUtils.toString(method.getParameterTypes()));
        System.out.println(ObjectUtils.toString(method.getGenericParameterTypes()));
        System.out.println(ObjectUtils.toString(method.getTypeParameters()));
        System.out.println(ObjectUtils.toString(GenericUtils.getActualTypeArgument(method, 0, 0)));
    }

}

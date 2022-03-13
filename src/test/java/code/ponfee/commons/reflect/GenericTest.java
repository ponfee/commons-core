/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2019, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package code.ponfee.commons.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.List;

import org.junit.Test;

import code.ponfee.commons.model.BaseEntity;
import code.ponfee.commons.util.ObjectUtils;
import code.ponfee.commons.ws.adapter.MarshalJsonAdapter;
import code.ponfee.commons.ws.adapter.ResultListAdapter;
import code.ponfee.commons.ws.adapter.ResultListMapNormalAdapter;

/**
 * 
 * 
 * @author Ponfee
 */
public class GenericTest {

    @Test
    public void test0() {
        Field creator = ClassUtils.getField(BeanClass.class, "creator");
        Field id = ClassUtils.getField(BeanClass.class, "id");

        System.out.println("\n1--------------------------------");
        System.out.println(creator.getType());
        System.out.println(creator.getGenericType().getClass());

        System.out.println("\n2--------------------------------");
        System.out.println(id.getType());
        System.out.println(id.getGenericType().getClass());

        System.out.println("\n3--------------------------------");
        System.out.println(GenericUtils.getActualTypeArgument(id));
        System.out.println(GenericUtils.getActualTypeArgument(creator));
    }

    @Test
    public void test1() throws Exception {
        Method method = B.class.getDeclaredMethod("setList", List.class);

        System.out.println("====" + GenericUtils.getActualArgTypeArgument(method, 0, 0));

        java.lang.reflect.Type type = method.getGenericParameterTypes()[0];
        System.out.println(type.getClass()); // class sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl
        System.out.println(type.getTypeName()); // java.util.List<test.TestGeneric$A>

        System.out.println("\n1--------------------------------");
        ParameterizedType ptype = (ParameterizedType) type;
        System.out.println(ptype.getRawType()); // interface java.util.List
        System.out.println(ptype.getActualTypeArguments()[0]);
        System.out.println(ptype.getOwnerType()); // null

        System.out.println("\n2--------------------------------");
        Field field = B.class.getDeclaredField("list");
        System.out.println("====" + GenericUtils.getActualTypeArgument(field, 0));
        type = field.getGenericType();
        System.out.println(type.getTypeName()); // java.util.List<test.TestGeneric$A>
        System.out.println(type.getClass()); // class sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl

        System.out.println("\n3--------------------------------");
        method = GenericUtils.class.getDeclaredMethod("getActualTypeArgument", Class.class, int.class);
        System.out.println(ObjectUtils.toString(method.getParameterTypes()));
        System.out.println(ObjectUtils.toString(method.getGenericParameterTypes()));
        System.out.println(ObjectUtils.toString(method.getTypeParameters()));
        System.out.println(ObjectUtils.toString(GenericUtils.getActualArgTypeArgument(method, 0, 0)));
    }

    @Test
    public void test3() throws Exception {
        //System.out.println(GenericUtils.getActualTypeArgument(ClassUtils.getField(B.class, "f1")));
        System.out.println(GenericUtils.getActualTypeArgument(B.class));
        System.out.println(GenericUtils.getActualTypeArgument(String.class));
        System.out.println(GenericUtils.getActualTypeArgument(BeanClass.class));
    }

    @Test
    public void test4() throws Exception {
        System.out.println(GenericUtils.getFieldActualType(BeanClass.class, ClassUtils.getField(BeanClass.class, "id")));
        System.out.println(GenericUtils.getFieldActualType(BeanClass.class, ClassUtils.getField(BeanClass.class, "creator")));
        System.out.println(GenericUtils.getFieldActualType(BeanClass2.class, ClassUtils.getField(BeanClass2.class, "creator")));
    }

    @Test
    public void test5() throws Exception {
        TypeVariable<?> type = (TypeVariable<?>) ClassUtils.getField(BeanClass.class, "creator").getGenericType();
        GenericDeclaration gd = type.getGenericDeclaration();
        System.out.println(gd);
        for (TypeVariable<?> t : gd.getTypeParameters()) {
            System.out.println(type == t);
        }
    }

    @Test
    public void test6() throws Exception {
        /*// ResultListAdapter<T>
        System.out.println(ResultListAdapter.class.getTypeParameters());

        System.out.println("\n=========================================");
        // XmlAdapter<Result<ArrayItem<T>>, Result<List<T>>>
        Type ggs = ResultListAdapter.class.getGenericSuperclass();
        ParameterizedType pt = (ParameterizedType) ggs;
        System.out.println(pt.getTypeName()); // <=> toString()
        System.out.println(pt.getRawType()); // XmlAdapter
        System.out.println(pt.getOwnerType()); // 内部类的“父类”，如 Map就是 Map.Entry<String,String>的拥有者

        System.out.println(Arrays.toString(((Class<?>) pt.getRawType()).getTypeParameters()));
        System.out.println(Arrays.toString(pt.getActualTypeArguments()));*/

        System.out.println(GenericUtils.getActualTypeVariableMapping(ResultListMapNormalAdapter.class));
        System.out.println(GenericUtils.getActualTypeVariableMapping(ResultListAdapter.class));
        System.out.println(GenericUtils.getActualTypeVariableMapping(MarshalJsonAdapter.class));

        System.out.println(B.class.toGenericString());
        System.out.println(B.class.getDeclaredMethod("setList", List.class).toGenericString());
        System.out.println(B.class.getConstructor().toGenericString());
        
        System.out.println(B.class.toString());
        System.out.println(B.class.getDeclaredMethod("setList", List.class).toString());
        System.out.println(B.class.getConstructor().toString());
    }

    // -------------------------------------------------------------
    public static class BeanClass extends BaseEntity.Creator<String> {
        private static final long serialVersionUID = 1L;
    }
    
    public static class BeanClass2<E> extends BaseEntity.Creator<List<E>> {
        private static final long serialVersionUID = 1L;
    }

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

    public static class B<E> {
        private List<? extends A> list;
        private List<E[]> f1;

        public List<? extends A> getList() {
            return list;
        }

        public void setList(List<? extends A> list) {
            this.list = list;
        }

        public List<E[]> getF1() {
            return f1;
        }

        public void setF1(List<E[]> f1) {
            this.f1 = f1;
        }
    }
}

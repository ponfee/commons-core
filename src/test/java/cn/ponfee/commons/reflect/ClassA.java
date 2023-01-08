package cn.ponfee.commons.reflect;

/**
 * 
 * 
 * @author Ponfee
 */
public abstract class ClassA<T> {

 
    
    public void test1(T arg) {
        System.out.println(arg);
    }
    public T test2() {
        return (T)"";
    }
    
    public static class ClassB extends ClassA<String> {
        
    }
    
    public static void main(String[] args) throws Exception {
        System.out.println(GenericUtils.getMethodArgActualType(ClassB.class, ClassB.class.getMethod("test1", Object.class), 0));
        System.out.println(GenericUtils.getMethodReturnActualType(ClassB.class, ClassB.class.getMethod("test1", Object.class)));
        
        System.out.println();
        System.out.println(GenericUtils.getMethodArgActualType(ClassB.class, ClassB.class.getMethod("test1", String.class), 0));
        System.out.println(GenericUtils.getMethodReturnActualType(ClassB.class, ClassB.class.getMethod("test1", String.class)));
    }
}

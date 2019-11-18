package code.ponfee.commons.reflect;

import java.util.List;

import code.ponfee.commons.model.ExtendedBaseEntity;

public class GenericExtendsTest {

    public static class ClassA<U> {
    }

    public static abstract class ClassB<T> extends ClassA<T> implements List<T> {
    }

    public static interface InterfaceC<T> extends List<T>, java.io.Serializable {
    }

    public static void main(String[] args) {
        System.out.println(GenericUtils.getActualTypeVariableMapping(ClassB.class));
        System.out.println(GenericUtils.getGenericTypes(ClassB.class));
        System.out.println(GenericUtils.getGenericTypes(ExtendedBaseEntity.class));
    }

}

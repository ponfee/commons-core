package test;

import code.ponfee.commons.exception.UnimplementedException;
import code.ponfee.commons.json.Jsons;
import code.ponfee.commons.reflect.Fields;
import org.openjdk.jol.info.ClassLayout;

public class Test3 {

    public static abstract class Animal implements java.io.Serializable {
        private static final long serialVersionUID = 3890678647435825868L;
        private String name;
        private int age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }

    public static class Dog extends Animal {
        private static final long serialVersionUID = -2829034009272727923L;
        private String ext1;

        public String getExt1() {
            return ext1;
        }

        public void setExt1(String ext1) {
            this.ext1 = ext1;
        }
        
    }

    public static interface A {
        static A fromByteArray(byte[] arg) {
            throw new UnimplementedException();
        }
    }

    public static class B implements A {
        public B() {
            System.out.println("Create B.");
        }
        static B fromByteArray(byte[] arg) {
            return new B();
        }
    }

    public static class C implements A {
        public C() {
            System.out.println("Create C.");
        }
        static C fromByteArray(byte[] arg) {
            return new C();
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println(0>>1);
        System.out.println(1>>1);
        System.out.println(4>>1);
        System.out.println(System.getProperty("user.home"));
        B.fromByteArray(new byte[] {});
        Class<? extends A> clazz1 = B.class;
        clazz1.getDeclaredMethod("fromByteArray", byte[].class).invoke(null, new byte[] {});

        Dog dog = new Dog();
        dog.setName("xxx");
        dog.setAge(10);
        dog.setExt1("extxxx");
        System.out.println(Jsons.toJson(dog));

        Fields.put(dog, "ext1", "extyyy");
        System.out.println(Jsons.toJson(dog));
        Jsons.toJson(dog);

        System.out.println("---------------------------------------------");
        System.out.println(ClassLayout.parseClass(B.class).toPrintable());
    }

}

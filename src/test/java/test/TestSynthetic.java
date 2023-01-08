package test;

import java.lang.reflect.Method;

import junit.framework.TestCase;

/**
 * 
 * 
 * @author Ponfee
 */
public class TestSynthetic extends TestCase {

    public void testSynthetic() {
        try {
            new User().age = 1;
            System.out.println(new User().age);
            Method[] methods = User.class.getDeclaredMethods();
            for (Method method : methods) {
                System.out.println(method.toString() + ", " + method.isSynthetic());
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    class User {
        private int age;
        private String name;

        private User() {}

        private User(int age, String name) {
            this.age = age;
            this.name = name;
        }

        private int getAge() {
            return age;
        }

        private void setAge(int age) {
            this.age = age;
        }

        private String getName() {
            return name;
        }

        private void setName(String name) {
            this.name = name;
        }

    }
}

package test.utils;

public class TestEnum {

    enum A {
        ONE("one");
        private String name;

        A(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static void main(String[] args) {
        System.out.println(A.ONE.getName());
        A.ONE.setName("two");
        System.out.println(A.ONE.getName());
    }
}

package code.ponfee.commons.innerclass;

public class TryUsingAnonymousClass {

    public static void main(String[] args) {
        new TryUsingAnonymousClass().useMyInterface();
    }

    public void useMyInterface() {
        final Integer number = 123;
        System.out.println(number);

        MyInterface myInterface = new MyInterface() {
            @Override
            public void doSomething() {
                System.out.println(number);
            }
        };
        myInterface.doSomething();

        System.out.println(number);
    }
}
package test.concurrent;

public class InheritableThreadLocalTest {

    private static InheritableThreadLocal<StringBuffer> ITL = new InheritableThreadLocal<StringBuffer>() {
        protected StringBuffer initialValue() {
            return new StringBuffer("hello");
        }
    };

    public static void main(String[] args) {
        System.out.println(Thread.currentThread().getName() + " : " + ITL.get());
        new Thread(new Runnable() {
            public void run() {
                System.out.println(Thread.currentThread().getName() + " : " + ITL.get());
                new Thread(new Runnable() {
                    public void run() {
                        System.out.println(Thread.currentThread().getName() + " : " + ITL.get());
                        ITL.get().append(", wqf");
                        System.out.println(Thread.currentThread().getName() + " : " + ITL.get());
                    }
                }).start();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                System.out.println(Thread.currentThread().getName() + " : " + ITL.get());
            }
        }).start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName() + " : " + ITL.get());
    }

}

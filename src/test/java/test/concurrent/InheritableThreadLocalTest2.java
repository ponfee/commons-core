package test.concurrent;

import com.alibaba.ttl.TransmittableThreadLocal;

public class InheritableThreadLocalTest2 {

    //private static InheritableThreadLocal<String> ITL = new InheritableThreadLocal<String>() {
    private static TransmittableThreadLocal<String> ITL = new TransmittableThreadLocal<String>() {
        protected String initialValue() {
            return "hello";
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
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
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
        }).start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        ITL.set("word"); // 创建子线程的时候拷贝父线程的threadLocalMap数据到子线程，之后与父线程没有关系了
        System.out.println(Thread.currentThread().getName() + " : " + ITL.get());
    }

}

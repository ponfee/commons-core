package test.concurrent;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TheadPoolExecTester {

    /*private static final ExecutorService EXECUTOR = new ThreadPoolExecutor(2, 10, 300, TimeUnit.SECONDS, 
                                                                           new SynchronousQueue<Runnable>(), 
                                                                           new ThreadPoolExecutor.CallerRunsPolicy());*/
    private static final ExecutorService EXECUTOR = new ThreadPoolExecutor(2, 10, 300, TimeUnit.SECONDS, 
                                                                           new LinkedBlockingQueue<>(300),
                                                                           new ThreadPoolExecutor.CallerRunsPolicy());
    
    public static void main(String[] args) {
        System.out.println("main thread "+Thread.currentThread().getId());
        for (int i = 0; i < 200; i++) {
            EXECUTOR.submit(new Caller());
        }
        EXECUTOR.shutdown();
    }
    
    private static final class Caller implements Callable<Boolean> {

        @Override
        public Boolean call() throws Exception {
            System.out.println(Thread.currentThread().getId() + "----start");
            Thread.sleep(1000+new Random().nextInt(10000));
            System.out.println(Thread.currentThread().getId() + "----end");
            return true;
        }
        
    }
}

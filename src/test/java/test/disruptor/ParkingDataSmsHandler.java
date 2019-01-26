package test.disruptor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

import com.lmax.disruptor.EventHandler;

public class ParkingDataSmsHandler implements EventHandler<InParkingDataEvent> {
    private final CountDownLatch latch;
    public ParkingDataSmsHandler(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void onEvent(InParkingDataEvent event, long sequence, boolean endOfBatch) throws Exception {
        long start = System.currentTimeMillis();
        Thread.sleep(ThreadLocalRandom.current().nextInt(400) + 100);
        long threadId = Thread.currentThread().getId();
        String threadName = Thread.currentThread().getName();
        String carLicense = event.getCarLicense();
        System.out.println(String.format("[%s, %s] send %s in plaza sms to user %s", threadId, threadName, carLicense, System.currentTimeMillis()-start));
        latch.countDown();
    }
}

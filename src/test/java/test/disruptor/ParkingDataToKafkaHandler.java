package test.disruptor;

import java.util.concurrent.ThreadLocalRandom;

import com.lmax.disruptor.EventHandler;

public class ParkingDataToKafkaHandler implements EventHandler<InParkingDataEvent> {

    @Override
    public void onEvent(InParkingDataEvent event, long sequence,
                        boolean endOfBatch) throws Exception {
        long start = System.currentTimeMillis();
        Thread.sleep(ThreadLocalRandom.current().nextInt(300));
        long threadId = Thread.currentThread().getId();
        String threadName = Thread.currentThread().getName();
        String carLicense = event.getCarLicense();
        System.out.println(String.format("[%s, %s] send %s in plaza messsage to kafka %s", threadId, threadName, carLicense, (System.currentTimeMillis()-start)));
    }
}

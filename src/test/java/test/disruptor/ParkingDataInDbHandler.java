package test.disruptor;

import java.util.concurrent.ThreadLocalRandom;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.WorkHandler;

public class ParkingDataInDbHandler implements EventHandler<InParkingDataEvent>, WorkHandler<InParkingDataEvent> {

    @Override
    public void onEvent(InParkingDataEvent event) throws Exception {
        long start = System.currentTimeMillis();
        Thread.sleep(ThreadLocalRandom.current().nextInt(400));
        long threadId = Thread.currentThread().getId();
        String threadName = Thread.currentThread().getName();
        String carLicense = event.getCarLicense();
        System.out.println(String.format("[%s, %s] save %s into db %s", threadId, threadName, carLicense, System.currentTimeMillis()-start));
    }

    @Override
    public void onEvent(InParkingDataEvent event, long sequence, boolean endOfBatch) throws Exception {
        // TODO Auto-generated method stub
        this.onEvent(event);
    }

}

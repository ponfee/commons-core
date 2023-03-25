package test.disruptor;

import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang3.RandomStringUtils;

import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.EventHandlerGroup;
import com.lmax.disruptor.dsl.ProducerType;

import cn.ponfee.commons.concurrent.NamedThreadFactory;

/**
 * 测试 P1生产消息，C1，C2消费消息，C1和C2会共享所有的event元素! C3依赖C1，C2处理结果
 */
@SuppressWarnings({ "unchecked", "deprecation" })
public class Main {
    private static final int COUNT = 50;
    private static final int RING_BUFFER_SIZE = 1;

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(COUNT);

        //构造缓冲区与事件生成
        Disruptor<InParkingDataEvent> disruptor = new Disruptor<>(() -> new InParkingDataEvent(), RING_BUFFER_SIZE,
                                                                  NamedThreadFactory.builder().prefix("disruptor").build(),
                                                                  ProducerType.SINGLE, new YieldingWaitStrategy());

        //使用disruptor创建消费者组C1,C2
        EventHandlerGroup<InParkingDataEvent> handlerGroup = disruptor.handleEventsWith(new ParkingDataToKafkaHandler(),
                                                                                        new ParkingDataInDbHandler());

        //声明在C1,C2完事之后执行SMS消息发送操作 也就是流程走到C3
        handlerGroup.then(new ParkingDataSmsHandler(latch));

        //启动
        disruptor.start();

        long beginTime = System.currentTimeMillis();
        //生产
        for (int i = 0; i < COUNT; i++) {
            /*RingBuffer<InParkingDataEvent> ringBuffer = disruptor.getRingBuffer();
            long seq = ringBuffer.next();
            try {
                ringBuffer.get(seq).setCarLicense("[粤B·" + RandomStringUtils.randomAlphanumeric(5).toUpperCase() + "]");
            } finally {
                ringBuffer.publish(seq);
            }*/
            disruptor.publishEvent((inParkingEvent, sequence) -> {
                inParkingEvent.setCarLicense("[粤B·" + RandomStringUtils.randomAlphanumeric(5).toUpperCase() + "]");
                System.out.println("["+Thread.currentThread().getId() + ", " +
                                   Thread.currentThread().getName() + "] in parking " + inParkingEvent.getCarLicense());
            });
        }

        latch.await(); // 等待全部处理结束
        System.out.println("已通行" + COUNT + "车辆，总耗时:" + (System.currentTimeMillis() - beginTime));
        disruptor.shutdown();
    }
}

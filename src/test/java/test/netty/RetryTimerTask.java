package test.netty;

import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class RetryTimerTask implements TimerTask {

    private static final Logger log = LoggerFactory.getLogger(RetryTimerTask.class);

    // 每隔几秒执行一次
    private final long tick;

    // 最大重试次数
    private final int retries;

    private int retryTimes = 0;

    private Runnable task;

    public RetryTimerTask(Runnable task, long tick, int retries) {
        this.tick = tick;
        this.retries = retries;
        this.task = task;
    }

    @Override
    public void run(Timeout timeout) {
        try {
            task.run();
        } catch (Throwable e) {
            if ((++retryTimes) >= retries) {
                // 重试次数超过了设置的值
                log.error("失败重试次数超过阈值: {}，不再重试", retries);
            } else {
                log.error("重试失败，继续重试");
                rePut(timeout);
            }
        }
    }

    // 通过 timeout 拿到 timer 实例，重新提交一个定时任务
    private void rePut(Timeout timeout) {
        if (timeout == null) {
            return;
        }
        if (timeout.isCancelled()) {
            return;
        }
        System.out.println(timeout.task()==this); // true
        timeout.timer().newTimeout(timeout.task(), tick, TimeUnit.SECONDS);
    }
}

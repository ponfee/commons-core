package test.netty;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class Application {

    public static void main(String[] args) {
        Application app = new Application();
        app.invoke();
    }

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    private volatile Timer failTimer = null;

    public void invoke() {
        try {
            doInvoke();
        } catch (Throwable e) {
            log.error("调用 doInvoke 方法失败，5s 后将进入后台的自动重试，异常信息: ", e);
            addFailed(() -> doInvoke());
        }
    }

    // 实际的业务实现
    private void doInvoke() {
        // 这里让这个方法故意失败
        throw new RuntimeException("故意抛出异常");
    }

    private void addFailed(Runnable task) {
        // 延迟初始化
        if (failTimer == null) {
            synchronized (this) {
                if (failTimer == null) {
                    failTimer = new HashedWheelTimer();
                }
            }
        }
        RetryTimerTask retryTimerTask = new RetryTimerTask(task, 3, 5);
        try {
            // 5s 后执行第一次重试
            failTimer.newTimeout(retryTimerTask, 5, TimeUnit.SECONDS);
        } catch (Throwable e) {
            log.error("提交定时任务失败，exception: ", e);
        }
    }
}

package code.ponfee.commons.concurrent;

import code.ponfee.commons.model.ToJsonString;
import lombok.Getter;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池监控信息
 * 
 * @author Ponfee
 */
@Getter
public class ThreadPoolMonitor extends ToJsonString implements java.io.Serializable {
    private static final long serialVersionUID = 3890678647435855868L;

    private final long keepAliveTime;

    private final int corePoolSize;
    private final int maximumPoolSize;
    private final int largestPoolSize;
    private final int poolSize;

    private final long taskCount;
    private final long completedTaskCount;
    private final int queueSize;
    private final int activeCount;

    private final boolean shutdown;
    private final boolean terminated;

    public ThreadPoolMonitor(ThreadPoolExecutor pool) {
        this.keepAliveTime = pool.getKeepAliveTime(TimeUnit.MILLISECONDS);

        this.corePoolSize = pool.getCorePoolSize();
        this.maximumPoolSize = pool.getMaximumPoolSize();
        this.largestPoolSize = pool.getLargestPoolSize();
        this.poolSize = pool.getPoolSize();

        this.taskCount = pool.getTaskCount();
        this.completedTaskCount = pool.getCompletedTaskCount();
        this.queueSize = pool.getQueue().size();
        this.activeCount = pool.getActiveCount();

        this.shutdown = pool.isShutdown();
        this.terminated = pool.isTerminated();
    }

}

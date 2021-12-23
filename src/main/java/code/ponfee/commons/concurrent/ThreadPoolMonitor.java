package code.ponfee.commons.concurrent;

import code.ponfee.commons.json.Jsons;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池监控信息
 * 
 * @author Ponfee
 */
public class ThreadPoolMonitor implements java.io.Serializable {

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
        /*String threadPoolInfo = String.format(
            "thread_pool_info: keep_alive_time=%d, core_pool_size=%d, maximum_pool_size=%d, largest_pool_size=%d, pool_size=%d, task_count=%d, completed_task_count=%d, queue_size=%d, active_count=%d, is_shutdown=%s, is_terminated=%s",
            pool.getKeepAliveTime(TimeUnit.MILLISECONDS), pool.getCorePoolSize(), pool.getMaximumPoolSize(), pool.getLargestPoolSize(), pool.getPoolSize(), 
            pool.getTaskCount(), pool.getCompletedTaskCount(), pool.getQueue().size(), pool.getActiveCount(), pool.isShutdown(), pool.isTerminated()
        );*/
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

    public long getKeepAliveTime() {
        return keepAliveTime;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public int getLargestPoolSize() {
        return largestPoolSize;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public long getTaskCount() {
        return taskCount;
    }

    public long getCompletedTaskCount() {
        return completedTaskCount;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public int getActiveCount() {
        return activeCount;
    }

    public boolean isShutdown() {
        return shutdown;
    }

    public boolean isTerminated() {
        return terminated;
    }

    @Override
    public String toString() {
        return Jsons.toJson(this);
    }

}

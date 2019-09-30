package code.ponfee.commons.util;

/**
 * <pre>
 * 0 | 0000000000 0000000000 0000000000 0000000000 0 | 00000 | 00000 | 0000000000 00
 * - | --------------------时间戳--------------------- | -did- | -wid- | -----seq-----
 *  0 ~  0：1位未使用（实际上也可作为long的符号位）
 *  1 ~ 41：41位为毫秒级时间（能到2039-09-07 23:47:35，超过会溢出）
 * 42 ~ 46：5位datacenterId
 * 47 ~ 51：5位workerId（并不算标识符，实际是为线程标识），
 * 52 ~ 63：12位该毫秒内的当前毫秒内的计数
 *
 * 毫秒内序列 （由datacenter和机器ID作区分），并且效率较高。经测试，
 * snowflake每秒能够产生26万ID左右，完全满足需要。
 * </pre>
 *
 * 计算掩码方式：(1<<bits)-1 或 -1L^(-1L<<bits)
 * 基于snowflake算法的ID生成器
 *
 * @author Ponfee
 */
public final class IdWorker {

    private static final int MAX_SIZE = Long.toBinaryString(Long.MAX_VALUE).length();
    private static final long TWEPOCH = 1514736000000L; // 起始基准时间点(2018-01-01)

    private final long sequenceMask;
    private final long workerIdShift;
    private final long datacenterIdShift;
    private final long timestampShift;
    private final long timestampMask;

    private final int datacenterId; // 数据中心id
    private final int workerId; // 工作机器id

    private long lastTimestamp = -1L; // 时间戳
    private long sequence = 0L; // 0，并发控制

    public IdWorker(int workerId, int datacenterId,
                    int sequenceBits, int workerIdBits, 
                    int datacenterIdBits) {
        long maxWorkerId = (1L << workerIdBits) - 1;
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(
                String.format("worker Id can't be greater than %d "
                            + "or less than 0", maxWorkerId)
            );
        }

        long maxDatacenterId = (1L << datacenterIdBits) - 1;
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException(
                String.format("datacenter Id can't be greater than %d "
                            + "or less than 0", maxDatacenterId)
            );
        }

        this.sequenceMask = (1L << sequenceBits) - 1;
        this.workerIdShift = sequenceBits;
        this.datacenterIdShift = this.workerIdShift + workerIdBits;
        this.timestampShift = this.datacenterIdShift + datacenterIdBits;
        this.timestampMask = (1L << (MAX_SIZE - this.timestampShift)) - 1;

        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    /**
     * sequenceBits: 12 bit, value range of 0 ~ 4095(111111111111)
     * workerIdBits: 5 bit, value range of 0 ~ 31(11111)
     * datacenterIdBits: 5 bit, value range of 0 ~ 31(11111)
     * 
     * workerIdShift: sequenceBits，左移12位(seq12位)
     * datacenterIdShift: sequenceBits+workerIdBits，即左移17位(wid5位+seq12位)
     * timestampShift: sequenceBits+workerIdBits+datacenterIdBits，
     *                 即左移22位(did5位+wid5位+seq12位)
     * timestampMask: (1L<<(MAX_SIZE-timestampShift))-1 = (1L<<41)-1
     * 
     * @param workerId
     * @param datacenterId
     */
    public IdWorker(int workerId, int datacenterId) {
        this(workerId, datacenterId, 12, 5, 5);
    }

    public IdWorker(int workerId) {
        this(workerId, 0);
    }

    public synchronized long nextId() {
        long timestamp = timeGen();
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(
                String.format("Clock moved backwards. Refusing to generate id "
                            + "for %d milliseconds", lastTimestamp - timestamp)
            );
        }
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = timestamp;

        return (((timestamp - TWEPOCH) << timestampShift) & timestampMask)
             | (datacenterId << datacenterIdShift)
             | (workerId << workerIdShift)
             | sequence;
    }

    protected long tilNextMillis(long lastTimestamp) {
        long timestamp;
        do {
            timestamp = timeGen();
        } while (timestamp <= lastTimestamp);
        return timestamp;
    }

    protected long timeGen() {
        return System.currentTimeMillis();
    }

    /**
     * <pre>
     * 0 | 0000000000 0000000000 0000000000 0000000000 00 | 0000000000 0 | 0000000000
     * - | --------------------－时间戳--------------------－ | -----wid---- | ----seq---
     *   0 ~ 0：1位未使用（实际上也可作为long的符号位）
     *  1 ~ 42：42位为毫秒级时间（能到2109-05-15 15:35:11，超过会溢出）
     *    ~   ：0位datacenterId
     * 43 ~ 53：11位workerId（机器ip），
     * 54 ~ 63：10位该毫秒内的当前毫秒内的计数
     * </pre>
     * 
     * Builds a IdWorker based local ip address
     */
    public static final IdWorker LOCAL_WORKER;
    static {
        int sequenceBits = 10; // specified 10 bit length
        int workerIdBits = 11; // specified 11 bit length
        int datacenterIdBits = 0; // specified 0 bit length

        int maxWorkerId = (int) (1L << workerIdBits) - 1; // 2047(max and mask)
        int workerId = (int) Networks.toLong(Networks.HOST_IP) & maxWorkerId;
        int datacenterId = (int) (1L << datacenterIdBits) - 1;

        LOCAL_WORKER = new IdWorker(workerId, datacenterId, sequenceBits,
                                    workerIdBits, datacenterIdBits);
    }

}

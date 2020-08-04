package code.ponfee.commons.base;

/**
 * 时间戳服务提供
 *
 * @author Ponfee
 */
@FunctionalInterface
public interface TimestampProvider {

    TimestampProvider EARLIEST = () -> Long.MIN_VALUE;
    TimestampProvider CURRENT  = System::currentTimeMillis;
    TimestampProvider LATEST   = () -> Long.MAX_VALUE;

    long get();

}

package code.ponfee.commons.cache;

/**
 * 时间戳服务提供
 *
 * @author fupf
 */
@FunctionalInterface
public interface TimestampProvider {

    TimestampProvider CURRENT  = System::currentTimeMillis;
    TimestampProvider LATEST   = () -> Long.MAX_VALUE;
    TimestampProvider EARLIEST = () -> Long.MIN_VALUE;

    long now();

}

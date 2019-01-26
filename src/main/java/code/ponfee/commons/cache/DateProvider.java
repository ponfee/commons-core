package code.ponfee.commons.cache;

/**
 * 时间服务提供
 *
 * @author fupf
 */
@FunctionalInterface
public interface DateProvider {

    DateProvider CURRENT = System::currentTimeMillis;
    DateProvider LATEST = () -> Long.MAX_VALUE;

    long now();

}

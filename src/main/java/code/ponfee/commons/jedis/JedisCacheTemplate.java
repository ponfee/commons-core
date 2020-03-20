package code.ponfee.commons.jedis;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

/**
 * Jedis client cache template(template method pattern)
 * 
 * @author Ponfee
 */
public class JedisCacheTemplate {

    private final JedisClient client;

    public JedisCacheTemplate(@Nonnull JedisClient client) {
        this.client = client;
    }

    public <T> T execute(String redisKey, Class<T> type, Supplier<T> supplier) {
        return execute(redisKey.getBytes(), 86400, type, supplier);
    }

    public <T> T execute(byte[] redisKey, Class<T> type, Supplier<T> supplier) {
        return execute(redisKey, 86400, type, supplier);
    }

    public <T> T execute(String redisKey, int timeoutSeconds,
                         Class<T> type, Supplier<T> supplier) {
        return this.execute(redisKey.getBytes(), timeoutSeconds, type, supplier);
    }

    public <T> T execute(byte[] redisKey, int timeoutSeconds, 
                         Class<T> type, Supplier<T> supplier) {
        ValueOperations ops = client.valueOps();
        T value;

        if ((value = ops.getObject(redisKey, type)) != null) {
            return value;
        }

        if ((value = supplier.get()) != null) {
            ops.setObject(redisKey, value, timeoutSeconds);
        }

        return value;
    }

    public JedisClient getClient() {
        return client;
    }

}

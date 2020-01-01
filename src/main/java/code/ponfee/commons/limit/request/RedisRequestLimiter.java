package code.ponfee.commons.limit.request;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import code.ponfee.commons.jedis.JedisClient;

/**
 * The request limiter based redis
 * 
 * @author Ponfee
 */
public class RedisRequestLimiter extends RequestLimiter{

    private final JedisClient client;

    private RedisRequestLimiter(JedisClient client) {
        this.client = client;
    }

    public static RedisRequestLimiter create(JedisClient client) {
        return new RedisRequestLimiter(client);
    }

    @Override 
    public RedisRequestLimiter limitFrequency(String key, int period, String message)
        throws RequestLimitException {
        checkLimit(CHECK_FREQ_KEY + key, period, 1, message);
        return this;
    }

    @Override 
    public RedisRequestLimiter limitThreshold(String key, int period, 
                                              int limit, String message) 
        throws RequestLimitException {
        checkLimit(CHECK_THRE_KEY + key, period, limit, message);
        return this;
    }

    @Override 
    public void cacheCode(String key, String code, int ttl) {
        client.valueOps().set(CACHE_CODE_KEY + key, code, ttl);
        client.keysOps().del(CHECK_CODE_KEY + key);
    }

    @Override 
    public RedisRequestLimiter checkCode(String key, String code, int limit)
        throws RequestLimitException {
        if (StringUtils.isEmpty(code)) {
            throw new RequestLimitException("验证码不能为空！");
        }

        String cacheKey = CACHE_CODE_KEY + key;

        // 1、判断验证码是否已失效
        String actual = client.valueOps().get(cacheKey);
        if (actual == null) {
            throw new RequestLimitException("验证码失效，请重新获取！");
        }

        String checkKey = CHECK_CODE_KEY + key;

        // 2、检查是否验证超过限定次数
        long times = client.valueOps().incrBy(checkKey);
        if (times == 1) {
            int ttl = client.keysOps().ttl(cacheKey).intValue() + 1; // calc check key ttl
            client.keysOps().expire(checkKey, ttl); // 第一次验证，设置验证标识数据的缓存失效期
        } else if (times > limit) {
            client.keysOps().del(cacheKey, checkKey); // 超过验证次数，删除缓存中的验证码
            throw new RequestLimitException("验证错误次数过多，请重新获取！");
        }

        // 3、检查验证码是否匹配
        if (!actual.equals(code)) {
            throw new RequestLimitException("验证码错误！");
        }

        // 验证成功，删除缓存key
        client.keysOps().del(cacheKey, checkKey);
        return this;
    }

    @Override 
    public void cacheCaptcha(String key, String captcha, int expire) {
        client.valueOps().set(CACHE_CAPTCHA_KEY + key, captcha, expire);
    }

    @Override 
    public boolean checkCaptcha(String key, String captcha, boolean caseSensitive) {
        String value = client.valueOps().getAndDel(CACHE_CAPTCHA_KEY + key);

        if (value == null) {
            return false;
        }

        return caseSensitive ? value.equals(captcha) : value.equalsIgnoreCase(captcha);
    }

    // ---------------------------------------------------------action
    @Override
    public void recordAction(String key, int period) {
        client.valueOps().incrByEX(TRACE_ACTION_KEY + key, 1, period);
    }

    @Override
    public long countAction(String key) {
        return Optional.ofNullable(
            client.valueOps().getLong(TRACE_ACTION_KEY + key)
        ).orElse(0L);
    }

    @Override
    public void resetAction(String key) {
        client.keysOps().del(TRACE_ACTION_KEY + key);
    }

    // -----------------------------------------------------------------------private methods
    private void checkLimit(String key, int ttl, int limit, String message)
        throws RequestLimitException {
        long times = client.valueOps().incrByEX(key, 1, ttl);
        if (times > limit) {
            throw new RequestLimitException(message);
        }
    }

}

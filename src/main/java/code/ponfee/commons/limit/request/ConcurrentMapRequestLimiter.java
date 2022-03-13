package code.ponfee.commons.limit.request;

import code.ponfee.commons.util.Asserts;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The request limiter based ConcurrentHashMap
 * 
 * Warning: distribute depoly with multiple server nodes maybe occur problem
 * 
 * @author Ponfee
 */
public final class ConcurrentMapRequestLimiter extends RequestLimiter {

    private final ConcurrentMap<String, CacheValue<?>> cache = new ConcurrentHashMap<>();
    private final Lock lock = new ReentrantLock(); // 定时清理加锁

    private ConcurrentMapRequestLimiter(ScheduledExecutorService scheduler) {
        Asserts.notNull(scheduler, "Scheduler cannot be null.");
        scheduler.scheduleAtFixedRate(() -> {
            if (!lock.tryLock()) {
                return;
            }
            try {
                long now = System.currentTimeMillis();
                cache.entrySet().removeIf(x -> x.getValue().isExpire(now));
            } finally {
                lock.unlock();
            }
        }, 60, 120, TimeUnit.SECONDS);
    }

    // ---------------------------------------------------------------------request limit
    @Override
    public ConcurrentMapRequestLimiter limitFrequency(String key, int period, String message)
        throws RequestLimitException {
        checkLimit(CHECK_FREQ_KEY + key, period, 1, message);
        return this;
    }

    @Override
    public ConcurrentMapRequestLimiter limitThreshold(String key, int period, 
                                                      int limit, String message) 
        throws RequestLimitException {
        checkLimit(CHECK_THRE_KEY + key, period, limit, message);
        return this;
    }

    // ---------------------------------------------------------------------cache sms code
    @Override
    public void cacheCode(String key, String code, int ttl) {
        add(CACHE_CODE_KEY + key, code, ttl);
        remove(CHECK_CODE_KEY + key);
    }

    @Override
    public ConcurrentMapRequestLimiter checkCode(String key, String code, int limit)
        throws RequestLimitException {
        if (StringUtils.isEmpty(code)) {
            throw new RequestLimitException("验证码不能为空！");
        }

        String cacheKey = CACHE_CODE_KEY + key;

        // 1、判断验证码是否已失效
        CacheValue<String> actual = get(cacheKey);
        if (actual == null || actual.get() == null) {
            throw new RequestLimitException("验证码失效，请重新获取！");
        }

        String checkKey = CHECK_CODE_KEY + key;

        // 2、检查是否验证超过限定次数
        CacheValue<?> times = incrementAndGet(checkKey, actual.expireTimeMillis);
        if (times.count() > limit) {
            remove(cacheKey, checkKey); // 超过验证次数，删除缓存中的验证码
            throw new RequestLimitException("验证错误次数过多，请重新获取！");
        }

        // 3、检查验证码是否匹配
        if (!actual.get().equals(code)) {
            throw new RequestLimitException("验证码错误！");
        }

        // 验证成功，删除缓存key
        remove(cacheKey, checkKey);
        return this;
    }

    // ---------------------------------------------------------------------cache captcha
    @Override
    public void cacheCaptcha(String key, String captcha, int expire) {
        add(CACHE_CAPTCHA_KEY + key, captcha, expire);
    }

    @Override
    public boolean checkCaptcha(String key, String captcha, boolean caseSensitive) {
        CacheValue<String> value = getAndRemove(CACHE_CAPTCHA_KEY + key);

        if (value == null || value.get() == null) {
            return false;
        }

        return caseSensitive 
               ? value.get().equals(captcha)
               : value.get().equalsIgnoreCase(captcha);
    }

    // ---------------------------------------------------------------------action
    @Override
    public void recordAction(String key, int period) {
        incrementAndGet(TRACE_ACTION_KEY + key, expire(period));
    }

    @Override
    public long countAction(String key) {
        CacheValue<Void> cache = get(TRACE_ACTION_KEY + key);
        return cache == null ? 0 : cache.count();
    }

    @Override
    public void resetAction(String key) {
        remove(TRACE_ACTION_KEY + key);
    }

    // ---------------------------------------------------------------------private methods
    private void checkLimit(String key, int ttl, int limit, String message)
        throws RequestLimitException {
        CacheValue<?> cache = incrementAndGet(key, expire(ttl));
        if (cache.count() > limit) {
            throw new RequestLimitException(message);
        }
    }

    private CacheValue<?> incrementAndGet(String key, long expireTimeMillis) {
        CacheValue<?> value = cache.get(key);
        if (value == null || value.isExpire()) {
            synchronized (cache) {
                value = cache.get(key);
                if (value == null || value.isExpire()) { // 失效则重置
                    value = new CacheValue<>(null, expireTimeMillis);
                    cache.put(key, value);
                    return value;
                }
            }
        }
        value.increment();
        return value;
    }

    private void remove(String... keys) {
        for (String key : keys) {
            cache.remove(key);
        }
    }

    private <T> CacheValue<T> getAndRemove(String key) {
        CacheValue<T> value = (CacheValue<T>) cache.remove(key);
        return value == null || value.isExpire() ? null : value;
    }

    private <T> void add(String key, T value, int ttl) {
        cache.put(key, new CacheValue<>(value, expire(ttl)));
    }

    private <T> CacheValue<T> get(String key) {
        CacheValue<T> value = (CacheValue<T>) cache.get(key);
        if (value == null) {
            return null;
        } else if (value.isExpire()) {
            cache.remove(key);
            return null;
        } else {
            return value;
        }
    }

}

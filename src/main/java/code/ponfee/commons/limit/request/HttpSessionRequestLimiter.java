package code.ponfee.commons.limit.request;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpSession;

/**
 * The request limiter based http session
 * 
 * Warning: User clear cookie maybe occur problem
 * 
 * @author Ponfee
 */
@SuppressWarnings("unchecked")
public class HttpSessionRequestLimiter extends RequestLimiter {

    private final HttpSession session;

    private HttpSessionRequestLimiter(HttpSession session) {
        this.session = session;
    }

    public static HttpSessionRequestLimiter create(HttpSession session) {
        return new HttpSessionRequestLimiter(session);
    }

    // ---------------------------------------------------------------------request limit
    /**
     * Client user (web browser) can clear session(cookie),
     * so this limit can't really effect
     * 
     * @deprecated
     */
    @Override @Deprecated
    public HttpSessionRequestLimiter limitFrequency(String key, int period, String message)
        throws RequestLimitException {
        checkLimit(CHECK_FREQ_KEY + key, period, 1, message);
        return this;
        //throw new UnsupportedOperationException();
    }

    @Override @Deprecated
    public HttpSessionRequestLimiter limitThreshold(String key, int period, 
                                                              int limit, String message) 
        throws RequestLimitException {
        checkLimit(CHECK_THRE_KEY + key, period, limit, message);
        return this;
        //throw new UnsupportedOperationException();
    }

    // ---------------------------------------------------------------------cache sms code
    @Override
    public void cacheCode(String key, String code, int ttl) {
        add(CACHE_CODE_KEY + key, code, ttl);
        remove(CHECK_CODE_KEY + key);
    }

    @Override
    public HttpSessionRequestLimiter checkCode(String key, String code, int limit)
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
    /**
     * Client user (web browser) can clear session(cookie),
     * so this limit can't really effect
     * 
     * @deprecated
     */
    @Override @Deprecated
    public void recordAction(String key, int period) {
        incrementAndGet(TRACE_ACTION_KEY + key, expire(period));
    }

    @Override @Deprecated
    public long countAction(String key) {
        CacheValue<Void> cache = get(TRACE_ACTION_KEY + key);
        return cache == null ? 0 : cache.count();
    }

    @Override @Deprecated
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
        synchronized (session) {
            CacheValue<?> cache = (CacheValue<?>) session.getAttribute(key);
            if (cache == null || cache.isExpire()) { // 失效则重置
                cache = new CacheValue<>(null, expireTimeMillis);
                session.setAttribute(key, cache);
            } else {
                cache.increment();
            }
            return cache;
        }
    }

    private void remove(String... keys) {
        for (String key : keys) {
            session.removeAttribute(key);
        }
    }

    private <T> CacheValue<T> getAndRemove(String key) {
        CacheValue<T> cache = (CacheValue<T>) session.getAttribute(key);
        if (cache == null) {
            return null;
        } else {
            session.removeAttribute(key);
            return cache.isExpire() ? null : cache;
        }
    }

    private <T> void add(String key, T value, int ttl) {
        session.setAttribute(key, new CacheValue<>(value, expire(ttl)));
    }

    private <T> CacheValue<T> get(String key) {
        CacheValue<T> cache = (CacheValue<T>) session.getAttribute(key);
        if (cache == null) {
            return null;
        } else if (cache.isExpire()) {
            session.removeAttribute(key);
            return null;
        } else {
            return cache;
        }
    }

}

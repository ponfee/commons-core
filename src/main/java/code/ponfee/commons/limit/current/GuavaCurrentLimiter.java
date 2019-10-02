package code.ponfee.commons.limit.current;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.common.util.concurrent.RateLimiter;

/**
 * The rate limiter based guava RateLimiter
 *
 * @author Ponfee
 */
public class GuavaCurrentLimiter implements CurrentLimiter {

    private static final ConcurrentMap<String, RateLimiter> LIMITER_MAP = new ConcurrentHashMap<>();

    @Override
    public boolean checkpoint(String key) {
        RateLimiter limiter = LIMITER_MAP.get(key);
        return limiter == null || limiter.tryAcquire();
    }

    @Override
    public boolean checkpoint(String key, long requestThreshold) {
        if (requestThreshold < 0) {
            return true; // 小于0表示无限制
        } else if (requestThreshold == 0) {
            return false; // 禁止访问
        }

        RateLimiter limiter = LIMITER_MAP.get(key);
        if (limiter == null) {
            synchronized (LIMITER_MAP) {
                if ((limiter = LIMITER_MAP.get(key)) == null) {
                    limiter = RateLimiter.create(requestThreshold);
                    LIMITER_MAP.put(key, limiter);
                }
            }
        }

        if (((Double) limiter.getRate()).longValue() != requestThreshold) {
            synchronized (limiter) {
                if (((Double) limiter.getRate()).longValue() != requestThreshold) {
                    limiter.setRate(requestThreshold);
                }
            }
        }
        return limiter.tryAcquire();
    }

    @Override
    public long countByRange(String key, Date from, Date to) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean setRequestThreshold(String key, long threshold) {
        if (threshold < -1) {
            LIMITER_MAP.remove(key);
            return true;
        }

        RateLimiter limiter = LIMITER_MAP.get(key);
        if (limiter == null) {
            synchronized (LIMITER_MAP) {
                if ((limiter = LIMITER_MAP.get(key)) == null) {
                    limiter = RateLimiter.create(threshold);
                    LIMITER_MAP.put(key, limiter);
                    return true;
                }
            }
        }

        limiter.setRate(threshold);
        return true;
    }

    @Override
    public long getRequestThreshold(String key) {
        RateLimiter limiter = LIMITER_MAP.get(key);
        if (limiter == null) {
            return -1;
        }
        return ((Double) limiter.getRate()).longValue();
    }

}

/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.limit.current;

import cn.ponfee.commons.util.SynchronizedCaches;
import com.google.common.util.concurrent.RateLimiter;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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

        RateLimiter limiter = SynchronizedCaches.get(key, LIMITER_MAP, () -> RateLimiter.create(requestThreshold));

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
    public void setRequestThreshold(String key, long threshold) {
        if (threshold < -1) {
            LIMITER_MAP.remove(key);
        }

        SynchronizedCaches.get(key, LIMITER_MAP, () -> RateLimiter.create(threshold)).setRate(threshold);
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

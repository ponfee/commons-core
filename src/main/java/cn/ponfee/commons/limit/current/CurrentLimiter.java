/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.limit.current;

import java.util.Date;

/**
 * 流量限制：限流器（隔板）
 * 
 * https://www.cnblogs.com/softidea/p/6229543.html
 *
 * @author Ponfee
 */
public interface CurrentLimiter {

    /**
     * 校验并追踪
     * @param key
     * @return
     */
    boolean checkpoint(String key);

    /**
     * 校验并追踪
     * @param key
     * @param requestThreshold
     * @return
     */
    boolean checkpoint(String key, long requestThreshold);

    /**
     * 按区间统计
     * @param key
     * @param from
     * @param to
     * @return
     */
    long countByRange(String key, Date from, Date to);

    /**
     * 设置一分钟（60s）的访问限制量
     * @param key
     * @param threshold
     * @return
     */
    void setRequestThreshold(String key, long threshold);

    /**
     * 获取配置的访问量
     * @param key
     * @return
     */
    long getRequestThreshold(String key);
}

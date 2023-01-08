/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.base;

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

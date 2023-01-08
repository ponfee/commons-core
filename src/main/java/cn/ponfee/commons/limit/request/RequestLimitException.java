/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.limit.request;

/**
 * 请求超限异常
 * @author Ponfee
 */
public class RequestLimitException extends Exception {

    private static final long serialVersionUID = 2493768018114069549L;

    /**
     * @param message 错误信息
     */
    public RequestLimitException(String message) {
        super(message);
    }

}

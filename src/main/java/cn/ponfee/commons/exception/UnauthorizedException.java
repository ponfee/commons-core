/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.exception;

import cn.ponfee.commons.model.ResultCode;

/**
 * Unauthorized exception definition
 *
 * @author Ponfee
 */
public class UnauthorizedException extends BaseUncheckedException {
    private static final long serialVersionUID = -5678901285130119481L;

    private static final int CODE = ResultCode.UNAUTHORIZED.getCode();

    public UnauthorizedException() {
        super(CODE, null, null);
    }

    public UnauthorizedException(String message) {
        super(CODE, message, null);
    }

    public UnauthorizedException(Throwable cause) {
        super(CODE, null, cause);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(CODE, message, cause);
    }

    public UnauthorizedException(String message,
                                 Throwable cause,
                                 boolean enableSuppression,
                                 boolean writableStackTrace) {
        super(CODE, message, cause, enableSuppression, writableStackTrace);
    }

}

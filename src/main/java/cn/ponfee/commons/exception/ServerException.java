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
 * Server exception definition
 *
 * @author Ponfee
 */
public class ServerException extends BaseUncheckedException {
    private static final long serialVersionUID = -247253152815744553L;

    private static final int CODE = ResultCode.SERVER_ERROR.getCode();

    public ServerException() {
        super(CODE, null, null);
    }

    public ServerException(String message) {
        super(CODE, message, null);
    }

    public ServerException(Throwable cause) {
        super(CODE, null, cause);
    }

    public ServerException(String message, Throwable cause) {
        super(CODE, message, cause);
    }

    public ServerException(String message,
                           Throwable cause,
                           boolean enableSuppression,
                           boolean writableStackTrace) {
        super(CODE, message, cause, enableSuppression, writableStackTrace);
    }

}

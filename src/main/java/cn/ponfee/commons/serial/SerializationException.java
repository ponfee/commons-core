/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.serial;

/**
 * 序例化异常类
 * 
 * @author Ponfee
 */
public class SerializationException extends RuntimeException {

    private static final long serialVersionUID = -5285807406910063551L;

    public SerializationException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public SerializationException(String msg) {
        super(msg);
    }

    public SerializationException(Throwable cause) {
        super(cause);
    }
}

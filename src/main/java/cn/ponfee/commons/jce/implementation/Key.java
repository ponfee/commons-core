/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.jce.implementation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Key interface
 * @author Ponfee
 */
public interface Key {

    Key readKey(InputStream in) throws IOException;

    void writeKey(OutputStream out) throws IOException;

    Key getPublic();

    boolean isPublic();
}

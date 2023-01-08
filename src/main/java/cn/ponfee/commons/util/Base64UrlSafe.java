/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.util;

import java.util.Base64;

/**
 * Base64 Url Safe
 * 
 * @author Ponfee
 */
public class Base64UrlSafe {

    public static String encode(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding()
                                     .encodeToString(data);
    }

    public static byte[] decode(String b64) {
        return Base64.getUrlDecoder().decode(b64);
    }

}

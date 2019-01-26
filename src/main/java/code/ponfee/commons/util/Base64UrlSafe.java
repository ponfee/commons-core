/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2018, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package code.ponfee.commons.util;

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

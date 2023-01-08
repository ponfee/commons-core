/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.util;

import java.awt.Color;

/**
 * color rgb and hex transform
 * 
 * @author Ponfee
 */
public final class Colors {

    public static Color fromHex(String hex) {
        if (hex == null || hex.isEmpty()) {
            return null;
        }
        return new Color(Integer.parseInt(hex.substring(1), 16));
    }

    public static String toHex(Color c) {
        return '#' + hex(c.getRed()) + hex(c.getGreen()) + hex(c.getBlue());
    }

    private static String hex(int i) {
        // StringUtils.leftPad(Integer.toHexString(i), 2, "0")
        return String.format("%02x", i);
    }

}

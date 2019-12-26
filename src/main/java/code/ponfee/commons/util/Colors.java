package code.ponfee.commons.util;

import java.awt.Color;

import org.apache.commons.lang3.StringUtils;

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
        if (c == null) {
            return null;
        }

        return '#' + toHex(c.getRed()) + toHex(c.getGreen()) + toHex(c.getBlue());
    }

    private static String toHex(int i) {
        return StringUtils.leftPad(Integer.toHexString(i), 2, "0");
    }
}

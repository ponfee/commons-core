package code.ponfee.commons.util;

import org.apache.commons.lang3.StringUtils;

/**
 * Extended org.springframework.util.Assert
 * 
 * @author Ponfee
 */
public class Asserts extends org.springframework.util.Assert {

    public static void notEmpty(String text, String msg) {
        if (StringUtils.isEmpty(text)) {
            throw new IllegalArgumentException(msg);
        }
    }

    public static void notBlank(String text, String msg) {
        if (StringUtils.isBlank(text)) {
            throw new IllegalArgumentException(msg);
        }
    }

    public static void minLen(String text, int min, String msg) {
        if (text != null && text.length() < min) {
            throw new IllegalArgumentException(msg);
        }
    }

    public static void maxLen(String text, int max, String msg) {
        if (text != null && text.length() > max) {
            throw new IllegalArgumentException(msg);
        }
    }

    public static void rangeLen(String text, int min, int max, String msg) {
        int len;
        if (text != null && ((len = text.length()) > max || len < min)) {
            throw new IllegalArgumentException(msg);
        }
    }

}

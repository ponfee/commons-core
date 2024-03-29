/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.util;

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

    public static void range(int value, int min, int max, String msg) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(msg);
        }
    }

    public static void rangeLen(String text, int min, int max, String msg) {
        if (text == null) {
            return;
        }
        range(text.length(), min, max, msg);
    }

}

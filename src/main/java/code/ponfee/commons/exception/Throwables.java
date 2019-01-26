package code.ponfee.commons.exception;

import code.ponfee.commons.io.StringPrintWriter;

/**
 * 异常工具类
 * @author Ponfee
 */
public final class Throwables {
    private Throwables() {}

    /**
     * get the throwable stack trace
     * @param e
     * @return
     */
    public static String getStackTrace(Throwable e) {
        if (e == null) {
            return null;
        }

        try (StringPrintWriter writer = new StringPrintWriter()) {
            e.printStackTrace(writer);
            return writer.getString();
        }
    }

    /**
     * ignore the throwable
     * @param ignored
     */
    public static void ignore(Throwable ignored) {
        ignore(ignored, true);
    }

    /**
     * ignore the throwable, if {@code console} is true then will be 
     * print the throwable stack trace to console
     * @param ignored
     * @param console
     */
    public static void ignore(Throwable ignored, boolean console) {
        if (console) {
            console(ignored);
        }
    }

    /**
     * Prints the throwable stack trace to console
     * 
     * @param t the Throwable
     */
    public static void console(Throwable t) {
        t.printStackTrace();
    }

}

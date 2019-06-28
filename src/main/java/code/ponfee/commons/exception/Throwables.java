package code.ponfee.commons.exception;

import code.ponfee.commons.io.StringPrintWriter;

/**
 * 异常工具类
 * 
 * @author Ponfee
 */
public final class Throwables {
    private Throwables() {}

    /**
     * Gets the throwable stack trace
     * 
     * @param e the Throwable
     * @return a string of throwable stack trace information
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
     * Ignore the throwable
     * 
     * @param ignored the Throwable
     */
    public static void ignore(Throwable ignored) {
        ignore(ignored, true);
    }

    /**
     * Ignore the throwable, if {@code console} is true then will be 
     * print the throwable stack trace to console
     * 
     * @param ignored the Throwable
     * @param console whether print console, {@code true} is print
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

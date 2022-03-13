package code.ponfee.commons.exception;

import code.ponfee.commons.io.StringPrintWriter;

/**
 * 异常工具类
 * 
 * @author Ponfee
 */
public final class Throwables {

    /**
     * Gets the throwable stack trace
     * 
     * @param throwable the Throwable
     * @return a string of throwable stack trace information
     */
    public static String getStackTrace(Throwable throwable) {
        if (throwable == null) {
            return null;
        }

        try (StringPrintWriter writer = new StringPrintWriter()) {
            throwable.printStackTrace(writer);
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
     * @param throwable the Throwable
     */
    public static void console(Throwable throwable) {
        throwable.printStackTrace();
    }

    public static void checked(Throwable throwable) {
        checked(throwable, null);
    }

    /**
     * Checked the throwable
     * 
     * @param throwable the throwable
     * @param msg the msg
     */
    public static void checked(Throwable throwable, String msg) {
        if (throwable instanceof RuntimeException) {
            throw (RuntimeException) throwable;
        } else {
            if (msg != null) {
                throw new RuntimeException(msg, throwable);
            } else {
                throw new RuntimeException(throwable);
            }
        }
    }

}

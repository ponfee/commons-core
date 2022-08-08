package code.ponfee.commons.exception;

import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * 异常工具类
 * 
 * @author Ponfee
 */
public final class Throwables {

    /**
     * Gets the root cause throwable stack trace
     *
     * @param throwable the throwable
     * @return a string of throwable stack trace information
     */
    public static String getRootCauseStackTrace(Throwable throwable) {
        //return ExceptionUtils.getStackTrace(ExceptionUtils.getRootCause(throwable));
        return String.join("\n", ExceptionUtils.getRootCauseStackTrace(throwable));
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

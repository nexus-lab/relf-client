package android.util;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.logging.Logger;

/**
 * Override Android {@link android.util.Log} in Java unit tests.
 *
 * @author Ruipeng Zhang
 */
public final class Log {
    private static final Logger LOGGER = Logger.getLogger("AndroidLog");

    /**
     * Send a DEBUG log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @return 0
     */
    public static int d(String tag, String msg) {
        LOGGER.finer("DEBUG: " + tag + ": " + msg);
        return 0;
    }

    /**
     * Send a DEBUG log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @return 0
     */
    public static int i(String tag, String msg) {
        LOGGER.fine("INFO: " + tag + ": " + msg);
        return 0;
    }

    /**
     * Send a DEBUG log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @return 0
     */
    public static int w(String tag, String msg) {
        LOGGER.warning("WARN: " + tag + ": " + msg);
        return 0;
    }

    /**
     * Send a DEBUG log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param e   The {@link Throwable} you would like logged.
     * @return 0
     */
    public static int w(String tag, Throwable e) {
        LOGGER.warning("WARN: " + tag);
        LOGGER.warning(ExceptionUtils.getStackTrace(e));
        return 0;
    }

    /**
     * Send a DEBUG log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param e   The {@link Throwable} you would like logged.
     * @return 0
     */
    public static int w(String tag, String msg, Throwable e) {
        LOGGER.warning("WARN: " + tag + ": " + msg);
        LOGGER.warning(ExceptionUtils.getStackTrace(e));
        return 0;
    }

    /**
     * Send a DEBUG log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @return 0
     */
    public static int e(String tag, String msg) {
        LOGGER.severe("ERROR: " + tag + ": " + msg);
        return 0;
    }

    /**
     * Send a DEBUG log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param e   The {@link Throwable} you would like logged.
     * @return 0
     */
    public static int e(String tag, Throwable e) {
        LOGGER.severe("ERROR: " + tag);
        LOGGER.severe(ExceptionUtils.getStackTrace(e));
        return 0;
    }

    /**
     * Send a DEBUG log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param e   The {@link Throwable} you would like logged.
     * @return 0
     */
    public static int e(String tag, String msg, Throwable e) {
        LOGGER.severe("ERROR: " + tag + ": " + msg);
        LOGGER.severe(ExceptionUtils.getStackTrace(e));
        return 0;
    }
}

package com.quill.appender;

import java.util.List;

/**
 * Factory for creating built-in appender instances.
 */
public final class Appenders {

    private Appenders() {
        // Static factory - prevent instantiation
    }

    // ========== Console Appenders ==========

    /**
     * Creates a console appender with human-readable output (no colors).
     *
     * @return a new console appender
     */
    public static Appender console() {
        return new ConsoleAppender(false);
    }

    /**
     * Creates a console appender with optional ANSI color support.
     *
     * @param useColor whether to enable ANSI colors for log levels
     * @return a new console appender
     */
    public static Appender console(boolean useColor) {
        return new ConsoleAppender(useColor);
    }

    /**
     * Creates a JSON console appender that outputs single-line JSON to stdout.
     *
     * @return a new JSON console appender
     */
    public static Appender jsonConsole() {
        return new JsonConsoleAppender();
    }

    // ========== File Append ==========

    /**
     * Creates a file appender that writes JSON logs to the specified file.
     * Creates a new file (overwrites existing).
     *
     * @param filePath the path to the log file
     * @return a new file appender
     */
    public static FileAppender file(String filePath) {
        return new FileAppender(filePath, false);
    }

    /**
     * Creates a file appender that writes JSON logs to the specified file.
     *
     * @param filePath     the path to the log file
     * @param appendToFile if true, append to existing file; if false, overwrite
     * @return a new file appender
     */
    public static FileAppender file(String filePath, boolean appendToFile) {
        return new FileAppender(filePath, appendToFile);
    }

    /**
     * Creates a rolling file appender with automatic rotation based on file size.
     * Uses a default max file size of 10MB.
     *
     * @param filePath the path to the log file
     * @return a new rolling file appender
     */
    public static RollingFileAppender rollingFile(String filePath) {
        return new RollingFileAppender(filePath);
    }

    /**
     * Creates a rolling file appender with the specified max file size.
     *
     * @param filePath the path to the log file
     * @param maxBytes the maximum size in bytes before rotation
     * @return a new rolling file appender
     */
    public static RollingFileAppender rollingFile(String filePath, long maxBytes) {
        return new RollingFileAppender(filePath, maxBytes);
    }

    // ========== Async Append ==========

    /**
     * Wraps an appender to run asynchronously with default queue size of 1000.
     * Log events are dropped when the queue is full.
     *
     * @param delegate the appender to wrap
     * @return an async appender wrapper
     */
    public static AsyncAppender async(Appender delegate) {
        return new AsyncAppender(delegate);
    }

    /**
     * Wraps an appender to run asynchronously.
     *
     * @param delegate   the appender to wrap
     * @param queueSize  the maximum number of events to queue
     * @param dropOnFull if true, drop events when queue is full; if false, block
     * @return an async appender wrapper
     */
    public static AsyncAppender async(Appender delegate, int queueSize, boolean dropOnFull) {
        return new AsyncAppender(delegate, queueSize, dropOnFull);
    }

    // ========== Appender Chaining ==========

    /**
     * Creates a composite appender that delegates to multiple appenders.
     * All appenders receive every log event.
     *
     * @param appenders the appenders to chain together
     * @return a composite appender
     */
    @SafeVarargs
    public static Appender chain(Appender... appenders) {
        return new CompositeAppender(appenders);
    }

    /**
     * Creates a composite appender from a list of appenders.
     *
     * @param appenders the list of appenders to chain together
     * @return a composite appender
     */
    public static Appender chain(List<Appender> appenders) {
        return new CompositeAppender(appenders);
    }
}

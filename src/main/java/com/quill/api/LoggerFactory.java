package com.quill.api;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory for creating and caching Logger instances.
 */
public final class LoggerFactory {

    private static final ConcurrentHashMap<String, Logger> LOGGERS = new ConcurrentHashMap<>();

    private LoggerFactory() {
        // Static factory - prevent instantiation
    }

    /**
     * Returns a logger for the given class.
     * The logger name will be the class's simple name.
     *
     * @param clazz the class to create a logger for
     * @return a logger instance
     */
    public static Logger get(Class<?> clazz) {
        Objects.requireNonNull(clazz, "clazz must not be null");
        return get(clazz.getSimpleName());
    }

    /**
     * Returns a logger with the given name.
     * Loggers are cached and reused for the same name.
     *
     * @param name the logger name
     * @return a logger instance
     */
    public static Logger get(String name) {
        Objects.requireNonNull(name, "name must not be null");
        return LOGGERS.computeIfAbsent(name, DefaultLogger::new);
    }

    /**
     * Clears the logger cache.
     * Primarily used for testing.
     */
    public static void reset() {
        LOGGERS.clear();
    }
}

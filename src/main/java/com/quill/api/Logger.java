package com.quill.api;

import com.quill.config.Logging;
import com.quill.model.Level;

/**
 * Logger interface providing fluent API for structured logging.
 */
public interface Logger {

    /**
     * Returns the name of this logger.
     *
     * @return the logger name
     */
    String name();

    // ========== Convenience Methods (auto-emit) ==========

    /**
     * Logs a TRACE message and immediately emits it.
     *
     * @param message the log message
     */
    default void traceEmit(String message) {
        trace(message).emit();
    }

    /**
     * Logs a DEBUG message and immediately emits it.
     *
     * @param message the log message
     */
    default void debugEmit(String message) {
        debug(message).emit();
    }

    /**
     * Logs an INFO message and immediately emits it.
     *
     * @param message the log message
     */
    default void infoEmit(String message) {
        info(message).emit();
    }

    /**
     * Logs a WARN message and immediately emits it.
     *
     * @param message the log message
     */
    default void warnEmit(String message) {
        warn(message).emit();
    }

    /**
     * Logs an ERROR message and immediately emits it.
     *
     * @param message the log message
     */
    default void errorEmit(String message) {
        error(message).emit();
    }

    /**
     * Logs a TRACE message with one field and immediately emits it.
     *
     * @param message the log message
     * @param key     the field key
     * @param value   the field value
     */
    default void trace(String message, String key, Object value) {
        trace(message).field(key, value).emit();
    }

    /**
     * Logs a DEBUG message with one field and immediately emits it.
     *
     * @param message the log message
     * @param key     the field key
     * @param value   the field value
     */
    default void debug(String message, String key, Object value) {
        debug(message).field(key, value).emit();
    }

    /**
     * Logs an INFO message with one field and immediately emits it.
     *
     * @param message the log message
     * @param key     the field key
     * @param value   the field value
     */
    default void info(String message, String key, Object value) {
        info(message).field(key, value).emit();
    }

    /**
     * Logs a WARN message with one field and immediately emits it.
     *
     * @param message the log message
     * @param key     the field key
     * @param value   the field value
     */
    default void warn(String message, String key, Object value) {
        warn(message).field(key, value).emit();
    }

    /**
     * Logs an ERROR message with one field and immediately emits it.
     *
     * @param message the log message
     * @param key     the field key
     * @param value   the field value
     */
    default void error(String message, String key, Object value) {
        error(message).field(key, value).emit();
    }

    /**
     * Logs a TRACE message with multiple fields and immediately emits it.
     *
     * @param message       the log message
     * @param keyValuePairs alternating key-value pairs (must be even length)
     */
    default void trace(String message, Object... keyValuePairs) {
        trace(message).fields(keyValuePairs).emit();
    }

    /**
     * Logs a DEBUG message with multiple fields and immediately emits it.
     *
     * @param message       the log message
     * @param keyValuePairs alternating key-value pairs (must be even length)
     */
    default void debug(String message, Object... keyValuePairs) {
        debug(message).fields(keyValuePairs).emit();
    }

    /**
     * Logs an INFO message with multiple fields and immediately emits it.
     *
     * @param message       the log message
     * @param keyValuePairs alternating key-value pairs (must be even length)
     */
    default void info(String message, Object... keyValuePairs) {
        info(message).fields(keyValuePairs).emit();
    }

    /**
     * Logs a WARN message with multiple fields and immediately emits it.
     *
     * @param message       the log message
     * @param keyValuePairs alternating key-value pairs (must be even length)
     */
    default void warn(String message, Object... keyValuePairs) {
        warn(message).fields(keyValuePairs).emit();
    }

    /**
     * Logs an ERROR message with multiple fields and immediately emits it.
     *
     * @param message       the log message
     * @param keyValuePairs alternating key-value pairs (must be even length)
     */
    default void error(String message, Object... keyValuePairs) {
        error(message).fields(keyValuePairs).emit();
    }

    // ========== Fluent API (requires .emit() or use convenience methods above) ==========

    /**
     * Creates a builder for a TRACE level log event.
     *
     * @param message the log message
     * @return a log builder for adding fields and emitting
     */
    default LogBuilder trace(String message) {
        return log(Level.TRACE, message);
    }

    /**
     * Creates a builder for a DEBUG level log event.
     *
     * @param message the log message
     * @return a log builder for adding fields and emitting
     */
    default LogBuilder debug(String message) {
        return log(Level.DEBUG, message);
    }

    /**
     * Creates a builder for an INFO level log event.
     *
     * @param message the log message
     * @return a log builder for adding fields and emitting
     */
    default LogBuilder info(String message) {
        return log(Level.INFO, message);
    }

    /**
     * Creates a builder for a WARN level log event.
     *
     * @param message the log message
     * @return a log builder for adding fields and emitting
     */
    default LogBuilder warn(String message) {
        return log(Level.WARN, message);
    }

    /**
     * Creates a builder for an ERROR level log event.
     *
     * @param message the log message
     * @return a log builder for adding fields and emitting
     */
    default LogBuilder error(String message) {
        return log(Level.ERROR, message);
    }

    /**
     * Creates a builder for a log event at the specified level.
     *
     * @param level   the log level
     * @param message the log message
     * @return a log builder for adding fields and emitting
     */
    LogBuilder log(Level level, String message);

    /**
     * Checks if the given level is enabled for this logger.
     *
     * @param level the level to check
     * @return true if the level is enabled
     */
    default boolean isEnabled(Level level) {
        return level.isEnabled(Logging.getConfig().minLevel());
    }
}

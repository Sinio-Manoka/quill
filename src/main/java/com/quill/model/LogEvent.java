package com.quill.model;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable record representing a single log event.
 * Captures all contextual information at the moment of log emission.
 */
public record LogEvent(
        Instant timestamp,
        Level level,
        String message,
        Map<String, Object> fields,
        Map<String, String> context,
        String threadName,
        String loggerName
) {

    /**
     * Creates a new LogEvent with the current timestamp.
     * Defensive copies are made of the fields and context maps.
     *
     * @param level      the log level
     * @param message    the log message
     * @param fields     the structured fields (key-value pairs)
     * @param context    the contextual data (e.g., requestId, userId)
     * @param threadName the name of the thread emitting the log
     * @param loggerName the name of the logger
     */
    public LogEvent(Level level, String message, Map<String, Object> fields,
                    Map<String, String> context, String threadName, String loggerName) {
        this(Instant.now(), level, message, copyFields(fields), copyContext(context), threadName, loggerName);
    }

    /**
     * Canonical constructor with explicit timestamp.
     * Defensive copies are made of the fields and context maps.
     */
    public LogEvent(Instant timestamp, Level level, String message, Map<String, Object> fields,
                    Map<String, String> context, String threadName, String loggerName) {
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp must not be null");
        this.level = Objects.requireNonNull(level, "level must not be null");
        this.message = Objects.requireNonNull(message, "message must not be null");
        this.fields = fields == null ? Map.of() : copyFields(fields);
        this.context = context == null ? Map.of() : copyContext(context);
        this.threadName = Objects.requireNonNullElse(threadName, Thread.currentThread().getName());
        this.loggerName = Objects.requireNonNull(loggerName, "loggerName must not be null");
    }

    /**
     * Returns an unmodifiable view of the fields map.
     */
    @Override
    public Map<String, Object> fields() {
        return Collections.unmodifiableMap(fields);
    }

    /**
     * Returns an unmodifiable view of the context map.
     */
    @Override
    public Map<String, String> context() {
        return Collections.unmodifiableMap(context);
    }

    private static Map<String, Object> copyFields(Map<String, Object> fields) {
        if (fields == null || fields.isEmpty()) {
            return Map.of();
        }
        return new LinkedHashMap<>(fields);
    }

    private static Map<String, String> copyContext(Map<String, String> context) {
        if (context == null || context.isEmpty()) {
            return Map.of();
        }
        return new LinkedHashMap<>(context);
    }
}

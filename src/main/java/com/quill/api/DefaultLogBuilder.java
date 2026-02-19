package com.quill.api;

import com.quill.context.LogContext;
import com.quill.model.Level;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Default implementation of LogBuilder.
 */
class DefaultLogBuilder implements LogBuilder {

    private final Level level;
    private final String message;
    private final DefaultLogger logger;
    private final Map<String, Object> fields;
    private Map<String, String> context;

    DefaultLogBuilder(Level level, String message, DefaultLogger logger) {
        this.level = Objects.requireNonNull(level, "level must not be null");
        this.message = Objects.requireNonNull(message, "message must not be null");
        this.logger = Objects.requireNonNull(logger, "logger must not be null");
        this.fields = new LinkedHashMap<>();
        // Capture context from LogContext at creation time
        this.context = convertContext(LogContext.current());
    }

    @Override
    public LogBuilder field(String key, Object value) {
        Objects.requireNonNull(key, "field key must not be null");
        fields.put(key, value);
        return this;
    }

    @Override
    public void emit() {
        // Only emit if this level is enabled
        if (logger.isEnabled(level)) {
            // Re-capture context in case it changed since builder creation
            this.context = convertContext(LogContext.current());
            logger.emit(level, message, fields, context);
        }
    }

    /**
     * Converts Object values to String values for context.
     */
    private Map<String, String> convertContext(Map<String, Object> ctx) {
        if (ctx == null || ctx.isEmpty()) {
            return Map.of();
        }
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : ctx.entrySet()) {
            Object value = entry.getValue();
            String strValue = (value == null) ? "null" : String.valueOf(value);
            result.put(entry.getKey(), strValue);
        }
        return result;
    }
}

package com.quill.api;

/**
 * Fluent builder interface for constructing log events.
 * Returned by Logger level methods (trace, debug, info, warn, error).
 */
public interface LogBuilder {

    /**
     * Adds a structured field to the log event.
     *
     * @param key   the field name
     * @param value the field value (can be any Object)
     * @return this builder for chaining
     */
    LogBuilder field(String key, Object value);

    /**
     * Adds multiple structured fields to the log event from alternating key-value pairs.
     *
     * @param keyValuePairs alternating key-value pairs (keys must be Strings, must be even length)
     * @return this builder for chaining
     */
    default LogBuilder fields(Object... keyValuePairs) {
        if (keyValuePairs == null || keyValuePairs.length == 0) {
            return this;
        }
        if (keyValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException(
                    STR."keyValuePairs must have even length, got \{keyValuePairs.length}");
        }
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            String key = String.valueOf(keyValuePairs[i]);
            Object value = keyValuePairs[i + 1];
            field(key, value);
        }
        return this;
    }

    /**
     * Emits the log event to all configured appenders.
     * This is a terminal operation - after calling emit(), the builder should not be reused.
     */
    void emit();
}

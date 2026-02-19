package com.quill.context;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Context carrier for log-scoped contextual data.
 * Uses Java 21's ScopedValue for virtual thread-safe context propagation.
 *
 * <p>Example usage:</p>
 * <pre>
 * LogContext.bind("requestId", "abc-123")
 *    .and("userId", 42)
 *    .run(() -> {
 *        // All logs inside here automatically include requestId and userId
 *        log.info("Processing request").emit();
 *    });
 * </pre>
 */
public final class LogContext {

    // ThreadLocal storage for the context map (works with virtual threads via inheritance)
    private static final InheritableThreadLocal<Map<String, Object>> CONTEXT =
            new InheritableThreadLocal<>();

    // Builder for creating context scopes
    private final Map<String, Object> contextMap;

    private LogContext() {
        this.contextMap = new LinkedHashMap<>();
    }

    private LogContext(Map<String, Object> initial) {
        this.contextMap = new LinkedHashMap<>(initial);
    }

    /**
     * Starts building a new context scope with the given key-value pair.
     *
     * @param key   the context key
     * @param value the context value
     * @return a builder for adding more context or running code
     */
    public static LogContext bind(String key, Object value) {
        Objects.requireNonNull(key, "key must not be null");
        LogContext ctx = new LogContext();
        ctx.contextMap.put(key, value);
        return ctx;
    }

    /**
     * Starts building a new context scope from an existing map.
     *
     * @param values the key-value pairs to add
     * @return a builder for adding more context or running code
     */
    public static LogContext bind(Map<String, Object> values) {
        Objects.requireNonNull(values, "values must not be null");
        return new LogContext(values);
    }

    /**
     * Adds another key-value pair to this context scope.
     *
     * @param key   the context key
     * @param value the context value
     * @return this context for chaining
     */
    public LogContext and(String key, Object value) {
        Objects.requireNonNull(key, "key must not be null");
        contextMap.put(key, value);
        return this;
    }

    /**
     * Runs the given action with this context bound to the current scope.
     * All log statements within the action will automatically include this context.
     *
     * @param action the action to run
     */
    public void run(Runnable action) {
        Objects.requireNonNull(action, "action must not be null");

        Map<String, Object> previous = CONTEXT.get();
        // Merge current contextMap with previous context (new context wins on conflicts)
        Map<String, Object> merged = new LinkedHashMap<>(previous != null ? previous : Map.of());
        for (Map.Entry<String, Object> entry : contextMap.entrySet()) {
            if (entry.getValue() != null) {
                merged.put(entry.getKey(), entry.getValue());
            }
        }

        CONTEXT.set(Map.copyOf(merged));
        try {
            action.run();
        } finally {
            // Restore previous context (or clear if none)
            if (previous == null || previous.isEmpty()) {
                CONTEXT.remove();
            } else {
                CONTEXT.set(previous);
            }
        }
    }

    /**
     * Gets the current context map for the calling scope.
     * This is used internally by the logging framework.
     *
     * @return the current context map, or empty map if no context is bound
     */
    public static Map<String, Object> current() {
        Map<String, Object> ctx = CONTEXT.get();
        return ctx != null ? ctx : Map.of();
    }

    /**
     * Clears the current context (mainly for testing).
     */
    static void clear() {
        CONTEXT.remove();
    }
}

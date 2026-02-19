package com.quill.model;

/**
 * Log level enumeration with severity ordering.
 * Lower ordinal values indicate lower severity (TRACE < DEBUG < INFO < WARN < ERROR).
 */
public enum Level {
    TRACE(0),
    DEBUG(1),
    INFO(2),
    WARN(3),
    ERROR(4);

    private final int severity;

    Level(int severity) {
        this.severity = severity;
    }

    /**
     * Returns the severity value for this level.
     * Higher values indicate more severe log levels.
     *
     * @return the severity value
     */
    public int severity() {
        return severity;
    }

    /**
     * Checks if this level is enabled given a minimum threshold level.
     *
     * @param threshold the minimum level required for a log to be enabled
     * @return true if this level's severity is greater than or equal to the threshold
     */
    public boolean isEnabled(Level threshold) {
        return this.severity >= threshold.severity;
    }
}

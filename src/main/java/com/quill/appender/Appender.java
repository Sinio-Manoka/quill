package com.quill.appender;

import com.quill.model.LogEvent;

/**
 * Functional interface for log appenders.
 * Appenders are responsible for outputting log events to a destination.
 */
@FunctionalInterface
public interface Appender {

    /**
     * Appends a log event to the output destination.
     *
     * @param event the log event to append
     */
    void append(LogEvent event);
}

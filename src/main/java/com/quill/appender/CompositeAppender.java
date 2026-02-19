package com.quill.appender;

import com.quill.model.LogEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Composite appender that delegates to multiple appenders.
 * Useful for sending logs to multiple destinations simultaneously.
 * <p>
 * Example: send JSON to stdout AND to a file
 * <pre>
 * Appender composite = Appenders.chain(
 *     Appenders.jsonConsole(),
 *     Appenders.file("logs/app.log")
 * );
 * </pre>
 */
public class CompositeAppender implements Appender {

    private final List<Appender> appenders;

    /**
     * Creates a composite appender with the given delegates.
     *
     * @param appenders the appenders to delegate to
     */
    @SafeVarargs
    public CompositeAppender(Appender... appenders) {
        this(Arrays.asList(Objects.requireNonNull(appenders, "appenders must not be null")));
    }

    /**
     * Creates a composite appender with the given delegates.
     *
     * @param appenders the list of appenders to delegate to
     */
    public CompositeAppender(List<Appender> appenders) {
        this.appenders = new ArrayList<>(Objects.requireNonNull(appenders, "appenders must not be null"));
        if (this.appenders.isEmpty()) {
            throw new IllegalArgumentException("At least one appender must be provided");
        }
    }

    @Override
    public void append(LogEvent event) {
        for (Appender appender : appenders) {
            try {
                appender.append(event);
            } catch (Exception e) {
                // Don't let one failing appender break others
                System.err.println(STR."Error in composite appender delegate: \{e.getMessage()}");
            }
        }
    }

    /**
     * Returns the list of delegate appenders.
     *
     * @return an unmodifiable list of appenders
     */
    public List<Appender> getDelegates() {
        return List.copyOf(appenders);
    }

    /**
     * Adds an appender to this composite.
     *
     * @param appender the appender to add
     * @return this composite for chaining
     */
    public CompositeAppender add(Appender appender) {
        appenders.add(Objects.requireNonNull(appender, "appender must not be null"));
        return this;
    }
}

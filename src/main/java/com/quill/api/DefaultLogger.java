package com.quill.api;

import com.quill.appender.Appender;
import com.quill.config.Logging;
import com.quill.model.Level;
import com.quill.model.LogEvent;

import java.time.Instant;
import java.util.Map;
import java.util.Random;

/**
 * Default implementation of the Logger interface.
 */
class DefaultLogger implements Logger {

    private final String name;
    private final String packageName;
    private static final Random RANDOM = new Random();

    DefaultLogger(String name) {
        this.name = name;
        this.packageName = extractPackageName(name);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public LogBuilder log(Level level, String message) {
        return new DefaultLogBuilder(level, message, this);
    }

    /**
     * Checks if the given log level is enabled for this logger.
     * Takes into account global level, package-level configuration, and sampling.
     *
     * @param level the log level to check
     * @return true if logs at this level would be emitted
     */
    @Override
    public boolean isEnabled(Level level) {
        var config = Logging.getConfig();

        // Check package-level configuration first
        Level packageLevel = config.getLevelForPackage(packageName);
        Level effectiveLevel = (packageLevel != null) ? packageLevel : config.minLevel();

        if (level.severity() < effectiveLevel.severity()) {
            return false;
        }

        // Check sampling for TRACE and DEBUG
        return !config.isSamplingEnabled() || !shouldSample(level);
    }

    /**
     * Determines if a log event should be sampled (skipped).
     * Only applies to TRACE and DEBUG levels.
     *
     * @param level the log level
     * @return true if the log should be sampled (skipped)
     */
    private boolean shouldSample(Level level) {
        // Only sample TRACE and DEBUG
        if (level != Level.TRACE && level != Level.DEBUG) {
            return false;
        }

        double samplingRate = Logging.getConfig().samplingRate();
        // samplingRate of 0.1 means 10% of logs are kept (90% sampled/skipped)
        // So we return true (should skip) if random() > samplingRate
        return RANDOM.nextDouble() > samplingRate;
    }

    /**
     * Emits a log event to all configured appenders (without context).
     */
    void emit(Level level, String message, Map<String, Object> fields) {
        emit(level, message, fields, Map.of());
    }

    /**
     * Emits a log event to all configured appenders with context.
     */
    void emit(Level level, String message, Map<String, Object> fields, Map<String, String> context) {
        // Double-check in case config changed between isEnabled and emit
        if (!isEnabled(level)) {
            return;
        }

        LogEvent event = new LogEvent(
                Instant.now(),
                level,
                message,
                fields,
                context,
                Thread.currentThread().getName(),
                this.name
        );

        for (Appender appender : Logging.getConfig().appenders()) {
            appender.append(event);
        }
    }

    /**
     * Extracts the package name from a logger name.
     * For class names like "com.example.MyClass", returns "com.example".
     * For simple names like "MyClass", returns "".
     */
    private String extractPackageName(String loggerName) {
        if (loggerName == null || loggerName.isEmpty()) {
            return "";
        }

        // Check if it looks like a fully qualified class name
        int lastDot = loggerName.lastIndexOf('.');
        if (lastDot > 0) {
            // Extract package (everything before the last dot)
            return loggerName.substring(0, lastDot);
        }

        // Not a fully qualified name - no package
        return "";
    }
}

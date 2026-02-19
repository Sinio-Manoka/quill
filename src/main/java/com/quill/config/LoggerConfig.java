package com.quill.config;

import com.quill.appender.Appender;
import com.quill.model.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder for creating LogConfig instances.
 */
public class LoggerConfig {

    private Level minLevel = Level.INFO;
    private final List<Appender> appenders = new ArrayList<>();
    private boolean async = false;
    private double samplingRate = 1.0;
    private final Map<String, Level> packageLevels = new HashMap<>();

    public LoggerConfig() {
        // Default constructor
    }

    /**
     * Sets the minimum log level.
     * Logs below this level will be ignored.
     *
     * @param level the minimum level
     * @return this builder
     */
    public LoggerConfig level(Level level) {
        this.minLevel = level;
        return this;
    }

    /**
     * Adds an appender to the configuration.
     *
     * @param appender the appender to add
     * @return this builder
     */
    public LoggerConfig appender(Appender appender) {
        this.appenders.add(appender);
        return this;
    }

    /**
     * Sets whether logging should be asynchronous.
     * (Not yet implemented in Phase 1 - reserved for future)
     *
     * @param async true for async logging
     * @return this builder
     */
    public LoggerConfig async(boolean async) {
        this.async = async;
        return this;
    }

    /**
     * Sets the sampling rate for TRACE and DEBUG level logs.
     * A value of 0.1 means 10% of debug logs will be emitted.
     * Sampling only applies to TRACE and DEBUG levels; INFO and above are always logged.
     *
     * @param rate the sampling rate (0.0 to 1.0)
     * @return this builder
     * @throws IllegalArgumentException if rate is not between 0.0 and 1.0
     */
    public LoggerConfig sampling(double rate) {
        if (rate < 0.0 || rate > 1.0) {
            throw new IllegalArgumentException("samplingRate must be between 0.0 and 1.0");
        }
        this.samplingRate = rate;
        return this;
    }

    /**
     * Sets a specific log level for a package.
     * This overrides the global minLevel for loggers in this package.
     *
     * @param packageName the package name (e.g., "com.example.service")
     * @param level       the level for this package
     * @return this builder
     */
    public LoggerConfig packageLevel(String packageName, Level level) {
        this.packageLevels.put(packageName, level);
        return this;
    }

    /**
     * Sets multiple package-level log levels at once.
     *
     * @param levels map of package names to levels
     * @return this builder
     */
    public LoggerConfig packageLevels(Map<String, Level> levels) {
        if (levels != null) {
            this.packageLevels.putAll(levels);
        }
        return this;
    }

    /**
     * Builds the immutable LogConfig.
     *
     * @return the configuration
     */
    public LogConfig build() {
        if (appenders.isEmpty()) {
            // Default to JSON console appender if none specified
            appenders.add(new com.quill.appender.JsonConsoleAppender());
        }
        return new LogConfig(minLevel, appenders, async, samplingRate, packageLevels);
    }
}

package com.quill.config;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Static entry point for configuring the logging system.
 */
public final class Logging {

    private static final AtomicReference<LogConfig> config = new AtomicReference<>(LogConfig.defaultConfig());

    private Logging() {
        // Static utility - prevent instantiation
    }

    /**
     * Configures the logging system using the provided consumer.
     *
     * @param configurator a consumer that configures the LoggerConfig builder
     */
    public static void configure(Consumer<LoggerConfig> configurator) {
        LoggerConfig builder = new LoggerConfig();
        configurator.accept(builder);
        config.set(builder.build());
    }

    /**
     * Returns the current logging configuration.
     *
     * @return the current configuration
     */
    public static LogConfig getConfig() {
        return config.get();
    }

    /**
     * Resets the logging configuration to defaults.
     * Primarily used for testing.
     */
    public static void reset() {
        config.set(LogConfig.defaultConfig());
    }
}

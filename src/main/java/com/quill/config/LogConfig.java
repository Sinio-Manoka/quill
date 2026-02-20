package com.quill.config;

import com.quill.appender.Appender;
import com.quill.model.Level;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable configuration for the logging system.
 */
public record LogConfig(
        Level minLevel,
        List<Appender> appenders,
        boolean async,
        double samplingRate,
        Map<String, Level> packageLevels
) {
    private static final double DEFAULT_SAMPLING_RATE = 1.0; // 100% = no sampling

    public LogConfig {
        Objects.requireNonNull(minLevel, "minLevel must not be null");
        appenders = List.copyOf(Objects.requireNonNull(appenders, "appenders must not be null"));
        if (samplingRate < 0.0 || samplingRate > 1.0) {
            throw new IllegalArgumentException("samplingRate must be between 0.0 and 1.0");
        }
        packageLevels = packageLevels == null ? Map.of() : Map.copyOf(packageLevels);
    }

    /**
     * Returns the default configuration.
     * - minLevel: INFO
     * - appenders: JsonConsoleAppender
     * - async: false
     * - samplingRate: 1.0 (no sampling)
     * - packageLevels: empty
     *
     * @return the default configuration
     */
    public static LogConfig defaultConfig() {
        return new LogConfig(
                Level.INFO,
                List.of(new com.quill.appender.JsonConsoleAppender()),
                false,
                DEFAULT_SAMPLING_RATE,
                Map.of()
        );
    }

    /**
     * Checks if sampling is enabled (rate &lt; 1.0).
     */
    public boolean isSamplingEnabled() {
        return samplingRate < 1.0;
    }

    /**
     * Checks if a given package has a specific level configured.
     *
     * @param packageName the package name to check
     * @return the configured level, or null if not configured
     */
    public Level getLevelForPackage(String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            return null;
        }
        // Check for exact match first
        if (packageLevels.containsKey(packageName)) {
            return packageLevels.get(packageName);
        }
        // Check for parent package match (e.g., "com.example" matches "com.example.sub")
        String[] parts = packageName.split("\\.");
        StringBuilder current = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            if (packageLevels.containsKey(current.toString())) {
                return packageLevels.get(current.toString());
            }
            current.append(".").append(parts[i]);
        }
        return null;
    }
}

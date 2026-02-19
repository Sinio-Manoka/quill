package com.quill.config;

import com.quill.appender.Appender;
import com.quill.appender.ConsoleAppender;
import com.quill.appender.JsonConsoleAppender;
import com.quill.model.Level;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the LoggerConfig builder class.
 */
@DisplayName("LoggerConfig Builder Tests")
class LoggerConfigTest {

    @Test
    @DisplayName("Default builder values")
    void defaultBuilderValues() {
        LoggerConfig builder = new LoggerConfig();
        LogConfig config = builder.build();

        assertEquals(Level.INFO, config.minLevel());
        assertEquals(1, config.appenders().size());
        assertInstanceOf(JsonConsoleAppender.class, config.appenders().getFirst());
        assertFalse(config.async());
    }

    @Test
    @DisplayName("level sets the minimum level")
    void levelSetsMinimumLevel() {
        LoggerConfig builder = new LoggerConfig();
        LogConfig config = builder.level(Level.DEBUG).build();

        assertEquals(Level.DEBUG, config.minLevel());
    }

    @Test
    @DisplayName("async enables async logging")
    void asyncEnablesAsyncLogging() {
        LoggerConfig builder = new LoggerConfig();
        LogConfig config = builder.async(true).build();

        assertTrue(config.async());
    }

    @Test
    @DisplayName("appender adds an appender")
    void appenderAddsAppender() {
        LoggerConfig builder = new LoggerConfig();
        Appender customAppender = new ConsoleAppender(false);

        LogConfig config = builder.appender(customAppender).build();

        // When custom appenders are added, only those are included (default is not added)
        assertEquals(1, config.appenders().size());
        assertTrue(config.appenders().contains(customAppender));
    }

    @Test
    @DisplayName("Multiple appenders can be added")
    void multipleAppendersCanBeAdded() {
        LoggerConfig builder = new LoggerConfig();
        Appender appender1 = new ConsoleAppender(false);
        Appender appender2 = new JsonConsoleAppender();

        LogConfig config = builder
                .appender(appender1)
                .appender(appender2)
                .build();

        // When custom appenders are added, only those are included
        assertEquals(2, config.appenders().size());
        assertTrue(config.appenders().contains(appender1));
        assertTrue(config.appenders().contains(appender2));
    }

    @Test
    @DisplayName("Builder methods can be chained")
    void builderMethodsCanBeChained() {
        LoggerConfig builder = new LoggerConfig();
        Appender appender = new ConsoleAppender(false);

        LogConfig config = builder
                .level(Level.WARN)
                .async(true)
                .appender(appender)
                .build();

        assertEquals(Level.WARN, config.minLevel());
        assertTrue(config.async());
        assertTrue(config.appenders().contains(appender));
    }

    @Test
    @DisplayName("build can be called multiple times")
    void buildCanBeCalledMultipleTimes() {
        LoggerConfig builder = new LoggerConfig();

        LogConfig config1 = builder.level(Level.DEBUG).build();
        LogConfig config2 = builder.level(Level.ERROR).build();

        assertEquals(Level.DEBUG, config1.minLevel());
        assertEquals(Level.ERROR, config2.minLevel());
        // Each build should create independent config
        assertNotSame(config1, config2);
    }

    @Test
    @DisplayName("Built config is immutable")
    void builtConfigIsImmutable() {
        LoggerConfig builder = new LoggerConfig();
        LogConfig config = builder.build();

        assertThrows(UnsupportedOperationException.class,
                () -> config.appenders().add(new ConsoleAppender()));
    }

    @Test
    @DisplayName("packageLevels adds multiple package levels")
    void packageLevelsAddsMultiplePackageLevels() {
        LoggerConfig builder = new LoggerConfig();
        LogConfig config = builder
                .packageLevels(Map.of(
                        "com.example", Level.DEBUG,
                        "org.test", Level.WARN
                ))
                .build();

        assertEquals(Level.DEBUG, config.getLevelForPackage("com.example"));
        assertEquals(Level.WARN, config.getLevelForPackage("org.test"));
    }

    @Test
    @DisplayName("packageLevels with null map does not throw")
    void packageLevelsWithNullMapDoesNotThrow() {
        LoggerConfig builder = new LoggerConfig();
        // The implementation handles null gracefully
        assertDoesNotThrow(() -> builder.packageLevels(null).build());
    }

    @Test
    @DisplayName("sampling sets sampling rate")
    void samplingSetsSamplingRate() {
        LoggerConfig builder = new LoggerConfig();
        LogConfig config = builder.sampling(0.1).build();

        assertEquals(0.1, config.samplingRate());
        assertTrue(config.isSamplingEnabled());
    }

    @Test
    @DisplayName("sampling with negative rate throws exception")
    void samplingWithNegativeRateThrowsException() {
        LoggerConfig builder = new LoggerConfig();

        assertThrows(IllegalArgumentException.class,
                () -> builder.sampling(-0.1));
    }

    @Test
    @DisplayName("sampling with rate > 1.0 throws exception")
    void samplingWithRateAboveOneThrowsException() {
        LoggerConfig builder = new LoggerConfig();

        assertThrows(IllegalArgumentException.class,
                () -> builder.sampling(1.5));
    }

    @Test
    @DisplayName("sampling with rate = 0.0 is valid")
    void samplingWithZeroRateIsValid() {
        LoggerConfig builder = new LoggerConfig();
        LogConfig config = builder.sampling(0.0).build();

        assertEquals(0.0, config.samplingRate());
    }

    @Test
    @DisplayName("packageLevel adds single package level")
    void packageLevelAddsSinglePackageLevel() {
        LoggerConfig builder = new LoggerConfig();
        LogConfig config = builder
                .packageLevel("com.example", Level.TRACE)
                .build();

        assertEquals(Level.TRACE, config.getLevelForPackage("com.example"));
    }

    @Test
    @DisplayName("build with no appenders adds default JsonConsoleAppender")
    void buildWithNoAppendersAddsDefault() {
        LoggerConfig builder = new LoggerConfig();
        LogConfig config = builder.build();

        assertEquals(1, config.appenders().size());
        assertInstanceOf(JsonConsoleAppender.class, config.appenders().getFirst());
    }
}

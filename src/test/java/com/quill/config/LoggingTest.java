package com.quill.config;

import com.quill.appender.Appender;
import com.quill.appender.ConsoleAppender;
import com.quill.appender.JsonConsoleAppender;
import com.quill.model.Level;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Logging configuration class.
 */
@DisplayName("Logging Configuration Tests")
class LoggingTest {

    @AfterEach
    void resetConfig() {
        Logging.reset();
    }

    @Test
    @DisplayName("Default configuration has INFO level")
    void defaultConfigHasInfoLevel() {
        LogConfig config = Logging.getConfig();
        assertEquals(Level.INFO, config.minLevel());
    }

    @Test
    @DisplayName("Default configuration has JsonConsoleAppender")
    void defaultConfigHasJsonConsoleAppender() {
        LogConfig config = Logging.getConfig();
        assertEquals(1, config.appenders().size());
        assertInstanceOf(JsonConsoleAppender.class, config.appenders().getFirst());
    }

    @Test
    @DisplayName("Default configuration is not async")
    void defaultConfigIsNotAsync() {
        LogConfig config = Logging.getConfig();
        assertFalse(config.async());
    }

    @Test
    @DisplayName("configure updates the configuration")
    void configureUpdatesConfiguration() {
        Logging.configure(config -> config
                .level(Level.DEBUG)
                .async(true)
        );

        LogConfig config = Logging.getConfig();
        assertEquals(Level.DEBUG, config.minLevel());
        assertTrue(config.async());
    }

    @Test
    @DisplayName("configure with appender")
    void configureWithAppender() {
        Appender customAppender = new ConsoleAppender(true);

        Logging.configure(config -> config
                .level(Level.WARN)
                .appender(customAppender)
        );

        LogConfig config = Logging.getConfig();
        assertEquals(Level.WARN, config.minLevel());
        assertEquals(1, config.appenders().size());
        assertInstanceOf(ConsoleAppender.class, config.appenders().getFirst());
    }

    @Test
    @DisplayName("configure with multiple appenders")
    void configureWithMultipleAppenders() {
        Appender appender1 = new ConsoleAppender(false);
        Appender appender2 = new JsonConsoleAppender();

        Logging.configure(config -> config
                .appender(appender1)
                .appender(appender2)
        );

        LogConfig config = Logging.getConfig();
        assertEquals(2, config.appenders().size());
    }

    @Test
    @DisplayName("configure can be called multiple times")
    void configureCanBeCalledMultipleTimes() {
        Logging.configure(config -> config.level(Level.TRACE));
        assertEquals(Level.TRACE, Logging.getConfig().minLevel());

        Logging.configure(config -> config.level(Level.ERROR));
        assertEquals(Level.ERROR, Logging.getConfig().minLevel());
    }

    @Test
    @DisplayName("reset restores default configuration")
    void resetRestoresDefaultConfig() {
        Logging.configure(config -> config.level(Level.ERROR).async(true));

        Logging.reset();

        LogConfig config = Logging.getConfig();
        assertEquals(Level.INFO, config.minLevel());
        assertFalse(config.async());
    }

    @Test
    @DisplayName("configure with no appenders adds default JsonConsoleAppender")
    void configureWithNoAppendersAddsDefault() {
        Logging.reset(); // Start with default
        Logging.getConfig();

        Logging.configure(config -> config.level(Level.DEBUG));

        LogConfig after = Logging.getConfig();
        assertEquals(Level.DEBUG, after.minLevel());
        // Should have the default JsonConsoleAppender since we didn't specify any
        assertFalse(after.appenders().isEmpty());
    }
}

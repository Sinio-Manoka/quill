package com.quill.api;

import com.quill.config.Logging;
import com.quill.model.Level;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Logger interface and DefaultLogger implementation.
 */
@DisplayName("Logger Tests")
class LoggerTest {

    @AfterEach
    void resetConfig() {
        Logging.reset();
        LoggerFactory.reset();
    }

    @Test
    @DisplayName("Logger has correct name")
    void loggerHasCorrectName() {
        Logger logger = LoggerFactory.get("MyLogger");
        assertEquals("MyLogger", logger.name());
    }

    @Test
    @DisplayName("Logger.info returns LogBuilder")
    void infoReturnsLogBuilder() {
        Logger logger = LoggerFactory.get("TestLogger");
        LogBuilder builder = logger.info("Test message");
        assertNotNull(builder);
    }

    @Test
    @DisplayName("Logger.debug returns LogBuilder")
    void debugReturnsLogBuilder() {
        Logger logger = LoggerFactory.get("TestLogger");
        LogBuilder builder = logger.debug("Debug message");
        assertNotNull(builder);
    }

    @Test
    @DisplayName("Logger.trace returns LogBuilder")
    void traceReturnsLogBuilder() {
        Logger logger = LoggerFactory.get("TestLogger");
        LogBuilder builder = logger.trace("Trace message");
        assertNotNull(builder);
    }

    @Test
    @DisplayName("Logger.warn returns LogBuilder")
    void warnReturnsLogBuilder() {
        Logger logger = LoggerFactory.get("TestLogger");
        LogBuilder builder = logger.warn("Warning message");
        assertNotNull(builder);
    }

    @Test
    @DisplayName("Logger.error returns LogBuilder")
    void errorReturnsLogBuilder() {
        Logger logger = LoggerFactory.get("TestLogger");
        LogBuilder builder = logger.error("Error message");
        assertNotNull(builder);
    }

    @Test
    @DisplayName("Logger.log returns LogBuilder for each level")
    void logReturnsLogBuilderForEachLevel() {
        Logger logger = LoggerFactory.get("TestLogger");

        for (Level level : Level.values()) {
            LogBuilder builder = logger.log(level, "Message");
            assertNotNull(builder, STR."LogBuilder should not be null for \{level}");
        }
    }

    @Test
    @DisplayName("isEnabled returns true for enabled levels")
    void isEnabledReturnsTrueForEnabledLevels() {
        Logging.configure(config -> config.level(Level.INFO));

        Logger logger = LoggerFactory.get("TestLogger");

        assertTrue(logger.isEnabled(Level.INFO));
        assertTrue(logger.isEnabled(Level.WARN));
        assertTrue(logger.isEnabled(Level.ERROR));
    }

    @Test
    @DisplayName("isEnabled returns false for disabled levels")
    void isEnabledReturnsFalseForDisabledLevels() {
        Logging.configure(config -> config.level(Level.INFO));

        Logger logger = LoggerFactory.get("TestLogger");

        assertFalse(logger.isEnabled(Level.TRACE));
        assertFalse(logger.isEnabled(Level.DEBUG));
    }

    @Test
    @DisplayName("isEnabled respects configured threshold")
    void isEnabledRespectsConfiguredThreshold() {
        Logging.configure(config -> config.level(Level.WARN));

        Logger logger = LoggerFactory.get("TestLogger");

        assertFalse(logger.isEnabled(Level.INFO));
        assertTrue(logger.isEnabled(Level.WARN));
        assertTrue(logger.isEnabled(Level.ERROR));
    }

    @Test
    @DisplayName("isEnabled returns true for same level as threshold")
    void isEnabledReturnsTrueForSameLevel() {
        Logging.configure(config -> config.level(Level.ERROR));

        Logger logger = LoggerFactory.get("TestLogger");

        assertTrue(logger.isEnabled(Level.ERROR));
    }

    @Test
    @DisplayName("Multiple loggers have independent names")
    void multipleLoggersHaveIndependentNames() {
        Logger logger1 = LoggerFactory.get("Logger1");
        Logger logger2 = LoggerFactory.get("Logger2");

        assertEquals("Logger1", logger1.name());
        assertEquals("Logger2", logger2.name());
    }

    @Test
    @DisplayName("LogBuilder can be used without adding fields")
    void logBuilderCanBeUsedWithoutFields() {
        Logger logger = LoggerFactory.get("TestLogger");

        // Should not throw
        assertDoesNotThrow(() -> logger.info("Simple message").emit());
    }

    @Test
    @DisplayName("LogBuilder.field returns same builder for chaining")
    void logBuilderFieldReturnsSameBuilder() {
        Logger logger = LoggerFactory.get("TestLogger");
        LogBuilder builder = logger.info("Message");

        LogBuilder result = builder.field("key", "value");

        assertSame(builder, result, "field() should return this");
    }
}

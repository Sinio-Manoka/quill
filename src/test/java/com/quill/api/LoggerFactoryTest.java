package com.quill.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the LoggerFactory class.
 */
@DisplayName("LoggerFactory Tests")
class LoggerFactoryTest {

    @AfterEach
    void resetFactory() {
        LoggerFactory.reset();
    }

    @Test
    @DisplayName("get returns a Logger instance")
    void getReturnsLogger() {
        Logger logger = LoggerFactory.get("TestLogger");
        assertNotNull(logger);
        assertInstanceOf(DefaultLogger.class, logger);
    }

    @Test
    @DisplayName("get with Class returns Logger with class name")
    void getWithClassReturnsLoggerWithClassName() {
        Logger logger = LoggerFactory.get(LoggerFactoryTest.class);
        assertEquals("LoggerFactoryTest", logger.name());
    }

    @Test
    @DisplayName("get with Class.getSimpleName() as name")
    void getWithClassUsesSimpleClassName() {
        class InnerClass {
        }
        Logger logger = LoggerFactory.get(InnerClass.class);
        assertEquals("InnerClass", logger.name());
    }

    @Test
    @DisplayName("get returns same instance for same name")
    void returnsSameInstanceForSameName() {
        Logger logger1 = LoggerFactory.get("TestLogger");
        Logger logger2 = LoggerFactory.get("TestLogger");

        assertSame(logger1, logger2);
    }

    @Test
    @DisplayName("get returns same instance for same class")
    void returnsSameInstanceForSameClass() {
        Logger logger1 = LoggerFactory.get(LoggerFactoryTest.class);
        Logger logger2 = LoggerFactory.get(LoggerFactoryTest.class);

        assertSame(logger1, logger2);
    }

    @Test
    @DisplayName("get returns different instances for different names")
    void returnsDifferentInstancesForDifferentNames() {
        Logger logger1 = LoggerFactory.get("Logger1");
        Logger logger2 = LoggerFactory.get("Logger2");

        assertNotSame(logger1, logger2);
        assertEquals("Logger1", logger1.name());
        assertEquals("Logger2", logger2.name());
    }

    @Test
    @DisplayName("get throws on null class")
    void throwsOnNullClass() {
        assertThrows(NullPointerException.class,
                () -> LoggerFactory.get((Class<?>) null));
    }

    @Test
    @DisplayName("get throws on null name")
    void throwsOnNullName() {
        assertThrows(NullPointerException.class,
                () -> LoggerFactory.get((String) null));
    }

    @Test
    @DisplayName("Logger cache persists across multiple calls")
    void loggerCachePersists() {
        Logger logger1 = LoggerFactory.get("CachedLogger");
        Logger logger2 = LoggerFactory.get("CachedLogger");
        Logger logger3 = LoggerFactory.get("CachedLogger");

        assertSame(logger1, logger2);
        assertSame(logger2, logger3);
    }

    @Test
    @DisplayName("reset clears the logger cache")
    void resetClearsCache() {
        Logger logger1 = LoggerFactory.get("ResetLogger");

        LoggerFactory.reset();

        Logger logger2 = LoggerFactory.get("ResetLogger");

        assertNotSame(logger1, logger2, "After reset, should return new instance");
    }

    @Test
    @DisplayName("Multiple loggers can be cached simultaneously")
    void multipleLoggersCanBeCached() {
        Logger logger1 = LoggerFactory.get("Logger1");
        Logger logger2 = LoggerFactory.get("Logger2");
        Logger logger3 = LoggerFactory.get("Logger3");

        assertSame(logger1, LoggerFactory.get("Logger1"));
        assertSame(logger2, LoggerFactory.get("Logger2"));
        assertSame(logger3, LoggerFactory.get("Logger3"));
    }
}

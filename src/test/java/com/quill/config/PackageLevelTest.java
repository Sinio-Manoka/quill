package com.quill.config;

import com.quill.api.Logger;
import com.quill.api.LoggerFactory;
import com.quill.appender.Appender;
import com.quill.model.Level;
import com.quill.model.LogEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for package-level log filtering.
 */
class PackageLevelTest {

    private final List<LogEvent> capturedEvents = new ArrayList<>();
    private Appender testAppender;

    @BeforeEach
    void setUp() {
        Logging.reset();  // Reset to clean state
        LoggerFactory.reset();  // Clear logger cache
        capturedEvents.clear();
        testAppender = capturedEvents::add;
    }

    @AfterEach
    void tearDown() {
        Logging.reset();  // Reset to clean state
        LoggerFactory.reset();  // Clear logger cache
    }

    @Test
    void testPackageLevelOverridesGlobalLevel() {
        Logging.configure(c -> c
                .level(Level.WARN)  // Global WARN
                .packageLevel("com.example", Level.DEBUG)  // But DEBUG for com.example
                .appender(testAppender)
                .async(false)
        );

        Logger exampleLogger = LoggerFactory.get("com.example.MyService");
        Logger otherLogger = LoggerFactory.get("com.other.Service");

        exampleLogger.debugEmit("Example debug");  // Should appear
        exampleLogger.infoEmit("Example info");    // Should appear
        otherLogger.infoEmit("Other info");        // Should NOT appear (below WARN)
        otherLogger.warnEmit("Other warn");        // Should appear

        // Expected: Example debug, Example info, Other warn (3 events)
        assertEquals(3, capturedEvents.size());
        assertEquals("Example debug", capturedEvents.get(0).message());
        assertEquals("Example info", capturedEvents.get(1).message());
        assertEquals("Other warn", capturedEvents.get(2).message());
    }

    @Test
    void testSubpackageInheritsParentLevel() {
        Logging.configure(c -> c
                .level(Level.ERROR)
                .packageLevel("com.example", Level.INFO)
                .appender(testAppender)
                .async(false)
        );

        Logger subLogger = LoggerFactory.get("com.example.sub.service.MyService");

        subLogger.debugEmit("Debug");  // Should NOT appear (below INFO)
        subLogger.infoEmit("Info");    // Should appear
        subLogger.warnEmit("Warn");    // Should appear

        assertEquals(2, capturedEvents.size());
        assertEquals("Info", capturedEvents.get(0).message());
        assertEquals("Warn", capturedEvents.get(1).message());
    }

    @Test
    void testMoreSpecificPackageTakesPrecedence() {
        Logging.configure(c -> c
                .level(Level.ERROR)
                .packageLevel("com", Level.WARN)
                .packageLevel("com.example", Level.DEBUG)
                .appender(testAppender)
                .async(false)
        );

        Logger exampleLogger = LoggerFactory.get("com.example.Service");
        Logger otherLogger = LoggerFactory.get("com.other.Service");

        exampleLogger.debugEmit("Example debug");  // Should appear (com.example is DEBUG)
        otherLogger.debugEmit("Other debug");      // Should NOT appear (com is WARN)

        assertEquals(1, capturedEvents.size());
        assertEquals("Example debug", capturedEvents.getFirst().message());
    }

    @Test
    void testUnconfiguredPackageUsesGlobalLevel() {
        Logging.configure(c -> c
                .level(Level.INFO)
                .packageLevel("com.example", Level.DEBUG)
                .appender(testAppender)
                .async(false)
        );

        Logger unconfiguredLogger = LoggerFactory.get("org.unconfigured.Service");

        unconfiguredLogger.debugEmit("Debug");  // Should NOT appear (global INFO)
        unconfiguredLogger.infoEmit("Info");    // Should appear

        assertEquals(1, capturedEvents.size());
        assertEquals("Info", capturedEvents.getFirst().message());
    }

    @Test
    void testLoggerWithoutPackageUsesGlobalLevel() {
        Logging.configure(c -> c
                .level(Level.WARN)
                .packageLevel("com.example", Level.DEBUG)
                .appender(testAppender)
                .async(false)
        );

        Logger simpleLogger = LoggerFactory.get("SimpleLogger");

        simpleLogger.infoEmit("Info");  // Should NOT appear (below WARN)
        simpleLogger.warnEmit("Warn");  // Should appear

        assertEquals(1, capturedEvents.size());
        assertEquals("Warn", capturedEvents.getFirst().message());
    }

    @Test
    void testPackageLevelWithSampling() {
        Logging.configure(c -> c
                .level(Level.TRACE)
                .packageLevel("com.example", Level.DEBUG)
                .sampling(0.0)  // Drop all TRACE/DEBUG
                .appender(testAppender)
                .async(false)
        );

        Logger exampleLogger = LoggerFactory.get("com.example.Service");
        Logger otherLogger = LoggerFactory.get("com.other.Service");

        exampleLogger.traceEmit("Example trace");  // Should NOT appear (below DEBUG + sampled)
        exampleLogger.debugEmit("Example debug");  // Should NOT appear (sampled)
        exampleLogger.infoEmit("Example info");    // Should appear
        otherLogger.traceEmit("Other trace");      // Should NOT appear (below TRACE)
        otherLogger.debugEmit("Other debug");      // Should NOT appear (sampled)
        otherLogger.infoEmit("Other info");        // Should appear

        assertEquals(2, capturedEvents.size());
        assertEquals("Example info", capturedEvents.get(0).message());
        assertEquals("Other info", capturedEvents.get(1).message());
    }

    @Test
    void testMultiplePackageLevels() {
        Logging.configure(c -> c
                .level(Level.ERROR)
                .packageLevels(Map.of(
                        "com.example", Level.TRACE,
                        "com.example.api", Level.WARN,
                        "org.test", Level.DEBUG
                ))
                .appender(testAppender)
                .async(false)
        );

        Logger coreLogger = LoggerFactory.get("com.example.Core");
        Logger apiLogger = LoggerFactory.get("com.example.api.RestClient");
        Logger testLogger = LoggerFactory.get("org.test.Service");

        coreLogger.traceEmit("Core trace");    // Should appear (com.example is TRACE)
        coreLogger.debugEmit("Core debug");    // Should appear
        apiLogger.traceEmit("API trace");      // Should NOT appear (com.example.api is WARN)
        apiLogger.debugEmit("API debug");      // Should NOT appear
        apiLogger.warnEmit("API warn");        // Should appear
        testLogger.traceEmit("Test trace");    // Should NOT appear (org.test is DEBUG)
        testLogger.debugEmit("Test debug");    // Should appear

        assertEquals(4, capturedEvents.size());
    }

    @Test
    void testPackageLevelWorksWithLogBuilder() {
        Logging.configure(c -> c
                .level(Level.ERROR)
                .packageLevel("com.example", Level.DEBUG)
                .appender(testAppender)
                .async(false)
        );

        Logger exampleLogger = LoggerFactory.get("com.example.Service");

        exampleLogger.debug("Debug message")
                .field("key1", "value1")
                .field("key2", "value2")
                .emit();

        assertEquals(1, capturedEvents.size());
        assertEquals("Debug message", capturedEvents.getFirst().message());
        assertEquals("value1", capturedEvents.getFirst().fields().get("key1"));
        assertEquals("value2", capturedEvents.getFirst().fields().get("key2"));
    }
}

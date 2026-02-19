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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for sampling functionality.
 */
class SamplingTest {

    private final List<LogEvent> capturedEvents = new ArrayList<>();
    private Appender testAppender;

    @BeforeEach
    void setUp() {
        Logging.reset();  // Reset to clean state
        LoggerFactory.reset();  // Clear logger cache
        capturedEvents.clear();
        // Create appender AFTER reset to ensure clean state
        testAppender = capturedEvents::add;
    }

    @AfterEach
    void tearDown() {
        Logging.reset();  // Reset to clean state
        LoggerFactory.reset();  // Clear logger cache
    }

    @Test
    void testSamplingRateOfZeroDropsAllDebugLogs() {
        Logging.configure(c -> c
                .level(Level.DEBUG)
                .sampling(0.0)  // 0% = drop all
                .appender(testAppender)
                .async(false)
        );

        Logger log = LoggerFactory.get("com.test.TestLogger");

        for (int i = 0; i < 100; i++) {
            log.debugEmit(STR."Debug message \{i}");
        }

        // All should be sampled (dropped)
        assertEquals(0, capturedEvents.size());
    }

    @Test
    void testSamplingRateOfOneKeepsAllDebugLogs() {
        Logging.configure(c -> c
                .level(Level.DEBUG)
                .sampling(1.0)  // 100% = keep all
                .appender(testAppender)
                .async(false)
        );

        Logger log = LoggerFactory.get("com.test.TestLogger");

        for (int i = 0; i < 50; i++) {
            log.debugEmit(STR."Debug message \{i}");
        }

        // None should be sampled (all kept)
        assertEquals(50, capturedEvents.size());
    }

    @Test
    void testSamplingOnlyAppliesToTraceAndDebug() {
        Logging.configure(c -> c
                .level(Level.TRACE)
                .sampling(0.0)  // Drop all TRACE/DEBUG
                .appender(testAppender)
                .async(false)
        );

        Logger log = LoggerFactory.get("com.test.TestLogger");

        log.traceEmit("Trace");  // Should be sampled (dropped)
        log.debugEmit("Debug");  // Should be sampled (dropped)
        log.infoEmit("Info");    // Should NOT be sampled (kept)
        log.warnEmit("Warn");    // Should NOT be sampled (kept)
        log.errorEmit("Error");  // Should NOT be sampled (kept)

        // Only INFO, WARN, ERROR should appear
        assertEquals(3, capturedEvents.size());
        assertEquals("Info", capturedEvents.get(0).message());
        assertEquals("Warn", capturedEvents.get(1).message());
        assertEquals("Error", capturedEvents.get(2).message());
    }

    @Test
    void testSamplingWithFiftyPercentRate() {
        Logging.configure(c -> c
                .level(Level.DEBUG)
                .sampling(0.5)  // 50%
                .appender(testAppender)
                .async(false)
        );

        Logger log = LoggerFactory.get("com.test.TestLogger");

        for (int i = 0; i < 1000; i++) {
            log.debugEmit(STR."Debug message \{i}");
        }

        // Should be approximately 500 (give or take randomness)
        int count = capturedEvents.size();
        assertTrue(count > 200 && count < 800,
                STR."Expected ~500 messages, got \{count}");
    }

    @Test
    void testInfoAndAboveNeverSampled() {
        Logging.configure(c -> c
                .level(Level.TRACE)
                .sampling(0.01)  // 99% sampling (drop most)
                .appender(testAppender)
                .async(false)
        );

        Logger log = LoggerFactory.get("com.test.TestLogger");

        for (int i = 0; i < 100; i++) {
            log.infoEmit(STR."Info \{i}");
            log.warnEmit(STR."Warn \{i}");
            log.errorEmit(STR."Error \{i}");
        }

        // All 300 should appear (100 each)
        assertEquals(300, capturedEvents.size());
    }

    @Test
    void testSamplingDoesNotAffectLogBuilder() {
        Logging.configure(c -> c
                .level(Level.DEBUG)
                .sampling(0.0)  // Drop all DEBUG
                .appender(testAppender)
                .async(false)
        );

        Logger log = LoggerFactory.get("com.test.TestLogger");

        // Using fluent API
        log.debug("Debug message").field("key", "value").emit();

        // Should be sampled (dropped)
        assertEquals(0, capturedEvents.size());
    }
}

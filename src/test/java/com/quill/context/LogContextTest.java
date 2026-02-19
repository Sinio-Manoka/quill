package com.quill.context;

import com.quill.api.Logger;
import com.quill.api.LoggerFactory;
import com.quill.appender.Appender;
import com.quill.config.Logging;
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
 * Tests for LogContext functionality.
 */
class LogContextTest {

    private final List<LogEvent> capturedEvents = new ArrayList<>();
    private Logger logger;

    @BeforeEach
    void setUp() {
        LogContext.clear();
        capturedEvents.clear();

        // Configure with test appender that captures events
        Appender testAppender = capturedEvents::add;
        Logging.configure(config -> config
                .level(Level.TRACE)
                .appender(testAppender)
                .async(false)
        );

        logger = LoggerFactory.get(LogContextTest.class);
    }

    @AfterEach
    void tearDown() {
        LogContext.clear();
    }

    @Test
    void testBindSingleKeyValue() {
        LogContext.bind("requestId", "abc-123")
                .run(() -> assertEquals(Map.of("requestId", "abc-123"), LogContext.current()));

        // Context should be cleared after run
        assertTrue(LogContext.current().isEmpty());
    }

    @Test
    void testBindMultipleKeyValues() {
        LogContext.bind("requestId", "abc-123")
                .and("userId", 42)
                .and("sessionId", "xyz-789")
                .run(() -> {
                    Map<String, Object> ctx = LogContext.current();
                    assertEquals(3, ctx.size());
                    assertEquals("abc-123", ctx.get("requestId"));
                    assertEquals(42, ctx.get("userId"));
                    assertEquals("xyz-789", ctx.get("sessionId"));
                });
    }

    @Test
    void testBindFromMap() {
        Map<String, Object> values = Map.of(
                "traceId", "trace-abc",
                "spanId", "span-xyz"
        );

        LogContext.bind(values)
                .run(() -> {
                    Map<String, Object> ctx = LogContext.current();
                    assertEquals(2, ctx.size());
                    assertEquals("trace-abc", ctx.get("traceId"));
                    assertEquals("span-xyz", ctx.get("spanId"));
                });
    }

    @Test
    void testContextIsInjectedIntoLogs() {
        LogContext.bind("requestId", "req-123")
                .and("userId", 999)
                .run(() -> logger.infoEmit("Test message"));

        assertEquals(1, capturedEvents.size());
        LogEvent event = capturedEvents.getFirst();
        assertEquals("req-123", event.context().get("requestId"));
        assertEquals("999", event.context().get("userId"));
    }

    @Test
    void testContextIsClearedAfterRun() {
        LogContext.bind("key", "value")
                .run(() -> logger.infoEmit("Inside context"));

        // Log after context should not have the previous context
        logger.infoEmit("Outside context");

        assertEquals(2, capturedEvents.size());
        assertTrue(capturedEvents.get(0).context().containsKey("key"));
        assertFalse(capturedEvents.get(1).context().containsKey("key"));
    }

    @Test
    void testNestedContextInheritsOuter() {
        LogContext.bind("outerKey", "outerValue")
                .run(() -> {
                    logger.infoEmit("Outer scope");

                    LogContext.bind("innerKey", "innerValue")
                            .run(() -> {
                                logger.infoEmit("Inner scope");

                                // Inner scope should have both outer and inner context
                                Map<String, Object> ctx = LogContext.current();
                                assertEquals(2, ctx.size());
                                assertEquals("outerValue", ctx.get("outerKey"));
                                assertEquals("innerValue", ctx.get("innerKey"));
                            });

                    logger.infoEmit("Back to outer scope");
                });

        assertEquals(3, capturedEvents.size());

        // Outer scope log has only outer context
        LogEvent outerEvent = capturedEvents.getFirst();
        assertTrue(outerEvent.context().containsKey("outerKey"));
        assertFalse(outerEvent.context().containsKey("innerKey"));

        // Inner scope log has both contexts
        LogEvent innerEvent = capturedEvents.get(1);
        assertTrue(innerEvent.context().containsKey("outerKey"));
        assertTrue(innerEvent.context().containsKey("innerKey"));

        // Back to outer scope has only outer context
        LogEvent backToOuterEvent = capturedEvents.get(2);
        assertTrue(backToOuterEvent.context().containsKey("outerKey"));
        assertFalse(backToOuterEvent.context().containsKey("innerKey"));
    }

    @Test
    void testNestedContextOverridesOuterOnConflict() {
        LogContext.bind("key", "outer")
                .run(() -> LogContext.bind("key", "inner")
                        .run(() -> {
                            Map<String, Object> ctx = LogContext.current();
                            assertEquals("inner", ctx.get("key"));
                        }));
    }

    @Test
    void testMultipleSequentialContexts() {
        LogContext.bind("key1", "value1")
                .run(() -> logger.infoEmit("First"));

        LogContext.bind("key2", "value2")
                .run(() -> logger.infoEmit("Second"));

        assertEquals(2, capturedEvents.size());
        assertTrue(capturedEvents.get(0).context().containsKey("key1"));
        assertFalse(capturedEvents.get(0).context().containsKey("key2"));
        assertFalse(capturedEvents.get(1).context().containsKey("key1"));
        assertTrue(capturedEvents.get(1).context().containsKey("key2"));
    }

    @Test
    void testContextWithDifferentValueTypes() {
        LogContext.bind("string", "text")
                .and("number", 42)
                .and("decimal", 3.14)
                .and("bool", true)
                .and("nullValue", null)  // null values are filtered out
                .run(() -> {
                    Map<String, Object> ctx = LogContext.current();
                    assertEquals("text", ctx.get("string"));
                    assertEquals(42, ctx.get("number"));
                    assertEquals(3.14, ctx.get("decimal"));
                    assertEquals(true, ctx.get("bool"));
                    assertFalse(ctx.containsKey("nullValue"));  // null values are excluded
                });
    }

    @Test
    void testBindRequiresNonNullKey() {
        assertThrows(NullPointerException.class, () -> LogContext.bind(null, "value"));
    }

    @Test
    void testBindFromMapRequiresNonNullMap() {
        assertThrows(NullPointerException.class, () -> LogContext.bind(null));
    }

    @Test
    void testRunRequiresNonNullAction() {
        assertThrows(NullPointerException.class, () -> LogContext.bind("key", "value").run(null));
    }

    @Test
    void testAndRequiresNonNullKey() {
        assertThrows(NullPointerException.class, () -> LogContext.bind("key1", "value1").and(null, "value2"));
    }

    @Test
    void testEmptyContextDoesNotAddContextToLog() {
        // Log without any context
        logger.infoEmit("No context");

        assertTrue(capturedEvents.getFirst().context().isEmpty());
    }

    @Test
    void testDeepNesting() {
        LogContext.bind("level1", "1")
                .run(() -> LogContext.bind("level2", "2")
                        .run(() -> LogContext.bind("level3", "3")
                                .run(() -> logger.infoEmit("Deep"))));

        assertEquals(1, capturedEvents.size());
        LogEvent event = capturedEvents.getFirst();
        assertTrue(event.context().containsKey("level1"));
        assertTrue(event.context().containsKey("level2"));
        assertTrue(event.context().containsKey("level3"));
    }

    @Test
    void testContextInLogBuilder() {
        LogContext.bind("traceId", "trace-123")
                .run(() -> logger.info("Message with fields", "field1", "value1"));

        assertEquals(1, capturedEvents.size());
        LogEvent event = capturedEvents.getFirst();
        assertEquals("trace-123", event.context().get("traceId"));
        assertEquals("value1", event.fields().get("field1"));
    }

    @Test
    void testContextWithLogBuilderFieldChaining() {
        LogContext.bind("requestId", "req-xyz")
                .run(() -> logger.info("Complex event")
                        .field("userId", 42)
                        .field("action", "login")
                        .emit());

        assertEquals(1, capturedEvents.size());
        LogEvent event = capturedEvents.getFirst();
        assertEquals("req-xyz", event.context().get("requestId"));
        assertEquals(42, event.fields().get("userId"));
        assertEquals("login", event.fields().get("action"));
    }

    @Test
    void testNestedContextRestoresPreviousContext() {
        // This test specifically checks the branch where previous context is restored
        LogContext.bind("outer", "value1")
                .run(() -> {
                    // Before inner context
                    assertEquals("value1", LogContext.current().get("outer"));

                    LogContext.bind("inner", "value2")
                            .run(() -> {
                                // Inside inner context
                                assertEquals("value1", LogContext.current().get("outer"));
                                assertEquals("value2", LogContext.current().get("inner"));
                            });

                    // After inner context - outer should be restored
                    assertEquals("value1", LogContext.current().get("outer"));
                    assertFalse(LogContext.current().containsKey("inner"));
                });

        // After all contexts - should be empty
        assertTrue(LogContext.current().isEmpty());
    }

    @Test
    void testClearRemovesContext() {
        LogContext.bind("key", "value")
                .run(() -> {
                    // Context exists
                    assertFalse(LogContext.current().isEmpty());

                    // Clear the context
                    LogContext.clear();

                    // Context should be gone
                    assertTrue(LogContext.current().isEmpty());
                });
    }

    @Test
    void testNestedContextWithNonEmptyOuterRestoresProperly() {
        // Test the specific branch where previous is non-empty and needs to be restored
        LogContext.bind("key1", "value1")
                .run(() -> {
                    LogContext.bind("key2", "value2")
                            .run(() -> {
                                assertEquals(2, LogContext.current().size());
                            });

                    // Should have only key1 after inner context completes
                    assertEquals(1, LogContext.current().size());
                    assertEquals("value1", LogContext.current().get("key1"));
                });
    }

    @Test
    void testBindWithEmptyMap() {
        // Empty map should still work
        Map<String, Object> emptyMap = Map.of();
        LogContext.bind(emptyMap)
                .run(() -> assertTrue(LogContext.current().isEmpty()));
    }

    @Test
    void testBindWithSingletonMap() {
        // Single entry map
        Map<String, Object> singletonMap = Map.of("single", "value");
        LogContext.bind(singletonMap)
                .run(() -> assertEquals("value", LogContext.current().get("single")));
    }

    @Test
    void testNestedContextWithNullValueDoesNotOverride() {
        // Test that null values don't override existing values
        LogContext.bind("key", "original")
                .run(() -> {
                    LogContext.bind("key", null)
                            .run(() -> {
                                // null should not override the original value
                                assertEquals("original", LogContext.current().get("key"));
                            });
                });
    }
}

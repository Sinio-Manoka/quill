package com.quill.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the LogEvent record.
 */
@DisplayName("LogEvent Tests")
class LogEventTest {

    @Test
    @DisplayName("LogEvent can be created with all fields")
    void createWithAllFields() {
        Instant now = Instant.now();
        Map<String, Object> fields = Map.of("userId", 42, "action", "login");
        Map<String, String> context = Map.of("requestId", "abc-123");

        LogEvent event = new LogEvent(now, Level.INFO, "User logged in",
                fields, context, "main", "TestService");

        assertEquals(now, event.timestamp());
        assertEquals(Level.INFO, event.level());
        assertEquals("User logged in", event.message());
        assertEquals(2, event.fields().size());
        assertEquals(42, event.fields().get("userId"));
        assertEquals(1, event.context().size());
        assertEquals("abc-123", event.context().get("requestId"));
        assertEquals("main", event.threadName());
        assertEquals("TestService", event.loggerName());
    }

    @Test
    @DisplayName("LogEvent can be created without explicit timestamp")
    void createWithoutTimestamp() {
        Map<String, Object> fields = Map.of("key", "value");

        LogEvent event = new LogEvent(Level.INFO, "message", fields,
                Map.of(), "main", "Logger");

        assertNotNull(event.timestamp());
        assertTrue(event.timestamp().getEpochSecond() > 0);
    }

    @Test
    @DisplayName("LogEvent handles null maps correctly")
    void handlesNullMaps() {
        LogEvent event = new LogEvent(
                Instant.now(),
                Level.INFO,
                "message",
                null,
                null,
                "main",
                "Logger"
        );

        assertTrue(event.fields().isEmpty());
        assertTrue(event.context().isEmpty());
    }

    @Test
    @DisplayName("Fields map is unmodifiable")
    void fieldsMapIsUnmodifiable() {
        Map<String, Object> fields = Map.of("key", "value");
        LogEvent event = new LogEvent(
                Instant.now(),
                Level.INFO,
                "message",
                fields,
                Map.of(),
                "main",
                "Logger"
        );

        assertThrows(UnsupportedOperationException.class,
                () -> event.fields().put("newKey", "newValue"));
    }

    @Test
    @DisplayName("Context map is unmodifiable")
    void contextMapIsUnmodifiable() {
        Map<String, String> context = Map.of("key", "value");
        LogEvent event = new LogEvent(
                Instant.now(),
                Level.INFO,
                "message",
                Map.of(),
                context,
                "main",
                "Logger"
        );

        assertThrows(UnsupportedOperationException.class,
                () -> event.context().put("newKey", "newValue"));
    }

    @Test
    @DisplayName("Thread name defaults to current thread when null")
    void threadNameDefaultsToCurrentThread() {
        String currentThread = Thread.currentThread().getName();

        LogEvent event = new LogEvent(
                Instant.now(),
                Level.INFO,
                "message",
                Map.of(),
                Map.of(),
                null,  // null thread name
                "Logger"
        );

        assertEquals(currentThread, event.threadName());
    }

    @Test
    @DisplayName("LogEvent record equality works correctly")
    void recordEqualityWorks() {
        Instant now = Instant.now();
        Map<String, Object> fields = Map.of("key", "value");
        Map<String, String> context = Map.of("ctx", "value");

        LogEvent event1 = new LogEvent(now, Level.INFO, "message",
                fields, context, "main", "Logger");
        LogEvent event2 = new LogEvent(now, Level.INFO, "message",
                fields, context, "main", "Logger");

        assertEquals(event1, event2);
        assertEquals(event1.hashCode(), event2.hashCode());
    }

    @Test
    @DisplayName("LogEvent stores different data types in fields")
    void storesDifferentDataTypes() {
        // Use LinkedHashMap because Map.of() doesn't allow null values
        Map<String, Object> fields = new java.util.LinkedHashMap<>();
        fields.put("string", "value");
        fields.put("integer", 42);
        fields.put("long", 123L);
        fields.put("double", 3.14);
        fields.put("boolean", true);
        fields.put("nullValue", null);

        LogEvent event = new LogEvent(
                Instant.now(),
                Level.INFO,
                "message",
                fields,
                Map.of(),
                "main",
                "Logger"
        );

        assertEquals("value", event.fields().get("string"));
        assertEquals(42, event.fields().get("integer"));
        assertEquals(123L, event.fields().get("long"));
        assertEquals(3.14, event.fields().get("double"));
        assertEquals(true, event.fields().get("boolean"));
        assertNull(event.fields().get("nullValue"));
    }
}

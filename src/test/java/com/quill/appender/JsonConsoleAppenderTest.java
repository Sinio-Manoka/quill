package com.quill.appender;

import com.quill.model.Level;
import com.quill.model.LogEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the JsonConsoleAppender class.
 */
@DisplayName("JsonConsoleAppender Tests")
class JsonConsoleAppenderTest {

    @Test
    @DisplayName("JsonConsoleAppender outputs valid JSON")
    void outputsValidJson() {
        String output = captureJsonOutput(Level.INFO, "Test message", Map.of("key", "value"));

        assertTrue(output.startsWith("{"), "JSON should start with {");
        assertTrue(output.endsWith("}") || output.endsWith("}\n") || output.endsWith("}\r\n"),
                "JSON should end with }");
        assertTrue(output.contains("\"timestamp\":"), "Should contain timestamp field");
        assertTrue(output.contains("\"level\":\"INFO\""), "Should contain level field");
        assertTrue(output.contains("\"logger\""), "Should contain logger field");
        assertTrue(output.contains("\"thread\""), "Should contain thread field");
        assertTrue(output.contains("\"message\""), "Should contain message field");
    }

    @Test
    @DisplayName("JsonConsoleAppender includes all log levels correctly")
    void includesAllLogLevels() {
        for (Level level : Level.values()) {
            String output = captureJsonOutput(level, "message", Map.of());
            assertTrue(output.contains(STR."\"level\":\"\{level.name()}\""),
                    STR."Level \{level} should be in output");
        }
    }

    @Test
    @DisplayName("JsonConsoleAppender includes structured fields")
    void includesStructuredFields() {
        String output = captureJsonOutput(Level.INFO, "message",
                Map.of("userId", 42, "action", "login"));

        assertTrue(output.contains("\"userId\":42"), "Should include userId field");
        assertTrue(output.contains("\"action\":\"login\""), "Should include action field");
    }

    @Test
    @DisplayName("JsonConsoleAppender includes context with underscore prefix")
    void includesContextWithUnderscore() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(output));
        try {
            JsonConsoleAppender appender = new JsonConsoleAppender();
            LogEvent event = new LogEvent(
                    Instant.ofEpochMilli(0),
                    Level.INFO,
                    "Message",
                    Map.of("key", "value"),
                    Map.of("traceId", "abc-123", "userId", "42"),
                    "main",
                    "TestLogger"
            );
            appender.append(event);
        } finally {
            System.setOut(originalOut);
        }

        String result = output.toString();
        assertTrue(result.contains("\"_traceId\":\"abc-123\""),
                "Context should be prefixed with _");
        assertTrue(result.contains("\"_userId\":\"42\""),
                "Context should be prefixed with _");
    }

    @Test
    @DisplayName("JsonConsoleAppender handles different data types")
    void handlesDifferentDataTypes() {
        // Use LinkedHashMap instead of Map.of() which doesn't allow null values
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("string", "value");
        fields.put("integer", 42);
        fields.put("long", 123L);
        fields.put("double", 3.14);
        fields.put("boolean", true);
        fields.put("nullValue", null);

        String output = captureJsonOutput(Level.INFO, "message", fields);

        assertTrue(output.contains("\"string\":\"value\""));
        assertTrue(output.contains("\"integer\":42"));
        assertTrue(output.contains("\"long\":123"));
        assertTrue(output.contains("\"double\":3.14"));
        assertTrue(output.contains("\"boolean\":true"));
        assertTrue(output.contains("\"nullValue\":null"));
    }

    @Test
    @DisplayName("JsonConsoleAppender escapes special characters in strings")
    void escapesSpecialCharacters() {
        String output = captureJsonOutput(Level.INFO, "Line 1\nLine 2\tTabbed", Map.of());

        assertTrue(output.contains("\\n"), "Newlines should be escaped");
        assertTrue(output.contains("\\t"), "Tabs should be escaped");
    }

    @Test
    @DisplayName("JsonConsoleAppender includes timestamp in ISO format")
    void includesTimestampInIsoFormat() {
        long fixedMillis = 1705313021000L; // 2024-01-15T09:23:41Z
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(output));
        try {
            JsonConsoleAppender appender = new JsonConsoleAppender();
            LogEvent event = new LogEvent(
                    Instant.ofEpochMilli(fixedMillis),
                    Level.INFO,
                    "Message",
                    Map.of(),
                    Map.of(),
                    "main",
                    "TestLogger"
            );
            appender.append(event);
        } finally {
            System.setOut(originalOut);
        }

        String result = output.toString();
        // Check for ISO-8601 timestamp format
        assertTrue(result.replaceAll("[\r\n]", "").matches(".*\"timestamp\":\"\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z\".*"),
                STR."Should contain ISO-8601 timestamp. Got: \{result.replace("\r", "\\r").replace("\n", "\\n")}");
    }

    @Test
    @DisplayName("JsonConsoleAppender includes logger name")
    void includesLoggerName() {
        String output = captureJsonOutput(Level.INFO, "message", Map.of(), "MyCustomLogger");
        assertTrue(output.contains("\"logger\":\"MyCustomLogger\""));
    }

    @Test
    @DisplayName("JsonConsoleAppender includes thread name")
    void includesThreadName() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(output));
        try {
            JsonConsoleAppender appender = new JsonConsoleAppender();
            LogEvent event = new LogEvent(
                    Instant.ofEpochMilli(0),
                    Level.INFO,
                    "Message",
                    Map.of(),
                    Map.of(),
                    "custom-thread",
                    "TestLogger"
            );
            appender.append(event);
        } finally {
            System.setOut(originalOut);
        }

        String result = output.toString();
        assertTrue(result.contains("\"thread\":\"custom-thread\""));
    }

    @Test
    @DisplayName("JsonConsoleAppender handles empty fields and context")
    void handlesEmptyFieldsAndContext() {
        String output = captureJsonOutput(Level.INFO, "message", Map.of());

        // Should still be valid JSON
        assertTrue(output.startsWith("{"));
        assertTrue(output.endsWith("}") || output.trim().endsWith("}"));

        // Fields without context - check for underscore prefix in context keys only
        // The timestamp field might be present, check that context keys are not present
        assertFalse(output.contains("\"_traceId") || output.contains("\"_userId"),
                "No context underscore keys when context is empty");
    }

    @Test
    @DisplayName("JsonConsoleAppender outputs single line per event")
    void outputsSingleLinePerEvent() {
        String output = captureJsonOutput(Level.INFO, "message", Map.of());

        // Count newlines - should be exactly 1 (at end)
        long newlineCount = output.chars().filter(c -> c == '\n').count();
        assertEquals(1, newlineCount, "Should be exactly one line");
    }

    @Test
    @DisplayName("JsonConsoleAppender handles unicode characters")
    void handlesUnicodeCharacters() {
        String output = captureJsonOutput(Level.INFO, "Hello 🌍", Map.of("emoji", "👋"));

        assertTrue(output.contains("Hello 🌍"));
        assertTrue(output.contains("👋"));
    }

    private String captureJsonOutput(Level level, String message, Map<String, Object> fields) {
        return captureJsonOutput(level, message, fields, "TestLogger");
    }

    private String captureJsonOutput(Level level, String message, Map<String, Object> fields, String loggerName) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(output));
        try {
            JsonConsoleAppender appender = new JsonConsoleAppender();
            LogEvent event = new LogEvent(
                    Instant.ofEpochMilli(0),
                    level,
                    message,
                    fields,
                    Map.of(),
                    "main",
                    loggerName
            );
            appender.append(event);
        } finally {
            System.setOut(originalOut);
        }
        return output.toString();
    }
}

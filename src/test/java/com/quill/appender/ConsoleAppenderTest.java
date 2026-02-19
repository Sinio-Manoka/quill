package com.quill.appender;

import com.quill.model.Level;
import com.quill.model.LogEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the ConsoleAppender class.
 */
@DisplayName("ConsoleAppender Tests")
class ConsoleAppenderTest {

    @Test
    @DisplayName("ConsoleAppender outputs human-readable format")
    void outputsHumanReadableFormat() {
        ByteArrayOutputStream output = captureOutput(() -> {
            ConsoleAppender appender = new ConsoleAppender(false);
            Map<String, Object> fields = new HashMap<>();
            fields.put("key", "value");
            Map<String, String> context = new HashMap<>();
            context.put("ctx", "context");
            LogEvent event = new LogEvent(
                    Instant.ofEpochMilli(0),
                    Level.INFO,
                    "Test message",
                    fields,
                    context,
                    "main",
                    "TestLogger"
            );
            appender.append(event);
        });

        String result = output.toString();
        // Check for timestamp format (HH:mm:ss.SSS) - be flexible with timezone
        // Use contains instead of matches to avoid newline issues
        assertTrue(result.replaceAll("[\r\n]", "").matches(".*\\[\\d{2}:\\d{2}:\\d{2}\\.\\d{3}\\].*"),
                STR."Should contain timestamp. Got: \{result.replace("\r", "\\r").replace("\n", "\\n")}");
        assertTrue(result.contains("[INFO]"), "Should contain level");
        assertTrue(result.contains("[TestLogger]"), "Should contain logger name");
        assertTrue(result.contains("Test message"), "Should contain message");
        assertTrue(result.contains("key=\"value\"") || result.contains("key=value"), "Should contain field");
        assertTrue(result.contains("_ctx=context"), "Should contain context");
    }

    @Test
    @DisplayName("ConsoleAppender includes ANSI colors when enabled")
    void includesAnsiColorsWhenEnabled() {
        ByteArrayOutputStream output = captureOutput(() -> {
            ConsoleAppender appender = new ConsoleAppender(true);
            LogEvent event = new LogEvent(
                    Instant.ofEpochMilli(0),
                    Level.ERROR,
                    "Error message",
                    Map.of(),
                    Map.of(),
                    "main",
                    "TestLogger"
            );
            appender.append(event);
        });

        String result = output.toString();
        assertTrue(result.contains("\u001B[31m"), "Should contain red ANSI code for ERROR");
        assertTrue(result.contains("\u001B[0m"), "Should contain reset ANSI code");
    }

    @Test
    @DisplayName("ConsoleAppender does not include colors when disabled")
    void noColorsWhenDisabled() {
        ByteArrayOutputStream output = captureOutput(() -> {
            ConsoleAppender appender = new ConsoleAppender(false);
            LogEvent event = new LogEvent(
                    Instant.ofEpochMilli(0),
                    Level.ERROR,
                    "Error message",
                    Map.of(),
                    Map.of(),
                    "main",
                    "TestLogger"
            );
            appender.append(event);
        });

        String result = output.toString();
        assertFalse(result.contains("\u001B"), "Should not contain ANSI escape codes");
    }

    @Test
    @DisplayName("ConsoleAppender formats string values with quotes")
    void formatsStringsWithQuotes() {
        ByteArrayOutputStream output = captureOutput(() -> {
            ConsoleAppender appender = new ConsoleAppender(false);
            LogEvent event = new LogEvent(
                    Instant.ofEpochMilli(0),
                    Level.INFO,
                    "Message",
                    Map.of("name", "John"),
                    Map.of(),
                    "main",
                    "TestLogger"
            );
            appender.append(event);
        });

        String result = output.toString();
        assertTrue(result.contains("name=\"John\""), "String values should be quoted");
    }

    @Test
    @DisplayName("ConsoleAppender formats null values correctly")
    void formatsNullValues() {
        ByteArrayOutputStream output = captureOutput(() -> {
            ConsoleAppender appender = new ConsoleAppender(false);
            // Use LinkedHashMap instead of Map.of() which doesn't allow nulls
            Map<String, Object> fields = new LinkedHashMap<>();
            fields.put("nullable", null);
            LogEvent event = new LogEvent(
                    Instant.ofEpochMilli(0),
                    Level.INFO,
                    "Message",
                    fields,
                    Map.of(),
                    "main",
                    "TestLogger"
            );
            appender.append(event);
        });

        String result = output.toString();
        assertTrue(result.contains("nullable=null"), "Null values should display as 'null'");
    }

    @Test
    @DisplayName("ConsoleAppender handles multiple fields")
    void handlesMultipleFields() {
        ByteArrayOutputStream output = captureOutput(() -> {
            ConsoleAppender appender = new ConsoleAppender(false);
            LogEvent event = new LogEvent(
                    Instant.ofEpochMilli(0),
                    Level.INFO,
                    "Message",
                    Map.of("a", 1, "b", 2, "c", 3),
                    Map.of(),
                    "main",
                    "TestLogger"
            );
            appender.append(event);
        });

        String result = output.toString();
        assertTrue(result.contains("a=1"));
        assertTrue(result.contains("b=2"));
        assertTrue(result.contains("c=3"));
    }

    @Test
    @DisplayName("ConsoleAppender handles multiple context entries")
    void handlesMultipleContextEntries() {
        ByteArrayOutputStream output = captureOutput(() -> {
            ConsoleAppender appender = new ConsoleAppender(false);
            LogEvent event = new LogEvent(
                    Instant.ofEpochMilli(0),
                    Level.INFO,
                    "Message",
                    Map.of(),
                    Map.of("traceId", "abc", "userId", "123"),
                    "main",
                    "TestLogger"
            );
            appender.append(event);
        });

        String result = output.toString();
        assertTrue(result.contains("_traceId=abc"));
        assertTrue(result.contains("_userId=123"));
    }

    private ByteArrayOutputStream captureOutput(Runnable action) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(output));
        try {
            action.run();
        } finally {
            System.setOut(originalOut);
        }
        return output;
    }
}

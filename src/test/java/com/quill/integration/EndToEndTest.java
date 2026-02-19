package com.quill.integration;

import com.quill.api.Logger;
import com.quill.api.LoggerFactory;
import com.quill.appender.Appender;
import com.quill.appender.ConsoleAppender;
import com.quill.appender.JsonConsoleAppender;
import com.quill.config.Logging;
import com.quill.model.Level;
import com.quill.model.LogEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration tests for the Quill logging library.
 */
@DisplayName("End-to-End Integration Tests")
class EndToEndTest {

    @AfterEach
    void resetConfig() {
        Logging.reset();
        LoggerFactory.reset();
    }

    @Test
    @DisplayName("Full logging workflow with JSON output")
    void fullLoggingWorkflowWithJson() {
        ByteArrayOutputStream output = captureOutput(() -> {
            Logging.configure(config -> config
                    .level(Level.INFO)
                    .appender(new JsonConsoleAppender())
                    .async(false)
            );

            Logger log = LoggerFactory.get("UserService");

            log.info("User created")
                    .field("userId", 12345)
                    .field("email", "user@example.com")
                    .field("verified", true)
                    .emit();

            log.warn("Password reset requested")
                    .field("userId", 12345)
                    .emit();

            log.error("Database connection failed")
                    .field("host", "db.example.com")
                    .field("port", 5432)
                    .field("errorCode", "CONN_ERR")
                    .emit();
        });

        String result = output.toString();

        // Verify all events were logged
        assertTrue(result.contains("\"level\":\"INFO\""));
        assertTrue(result.contains("\"level\":\"WARN\""));
        assertTrue(result.contains("\"level\":\"ERROR\""));

        // Verify fields
        assertTrue(result.contains("\"userId\":12345"));
        assertTrue(result.contains("\"email\":\"user@example.com\""));
        assertTrue(result.contains("\"verified\":true"));
        assertTrue(result.contains("\"host\":\"db.example.com\""));
        assertTrue(result.contains("\"port\":5432"));
        assertTrue(result.contains("\"errorCode\":\"CONN_ERR\""));

        // Count the number of JSON objects (lines starting with "{")
        long eventCount = result.lines().filter(line -> line.startsWith("{")).count();
        assertEquals(3, eventCount);
    }

    @Test
    @DisplayName("Full logging workflow with console output")
    void fullLoggingWorkflowWithConsole() {
        ByteArrayOutputStream output = captureOutput(() -> {
            Logging.configure(config -> config
                    .level(Level.DEBUG)
                    .appender(new ConsoleAppender(false))
                    .async(false)
            );

            Logger log = LoggerFactory.get("PaymentService");

            log.debug("Processing payment")
                    .field("amount", 99.99)
                    .field("currency", "USD")
                    .emit();

            log.info("Payment successful")
                    .field("transactionId", "txn-abc-123")
                    .field("amount", 99.99)
                    .emit();
        });

        String result = output.toString();

        assertTrue(result.contains("[DEBUG]"));
        assertTrue(result.contains("[INFO]"));
        assertTrue(result.contains("Processing payment"));
        assertTrue(result.contains("Payment successful"));
        assertTrue(result.contains("amount=99.99"));
        assertTrue(result.contains("currency=\"USD\""));
        assertTrue(result.contains("transactionId=\"txn-abc-123\""));
    }

    @Test
    @DisplayName("Multiple appenders receive same event")
    void multipleAppendersReceiveSameEvent() {
        TestAppender appender1 = new TestAppender();
        TestAppender appender2 = new TestAppender();

        Logging.configure(config -> config
                .level(Level.INFO)
                .appender(appender1)
                .appender(appender2)
        );

        Logger log = LoggerFactory.get("TestService");

        log.info("Test message")
                .field("key", "value")
                .emit();

        // Both appenders should have received the event
        assertEquals(1, appender1.getEvents().size());
        assertEquals(1, appender2.getEvents().size());

        LogEvent event1 = appender1.getEvents().getFirst();
        LogEvent event2 = appender2.getEvents().getFirst();

        assertEquals("Test message", event1.message());
        assertEquals("Test message", event2.message());
        assertEquals("value", event1.fields().get("key"));
        assertEquals("value", event2.fields().get("key"));
    }

    @Test
    @DisplayName("Logger level filtering works correctly")
    void loggerLevelFilteringWorks() {
        TestAppender appender = new TestAppender();

        Logging.configure(config -> config
                .level(Level.WARN)
                .appender(appender)
        );

        Logger log = LoggerFactory.get("FilteredLogger");

        log.debug("Debug message").emit();
        log.info("Info message").emit();
        log.warn("Warn message").emit();
        log.error("Error message").emit();

        // Only WARN and ERROR should have been logged
        List<LogEvent> events = appender.getEvents();
        assertEquals(2, events.size());
        assertEquals(Level.WARN, events.get(0).level());
        assertEquals(Level.ERROR, events.get(1).level());
    }

    @Test
    @DisplayName("Configuration can be changed at runtime")
    void configurationCanBeChangedAtRuntime() {
        TestAppender appender = new TestAppender();

        Logging.configure(config -> config
                .level(Level.ERROR)
                .appender(appender)
        );

        Logger log = LoggerFactory.get("DynamicLogger");

        log.info("Info - should not appear").emit();
        assertEquals(0, appender.getEvents().size());

        // Change configuration
        Logging.configure(config -> config
                .level(Level.INFO)
                .appender(appender)
        );

        log.info("Info - should appear now").emit();
        assertEquals(1, appender.getEvents().size());
    }

    @Test
    @DisplayName("Multiple loggers work independently")
    void multipleLoggersWorkIndependently() {
        TestAppender appender = new TestAppender();

        Logging.configure(config -> config
                .level(Level.DEBUG)
                .appender(appender)
        );

        Logger service1 = LoggerFactory.get("Service1");
        Logger service2 = LoggerFactory.get("Service2");

        service1.info("Service1 message").field("service", 1).emit();
        service2.info("Service2 message").field("service", 2).emit();

        List<LogEvent> events = appender.getEvents();
        assertEquals(2, events.size());

        assertEquals("Service1", events.get(0).loggerName());
        assertEquals(1, events.get(0).fields().get("service"));

        assertEquals("Service2", events.get(1).loggerName());
        assertEquals(2, events.get(1).fields().get("service"));
    }

    @Test
    @DisplayName("Complex nested data structures are serialized")
    void complexNestedDataStructuresSerialized() {
        ByteArrayOutputStream output = captureOutput(() -> {
            Logging.configure(config -> config
                    .level(Level.INFO)
                    .appender(new JsonConsoleAppender())
            );

            Logger log = LoggerFactory.get("ComplexLogger");

            log.info("Complex data")
                    .field("map", Map.of("a", 1, "b", 2))
                    .field("list", java.util.List.of("x", "y", "z"))
                    .emit();
        });

        String result = output.toString();
        assertTrue(result.contains("\"map\":{"));
        assertTrue(result.contains("\"list\":["));
    }

    @Test
    @DisplayName("Log events include correct metadata")
    void logEventsIncludeCorrectMetadata() {
        TestAppender appender = new TestAppender();

        Logging.configure(config -> config.appender(appender));

        Logger log = LoggerFactory.get("MetadataTest");

        log.info("Metadata check").emit();

        LogEvent event = appender.getEvents().getFirst();

        assertNotNull(event.timestamp());
        assertEquals(Level.INFO, event.level());
        assertEquals("Metadata check", event.message());
        assertEquals("MetadataTest", event.loggerName());
        assertNotNull(event.threadName());
    }

    /**
     * Test appender that captures events for verification.
     */
    private static class TestAppender implements Appender {
        private final List<LogEvent> events = new ArrayList<>();

        @Override
        public void append(LogEvent event) {
            events.add(event);
        }

        public List<LogEvent> getEvents() {
            return new ArrayList<>(events);
        }

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

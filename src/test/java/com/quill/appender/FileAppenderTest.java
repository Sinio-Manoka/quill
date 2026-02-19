package com.quill.appender;

import com.quill.config.Logging;
import com.quill.model.Level;
import com.quill.model.LogEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for FileAppender.
 */
class FileAppenderTest {

    @TempDir
    Path tempDir;

    private final List<LogEvent> capturedEvents = new ArrayList<>();

    @BeforeEach
    void setUp() {
        capturedEvents.clear();
    }

    @AfterEach
    void tearDown() {
        Logging.configure(c -> c.appender(capturedEvents::add).level(Level.TRACE).async(false));
    }

    @Test
    void testCreatesLogFile() {
        Path logFile = tempDir.resolve("test.log");
        FileAppender appender = new FileAppender(logFile.toString(), false);

        appender.append(new LogEvent(
                java.time.Instant.now(),
                Level.INFO,
                "Test message",
                Map.of("key", "value"),
                Map.of("ctx", "val"),
                "main",
                "TestLogger"
        ));

        appender.close();

        assertTrue(Files.exists(logFile), "Log file should exist");
    }

    @Test
    void testWritesJsonToDisk() throws IOException {
        Path logFile = tempDir.resolve("test.log");
        FileAppender appender = new FileAppender(logFile.toString(), false);

        appender.append(new LogEvent(
                java.time.Instant.now(),
                Level.INFO,
                "Test message",
                Map.of("key", "value"),
                Map.of("ctx", "val"),
                "main",
                "TestLogger"
        ));

        appender.close();

        String content = Files.readString(logFile);
        assertTrue(content.contains("\"message\":\"Test message\""));
        assertTrue(content.contains("\"key\":\"value\""));
        assertTrue(content.contains("\"_ctx\":\"val\""));
    }

    @Test
    void testCreatesParentDirectories() {
        Path nestedPath = tempDir.resolve("nested/dir/test.log");
        FileAppender appender = new FileAppender(nestedPath.toString(), false);

        appender.append(new LogEvent(
                java.time.Instant.now(),
                Level.INFO,
                "Test",
                Map.of(),
                Map.of(),
                "main",
                "Test"
        ));

        appender.close();

        assertTrue(Files.exists(nestedPath), "Log file should exist in nested directory");
        assertTrue(Files.exists(nestedPath.getParent()), "Parent directories should be created");
    }

    @Test
    void testAppendModeAddsToFile() throws IOException {
        Path logFile = tempDir.resolve("append.log");

        // Write first log
        FileAppender appender1 = new FileAppender(logFile.toString(), false);
        appender1.append(new LogEvent(
                java.time.Instant.now(),
                Level.INFO,
                "First message",
                Map.of(),
                Map.of(),
                "main",
                "Test"
        ));
        appender1.close();

        // Append second log
        FileAppender appender2 = new FileAppender(logFile.toString(), true);
        appender2.append(new LogEvent(
                java.time.Instant.now(),
                Level.INFO,
                "Second message",
                Map.of(),
                Map.of(),
                "main",
                "Test"
        ));
        appender2.close();

        String content = Files.readString(logFile);
        assertTrue(content.contains("First message"));
        assertTrue(content.contains("Second message"));

        // Count lines - should be 2
        assertEquals(2, content.lines().count());
    }

    @Test
    void testMultipleLogEvents() throws IOException {
        Path logFile = tempDir.resolve("multi.log");
        FileAppender appender = new FileAppender(logFile.toString(), false);

        for (int i = 0; i < 5; i++) {
            appender.append(new LogEvent(
                    java.time.Instant.now(),
                    Level.INFO,
                    STR."Message \{i}",
                    Map.of("index", i),
                    Map.of(),
                    "main",
                    "Test"
            ));
        }

        appender.close();

        String content = Files.readString(logFile);
        assertEquals(5, content.lines().count());
        assertTrue(content.contains("\"index\":0"));
        assertTrue(content.contains("\"index\":4"));
    }

    @Test
    void testCloseCanBeCalledMultipleTimes() {
        Path logFile = tempDir.resolve("close.log");
        FileAppender appender = new FileAppender(logFile.toString(), false);

        appender.append(new LogEvent(
                java.time.Instant.now(),
                Level.INFO,
                "Test",
                Map.of(),
                Map.of(),
                "main",
                "Test"
        ));

        appender.close();
        appender.close(); // Should not throw

        assertTrue(Files.exists(logFile));
    }
}

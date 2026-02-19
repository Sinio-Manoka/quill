package com.quill.appender;

import com.quill.model.Level;
import com.quill.model.LogEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RollingFileAppender.
 */
class RollingFileAppenderTest {

    @Test
    void testRollsWhenFileExceedsMaxSize(@TempDir Path tempDir) throws IOException {
        Path logFile = tempDir.resolve("rolling.log");
        long maxSize = 500; // Very small for testing
        RollingFileAppender appender = new RollingFileAppender(logFile.toString(), maxSize);

        // Write enough data to trigger rolling
        for (int i = 0; i < 10; i++) {
            appender.append(new LogEvent(
                    java.time.Instant.now(),
                    Level.INFO,
                    STR."Message with some padding to make it longer \{i}",
                    Map.of("data", "x".repeat(50)), // Add 50 chars of data
                    Map.of(),
                    "main",
                    "Test"
            ));
        }

        appender.close();

        // Check that rolled files exist
        assertTrue(Files.exists(logFile), "Current log file should exist");
        assertTrue(Files.list(tempDir)
                        .filter(p -> p.getFileName().toString().startsWith("rolling"))
                        .count() >= 2,
                "At least 2 files should exist (current + rolled)");
    }

    @Test
    void testRolledFileHasTimestampSuffix(@TempDir Path tempDir) throws IOException {
        Path logFile = tempDir.resolve("test.log");
        long maxSize = 200; // Very small for testing
        RollingFileAppender appender = new RollingFileAppender(logFile.toString(), maxSize);

        // Write enough to trigger rolling
        for (int i = 0; i < 5; i++) {
            appender.append(new LogEvent(
                    java.time.Instant.now(),
                    Level.INFO,
                    STR."Long message \{i}\{" ".repeat(50)}",
                    Map.of("data", "x".repeat(30)),
                    Map.of(),
                    "main",
                    "Test"
            ));
        }

        appender.close();

        // Check for rolled file with timestamp pattern
        boolean foundRolledFile = Files.list(tempDir)
                .anyMatch(p -> p.getFileName().toString().matches("test_\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}\\.log"));

        assertTrue(foundRolledFile, "Should find a rolled file with timestamp suffix");
    }

    @Test
    void testRespectsMaxFileSize(@TempDir Path tempDir) throws IOException {
        Path logFile = tempDir.resolve("size-test.log");
        long maxSize = 1000; // 1KB max
        RollingFileAppender appender = new RollingFileAppender(logFile.toString(), maxSize);

        // Write many small messages
        for (int i = 0; i < 100; i++) {
            appender.append(new LogEvent(
                    java.time.Instant.now(),
                    Level.INFO,
                    STR."Msg \{i}",
                    Map.of(),
                    Map.of(),
                    "main",
                    "Test"
            ));
        }

        appender.close();

        // Check current file size is under limit
        assertTrue(Files.size(logFile) < maxSize + 2000, "Current file should not be significantly larger than max");
    }

    @Test
    void testCreatesParentDirectories(@TempDir Path tempDir) {
        Path nestedPath = tempDir.resolve("deep/nested/rolling.log");
        RollingFileAppender appender = new RollingFileAppender(nestedPath.toString(), 10000);

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

        assertTrue(Files.exists(nestedPath));
    }

    @Test
    void testContinuesLoggingAfterRoll(@TempDir Path tempDir) throws IOException {
        Path logFile = tempDir.resolve("continue.log");
        long maxSize = 300;
        RollingFileAppender appender = new RollingFileAppender(logFile.toString(), maxSize);

        // Write enough to trigger roll
        for (int i = 0; i < 10; i++) {
            appender.append(new LogEvent(
                    java.time.Instant.now(),
                    Level.INFO,
                    STR."Message \{i}",
                    Map.of("padding", "x".repeat(30)),
                    Map.of(),
                    "main",
                    "Test"
            ));
        }

        appender.close();

        // Should have content in the file
        String content = Files.readString(logFile);
        assertFalse(content.isEmpty(), "Current file should have content");

        // Should have rolled files
        long rolledFileCount = Files.list(tempDir)
                .filter(p -> p.getFileName().toString().startsWith("continue"))
                .count();
        assertTrue(rolledFileCount >= 2, "Should have at least current + rolled files");
    }

    @Test
    void testExistingFileIsRolledIfTooLarge(@TempDir Path tempDir) throws IOException {
        Path logFile = tempDir.resolve("existing.log");

        // Create a file that's larger than max size
        Files.writeString(logFile, "x".repeat(2000));

        long maxSize = 1000;
        RollingFileAppender appender = new RollingFileAppender(logFile.toString(), maxSize);

        appender.append(new LogEvent(
                java.time.Instant.now(),
                Level.INFO,
                "New message",
                Map.of(),
                Map.of(),
                "main",
                "Test"
        ));

        appender.close();

        // Old file should have been rolled
        long rolledCount = Files.list(tempDir)
                .filter(p -> p.getFileName().toString().startsWith("existing"))
                .count();
        assertTrue(rolledCount >= 2, "Existing large file should have been rolled");

        // Current file should have new content
        String content = Files.readString(logFile);
        assertTrue(content.contains("New message"));
    }
}

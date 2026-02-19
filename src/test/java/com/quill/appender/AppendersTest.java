package com.quill.appender;

import com.quill.model.Level;
import com.quill.model.LogEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Appenders factory class.
 */
@DisplayName("Appenders Factory Tests")
class AppendersTest {

    @Test
    @DisplayName("console() returns ConsoleAppender without colors")
    void consoleReturnsAppender() {
        Appender appender = Appenders.console();
        assertInstanceOf(ConsoleAppender.class, appender);
    }

    @Test
    @DisplayName("console(false) returns ConsoleAppender without colors")
    void consoleWithoutColors() {
        Appender appender = Appenders.console(false);
        assertInstanceOf(ConsoleAppender.class, appender);
    }

    @Test
    @DisplayName("console(true) returns ConsoleAppender with colors")
    void consoleWithColors() {
        Appender appender = Appenders.console(true);
        assertInstanceOf(ConsoleAppender.class, appender);
    }

    @Test
    @DisplayName("jsonConsole() returns JsonConsoleAppender")
    void jsonConsoleReturnsAppender() {
        Appender appender = Appenders.jsonConsole();
        assertInstanceOf(JsonConsoleAppender.class, appender);
    }

    @Test
    @DisplayName("Each call returns a new instance")
    void eachCallReturnsNewInstance() {
        Appender appender1 = Appenders.console();
        Appender appender2 = Appenders.console();
        assertNotSame(appender1, appender2);
    }

    @Test
    @DisplayName("file() returns FileAppender that overwrites")
    void fileReturnsFileAppender(@TempDir Path tempDir) {
        Path testFile = tempDir.resolve("test.log");
        FileAppender appender = Appenders.file(testFile.toString());
        assertInstanceOf(FileAppender.class, appender);
        // Clean up
        appender.close();
    }

    @Test
    @DisplayName("file(filePath, true) returns FileAppender that appends")
    void fileWithAppendReturnsFileAppender(@TempDir Path tempDir) {
        Path testFile = tempDir.resolve("test-append.log");
        FileAppender appender = Appenders.file(testFile.toString(), true);
        assertInstanceOf(FileAppender.class, appender);
        // Clean up
        appender.close();
    }

    @Test
    @DisplayName("file(filePath, false) returns FileAppender that overwrites")
    void fileWithOverwriteReturnsFileAppender(@TempDir Path tempDir) {
        Path testFile = tempDir.resolve("test-overwrite.log");
        FileAppender appender = Appenders.file(testFile.toString(), false);
        assertInstanceOf(FileAppender.class, appender);
        // Clean up
        appender.close();
    }

    @Test
    @DisplayName("rollingFile() returns RollingFileAppender with default max size")
    void rollingFileReturnsAppender(@TempDir Path tempDir) {
        Path testFile = tempDir.resolve("rolling.log");
        RollingFileAppender appender = Appenders.rollingFile(testFile.toString());
        assertInstanceOf(RollingFileAppender.class, appender);
        // Clean up
        appender.close();
    }

    @Test
    @DisplayName("rollingFile(filePath, maxBytes) returns RollingFileAppender with custom max size")
    void rollingFileWithCustomMaxSizeReturnsAppender(@TempDir Path tempDir) {
        Path testFile = tempDir.resolve("rolling-custom.log");
        long customMaxSize = 5_000_000; // 5MB
        RollingFileAppender appender = Appenders.rollingFile(testFile.toString(), customMaxSize);
        assertInstanceOf(RollingFileAppender.class, appender);
        // Verify the appender works by appending an event
        assertDoesNotThrow(() -> appender.append(new LogEvent(
                Instant.now(),
                Level.INFO,
                "Test",
                Map.of(),
                Map.of(),
                "main",
                "Test"
        )));
        // Clean up
        appender.close();
    }

    @Test
    @DisplayName("async() returns AsyncAppender with default queue size")
    void asyncReturnsAsyncAppender() {
        Appender delegate = Appenders.console();
        AsyncAppender appender = Appenders.async(delegate);
        assertInstanceOf(AsyncAppender.class, appender);
        assertEquals(1000, appender.getQueueCapacity());
        // Clean up
        appender.shutdown();
    }

    @Test
    @DisplayName("async(delegate, queueSize, dropOnFull) returns AsyncAppender with custom settings")
    void asyncWithCustomSettingsReturnsAsyncAppender() {
        Appender delegate = Appenders.console();
        int customQueueSize = 500;
        AsyncAppender appender = Appenders.async(delegate, customQueueSize, true);
        assertInstanceOf(AsyncAppender.class, appender);
        assertEquals(customQueueSize, appender.getQueueCapacity());
        // Clean up
        appender.shutdown();
    }

    @Test
    @DisplayName("async with dropOnFull=false creates blocking async appender")
    void asyncWithBlockingModeReturnsAsyncAppender() {
        Appender delegate = Appenders.console();
        AsyncAppender appender = Appenders.async(delegate, 100, false);
        assertInstanceOf(AsyncAppender.class, appender);
        assertEquals(100, appender.getQueueCapacity());
        // Clean up
        appender.shutdown();
    }

    @Test
    @DisplayName("chain() with varargs returns CompositeAppender")
    void chainWithVarargsReturnsCompositeAppender() {
        Appender appender1 = Appenders.console();
        Appender appender2 = Appenders.jsonConsole();
        Appender composite = Appenders.chain(appender1, appender2);
        assertInstanceOf(CompositeAppender.class, composite);
    }

    @Test
    @DisplayName("chain() with list returns CompositeAppender")
    void chainWithListReturnsCompositeAppender() {
        List<Appender> appenders = new ArrayList<>();
        appenders.add(Appenders.console());
        appenders.add(Appenders.jsonConsole());
        Appender composite = Appenders.chain(appenders);
        assertInstanceOf(CompositeAppender.class, composite);
    }

    @Test
    @DisplayName("chain() with empty list throws IllegalArgumentException")
    void chainWithEmptyListThrowsException() {
        List<Appender> appenders = List.of();
        assertThrows(IllegalArgumentException.class, () -> Appenders.chain(appenders));
    }

    @Test
    @DisplayName("chain() with single appender returns CompositeAppender")
    void chainWithSingleAppenderReturnsCompositeAppender() {
        Appender appender = Appenders.console();
        List<Appender> appenders = List.of(appender);
        Appender composite = Appenders.chain(appenders);
        assertInstanceOf(CompositeAppender.class, composite);
    }
}

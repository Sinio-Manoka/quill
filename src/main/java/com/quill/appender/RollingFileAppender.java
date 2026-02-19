package com.quill.appender;

import com.quill.model.LogEvent;
import com.quill.util.JsonWriter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * File appender with automatic log rotation based on file size.
 * When the current log file exceeds the max size, it is renamed
 * with a timestamp suffix and a new file is created.
 */
public class RollingFileAppender implements Appender {

    private final Path basePath;
    private final long maxFileSize;
    private final DateTimeFormatter timestampFormatter;
    private final DateTimeFormatter rollTimestampFormatter;
    private BufferedWriter writer;
    private final AtomicLong currentSize;

    /**
     * Creates a rolling file appender with the specified file path and max size.
     *
     * @param filePath the path to the log file
     * @param maxBytes the maximum size in bytes before rotation (e.g., 10_000_000 for 10MB)
     */
    public RollingFileAppender(String filePath, long maxBytes) {
        this.basePath = Paths.get(filePath);
        this.maxFileSize = maxBytes;
        this.timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .withZone(java.time.ZoneOffset.UTC);
        this.rollTimestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
                .withZone(java.time.ZoneOffset.UTC);
        this.currentSize = new AtomicLong(0);

        initializeWriter();
    }

    /**
     * Creates a rolling file appender with a default max size of 10MB.
     *
     * @param filePath the path to the log file
     */
    public RollingFileAppender(String filePath) {
        this(filePath, 10_000_000); // 10MB default
    }

    private void initializeWriter() {
        try {
            // Create parent directories if they don't exist
            if (basePath.getParent() != null) {
                Files.createDirectories(basePath.getParent());
            }

            // Check if file exists and get its size
            if (Files.exists(basePath)) {
                long existingSize = Files.size(basePath);
                // If file is at or over max size, roll it first
                if (existingSize >= maxFileSize) {
                    rollFile();
                } else {
                    currentSize.set(existingSize);
                }
            }

            // Open/create file in append mode
            StandardOpenOption option = Files.exists(basePath) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE;
            writer = Files.newBufferedWriter(basePath,
                    StandardOpenOption.CREATE,
                    option);

        } catch (IOException e) {
            throw new RuntimeException(STR."Failed to initialize RollingFileAppender for path: \{basePath}", e);
        }
    }

    @Override
    public void append(LogEvent event) {
        try {
            String jsonLine = formatAsJson(event);
            byte[] lineBytes = (jsonLine + System.lineSeparator()).getBytes();
            long newLineSize = lineBytes.length;

            // Check if we need to roll before writing
            if (currentSize.get() + newLineSize > maxFileSize) {
                rollFile();
            }

            writer.write(jsonLine);
            writer.newLine();
            writer.flush();

            currentSize.addAndGet(newLineSize);

        } catch (IOException e) {
            System.err.println(STR."Error writing to rolling log file: \{e.getMessage()}");
        }
    }

    /**
     * Rolls the current log file by renaming it with a timestamp suffix
     * and creating a new file.
     */
    private synchronized void rollFile() {
        try {
            // Close current writer
            if (writer != null) {
                writer.close();
            }

            // Rename current file with timestamp
            if (Files.exists(basePath)) {
                String timestamp = rollTimestampFormatter.format(Instant.now());
                String baseName = basePath.getFileName().toString();
                String nameWithoutExt = baseName.contains(".")
                        ? baseName.substring(0, baseName.lastIndexOf('.'))
                        : baseName;
                String extension = baseName.contains(".")
                        ? baseName.substring(baseName.lastIndexOf('.'))
                        : "";

                Path rolledPath = basePath.resolveSibling(STR."\{nameWithoutExt}_\{timestamp}\{extension}");
                Files.move(basePath, rolledPath);
            }

            // Create new writer
            writer = Files.newBufferedWriter(basePath,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE);
            currentSize.set(0);

        } catch (IOException e) {
            System.err.println(STR."Error rolling log file: \{e.getMessage()}");
            // Try to reinitialize writer on failure
            try {
                writer = Files.newBufferedWriter(basePath,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE);
                currentSize.set(0);
            } catch (IOException ex) {
                System.err.println(STR."Failed to reinitialize writer after roll error: \{ex.getMessage()}");
            }
        }
    }

    /**
     * Formats the log event as a single-line JSON string.
     */
    private String formatAsJson(LogEvent event) {
        JsonWriter jsonWriter = new JsonWriter();
        jsonWriter.beginObject();

        jsonWriter.key("timestamp").value(formatTimestamp(event.timestamp()));
        jsonWriter.key("level").value(event.level().name());
        jsonWriter.key("logger").value(event.loggerName());
        jsonWriter.key("thread").value(event.threadName());
        jsonWriter.key("message").value(event.message());

        // Structured fields
        for (Map.Entry<String, Object> entry : event.fields().entrySet()) {
            jsonWriter.key(entry.getKey()).value(entry.getValue());
        }

        // Context (prefixed with _)
        for (Map.Entry<String, String> entry : event.context().entrySet()) {
            jsonWriter.key(STR."_\{entry.getKey()}").value(entry.getValue());
        }

        jsonWriter.endObject();
        return jsonWriter.toString();
    }

    private String formatTimestamp(Instant instant) {
        return timestampFormatter.format(instant);
    }

    /**
     * Closes the file writer and releases resources.
     */
    public void close() {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            System.err.println(STR."Error closing rolling log file: \{e.getMessage()}");
        }
    }
}

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

/**
 * File appender that outputs log events as JSON to a file.
 * Each log event is written as a single line.
 */
public class FileAppender implements Appender {

    private final DateTimeFormatter timestampFormatter;
    private final BufferedWriter writer;

    /**
     * Creates a file appender that writes to the specified file path.
     *
     * @param filePath the path to the log file
     */
    public FileAppender(String filePath) {
        this(filePath, false);
    }

    /**
     * Creates a file appender that writes to the specified file path.
     *
     * @param filePath     the path to the log file
     * @param appendToFile if true, append to existing file; if false, overwrite
     */
    public FileAppender(String filePath, boolean appendToFile) {
        Path filePath1 = Paths.get(filePath);
        this.timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .withZone(java.time.ZoneOffset.UTC);
        try {
            // Create parent directories if they don't exist
            if (filePath1.getParent() != null) {
                Files.createDirectories(filePath1.getParent());
            }
            // Open file in append mode by default
            StandardOpenOption option = appendToFile ? StandardOpenOption.APPEND : StandardOpenOption.CREATE;
            this.writer = Files.newBufferedWriter(filePath1,
                    StandardOpenOption.CREATE,
                    option);
        } catch (IOException e) {
            throw new RuntimeException(STR."Failed to initialize FileAppender for path: \{filePath}", e);
        }
    }

    @Override
    public void append(LogEvent event) {
        try {
            String jsonLine = formatAsJson(event);
            writer.write(jsonLine);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            // Don't throw - log errors shouldn't crash the application
            System.err.println(STR."Error writing to log file: \{e.getMessage()}");
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
     * Should be called when the application shuts down.
     */
    public void close() {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            System.err.println(STR."Error closing log file: \{e.getMessage()}");
        }
    }
}

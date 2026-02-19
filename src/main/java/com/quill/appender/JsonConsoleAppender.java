package com.quill.appender;

import com.quill.model.LogEvent;
import com.quill.util.JsonWriter;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Console appender that outputs log events as single-line JSON to stdout.
 * Uses a zero-dependency JSON writer for serialization.
 */
public class JsonConsoleAppender implements Appender {

    // ISO-8601 format for timestamps
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(java.time.ZoneOffset.UTC);

    @Override
    public void append(LogEvent event) {
        JsonWriter writer = new JsonWriter();
        writer.beginObject();

        // Timestamp
        writer.key("timestamp").value(formatTimestamp(event.timestamp()));

        // Level
        writer.key("level").value(event.level().name());

        // Logger name
        writer.key("logger").value(event.loggerName());

        // Thread name
        writer.key("thread").value(event.threadName());

        // Message
        writer.key("message").value(event.message());

        // Structured fields (flattened at root level)
        for (Map.Entry<String, Object> entry : event.fields().entrySet()) {
            writer.key(entry.getKey()).value(entry.getValue());
        }

        // Context (prefixed with _ to avoid collisions)
        for (Map.Entry<String, String> entry : event.context().entrySet()) {
            writer.key(STR."_\{entry.getKey()}").value(entry.getValue());
        }

        writer.endObject();

        System.out.println(writer);
    }

    private String formatTimestamp(Instant instant) {
        return TIMESTAMP_FORMATTER.format(instant);
    }
}

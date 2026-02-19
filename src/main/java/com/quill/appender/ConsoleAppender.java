package com.quill.appender;

import com.quill.model.LogEvent;
import com.quill.model.Level;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Console appender that outputs human-readable log messages to stdout.
 * Format: [TIMESTAMP] [LEVEL] [LOGGER] MESSAGE key=value ...
 */
public class ConsoleAppender implements Appender {

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault());

    private final boolean useColor;

    /**
     * Creates a console appender without ANSI colors.
     */
    public ConsoleAppender() {
        this(false);
    }

    /**
     * Creates a console appender with optional ANSI colors.
     *
     * @param useColor whether to use ANSI color codes for log levels
     */
    public ConsoleAppender(boolean useColor) {
        this.useColor = useColor;
    }

    @Override
    public void append(LogEvent event) {
        StringBuilder sb = new StringBuilder();

        // [TIMESTAMP]
        sb.append('[').append(formatTime(event.timestamp())).append("] ");

        // [LEVEL] with color
        sb.append('[').append(coloredLevel(event.level())).append("] ");

        // [LOGGER]
        sb.append('[').append(event.loggerName()).append("] ");

        // MESSAGE
        sb.append(event.message());

        // Fields
        for (Map.Entry<String, Object> entry : event.fields().entrySet()) {
            sb.append(' ').append(entry.getKey()).append('=').append(formatValue(entry.getValue()));
        }

        // Context (prefixed with _)
        for (Map.Entry<String, String> entry : event.context().entrySet()) {
            sb.append(" _").append(entry.getKey()).append('=').append(entry.getValue());
        }

        System.out.println(sb);
    }

    private String formatTime(Instant instant) {
        return TIME_FORMATTER.format(instant);
    }

    private String coloredLevel(Level level) {
        if (!useColor) {
            return level.name();
        }
        return switch (level) {
            case TRACE -> "\u001B[90mTRACE\u001B[0m";    // Gray
            case DEBUG -> "\u001B[36mDEBUG\u001B[0m";    // Cyan
            case INFO -> "\u001B[32mINFO\u001B[0m";      // Green
            case WARN -> "\u001B[33mWARN\u001B[0m";      // Yellow
            case ERROR -> "\u001B[31mERROR\u001B[0m";    // Red
        };
    }

    private String formatValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String) {
            return STR."\"\{value}\"";
        }
        return String.valueOf(value);
    }
}

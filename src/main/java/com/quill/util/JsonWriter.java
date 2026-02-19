package com.quill.util;

import java.util.Map;
import java.util.Objects;

/**
 * Zero-dependency JSON writer using StringBuilder.
 * Provides a fluent API for building JSON output with proper string escaping.
 */
public class JsonWriter {

    private final StringBuilder builder;
    private boolean needsComma;
    private int depth;

    public JsonWriter() {
        this.builder = new StringBuilder();
        this.needsComma = false;
        this.depth = 0;
    }

    /**
     * Starts a new JSON object.
     *
     * @return this writer for chaining
     */
    public JsonWriter beginObject() {
        appendCommaIfNeeded();
        builder.append('{');
        depth++;
        needsComma = false;
        return this;
    }

    /**
     * Ends the current JSON object.
     *
     * @return this writer for chaining
     */
    public JsonWriter endObject() {
        depth--;
        builder.append('}');
        needsComma = true;
        return this;
    }

    /**
     * Starts a new JSON array.
     *
     * @return this writer for chaining
     */
    public JsonWriter beginArray() {
        appendCommaIfNeeded();
        builder.append('[');
        depth++;
        needsComma = false;
        return this;
    }

    /**
     * Ends the current JSON array.
     *
     * @return this writer for chaining
     */
    public JsonWriter endArray() {
        depth--;
        builder.append(']');
        needsComma = true;
        return this;
    }

    /**
     * Writes a key to the current object.
     * Must be followed by a value call.
     *
     * @param key the key name
     * @return this writer for chaining
     */
    public JsonWriter key(String key) {
        Objects.requireNonNull(key, "key must not be null");
        appendCommaIfNeeded();
        writeQuoted(key);
        builder.append(':');
        needsComma = false;
        return this;
    }

    /**
     * Writes a value of any supported type.
     * Supports: String, Number, Boolean, null, Map, Iterable.
     *
     * @param value the value to write
     * @return this writer for chaining
     */
    public JsonWriter value(Object value) {
        appendCommaIfNeeded();
        writeValue(value);
        needsComma = true;
        return this;
    }

    /**
     * Returns the built JSON string.
     *
     * @return the JSON string
     */
    @Override
    public String toString() {
        return builder.toString();
    }

    /**
     * Resets the writer for reuse.
     */
    public void reset() {
        builder.setLength(0);
        needsComma = false;
        depth = 0;
    }

    private void appendCommaIfNeeded() {
        if (needsComma) {
            builder.append(',');
        }
    }

    private void writeValue(Object value) {
        switch (value) {
            case null -> builder.append("null");
            case String str -> writeQuoted(str);
            case Number num -> writeNumber(num);
            case Boolean bool -> builder.append(bool);
            case Character ch -> writeQuoted(ch.toString());
            case Map<?, ?> map -> writeMap(map);
            case Iterable<?> iterable -> writeIterable(iterable);
            default ->
                // Fallback: toString representation as string
                    writeQuoted(value.toString());
        }
    }

    private void writeQuoted(String value) {
        builder.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> builder.append("\\\"");
                case '\\' -> builder.append("\\\\");
                case '\b' -> builder.append("\\b");
                case '\f' -> builder.append("\\f");
                case '\n' -> builder.append("\\n");
                case '\r' -> builder.append("\\r");
                case '\t' -> builder.append("\\t");
                default -> {
                    if (c <= 0x1F) {
                        // Control characters: unicode escape format
                        builder.append(String.format("\\u%04x", (int) c));
                    } else {
                        builder.append(c);
                    }
                }
            }
        }
        builder.append('"');
    }

    private void writeNumber(Number number) {
        if (number instanceof Double || number instanceof Float) {
            double d = number.doubleValue();
            if (Double.isNaN(d) || Double.isInfinite(d)) {
                builder.append("null");
            } else {
                builder.append(number);
            }
        } else {
            builder.append(number);
        }
    }

    private void writeMap(Map<?, ?> map) {
        builder.append('{');
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) {
                builder.append(',');
            }
            first = false;
            writeQuoted(String.valueOf(entry.getKey()));
            builder.append(':');
            writeValue(entry.getValue());
        }
        builder.append('}');
    }

    private void writeIterable(Iterable<?> iterable) {
        builder.append('[');
        boolean first = true;
        for (Object item : iterable) {
            if (!first) {
                builder.append(',');
            }
            first = false;
            writeValue(item);
        }
        builder.append(']');
    }
}

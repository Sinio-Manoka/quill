package com.quill.api;

import com.quill.config.Logging;
import com.quill.model.Level;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the LogBuilder interface and DefaultLogBuilder implementation.
 */
@DisplayName("LogBuilder Tests")
class LogBuilderTest {

    @AfterEach
    void resetConfig() {
        Logging.reset();
        LoggerFactory.reset();
    }

    @Test
    @DisplayName("LogBuilder.field can be chained")
    void logBuilderFieldCanBeChained() {
        Logger logger = LoggerFactory.get("TestLogger");
        LogBuilder builder = logger.info("Message");

        // Should not throw
        assertDoesNotThrow(() -> builder.field("key1", "value1")
                .field("key2", "value2")
                .field("key3", "value3")
                .emit());
    }

    @Test
    @DisplayName("LogBuilder.emit produces output")
    void logBuilderEmitProducesOutput() {
        ByteArrayOutputStream output = captureOutput(() -> {
            Logging.configure(config -> config.level(Level.INFO));
            Logger logger = LoggerFactory.get("TestLogger");
            logger.info("Test message").emit();
        });

        String result = output.toString();
        assertFalse(result.isEmpty(), "Should produce output");
    }

    @Test
    @DisplayName("LogBuilder includes fields in output")
    void logBuilderIncludesFieldsInOutput() {
        ByteArrayOutputStream output = captureOutput(() -> {
            Logging.configure(config -> config.level(Level.INFO));
            Logger logger = LoggerFactory.get("TestLogger");
            logger.info("Message")
                    .field("userId", 42)
                    .field("action", "login")
                    .emit();
        });

        String result = output.toString();
        assertTrue(result.contains("userId") || result.contains("\"userId\""),
                "Should contain userId field");
        assertTrue(result.contains("42"), "Should contain userId value");
    }

    @Test
    @DisplayName("LogBuilder handles different data types")
    void logBuilderHandlesDifferentDataTypes() {
        Logger logger = LoggerFactory.get("TestLogger");

        assertDoesNotThrow(() -> logger.info("Message")
                .field("string", "value")
                .field("integer", 42)
                .field("long", 123L)
                .field("double", 3.14)
                .field("boolean", true)
                .field("nullValue", null)
                .emit());
    }

    @Test
    @DisplayName("LogBuilder handles null keys gracefully")
    void logBuilderHandlesNullKeys() {
        Logger logger = LoggerFactory.get("TestLogger");
        LogBuilder builder = logger.info("Message");

        assertThrows(NullPointerException.class,
                () -> builder.field(null, "value"));
    }

    @Test
    @DisplayName("Multiple builders can be created")
    void multipleBuildersCanBeCreated() {
        Logger logger = LoggerFactory.get("TestLogger");

        LogBuilder builder1 = logger.info("Message1");
        LogBuilder builder2 = logger.debug("Message2");
        LogBuilder builder3 = logger.error("Message3");

        assertNotNull(builder1);
        assertNotNull(builder2);
        assertNotNull(builder3);

        // All should work independently
        assertDoesNotThrow(() -> {
            builder1.emit();
            builder2.emit();
            builder3.emit();
        });
    }

    @Test
    @DisplayName("LogBuilder with empty string key")
    void logBuilderWithEmptyStringKey() {
        Logger logger = LoggerFactory.get("TestLogger");

        // Empty string key is valid (though not recommended)
        assertDoesNotThrow(() -> logger.info("Message").field("", "value").emit());
    }

    @Test
    @DisplayName("LogBuilder handles special characters in keys")
    void logBuilderHandlesSpecialCharactersInKeys() {
        Logger logger = LoggerFactory.get("TestLogger");

        assertDoesNotThrow(() -> logger.info("Message")
                .field("key-with-dash", "value")
                .field("key_with_underscore", "value")
                .field("key.with.dots", "value")
                .emit());
    }

    @Test
    @DisplayName("LogBuilder can add many fields")
    void logBuilderCanAddManyFields() {
        Logger logger = LoggerFactory.get("TestLogger");
        LogBuilder builder = logger.info("Message");

        // Add 20 fields
        for (int i = 0; i < 20; i++) {
            builder.field(STR."field\{i}", i);
        }

        assertDoesNotThrow(builder::emit);
    }

    @Test
    @DisplayName("LogBuilder fields are captured at emit time")
    void logBuilderFieldsCapturedAtEmitTime() {
        ByteArrayOutputStream output = captureOutput(() -> {
            Logging.configure(config -> config.level(Level.INFO));
            Logger logger = LoggerFactory.get("TestLogger");

            LogBuilder builder = logger.info("Message");
            builder.field("step", 1);
            builder.field("step", 2);  // Overwrite
            builder.emit();
        });

        String result = output.toString();
        // The last value should win
        assertTrue(result.contains("2"));
    }

    @Test
    @DisplayName("LogBuilder.fields() with empty array returns same builder")
    void logBuilderFieldsEmptyArray() {
        Logger logger = LoggerFactory.get("TestLogger");
        LogBuilder builder = logger.info("Message");

        // Empty array should return the same builder
        assertDoesNotThrow(() -> {
            LogBuilder result = builder.fields();
            assertNotNull(result);
            result.emit();
        });
    }

    @Test
    @DisplayName("LogBuilder.fields() with null array returns same builder")
    void logBuilderFieldsNullArray() {
        Logger logger = LoggerFactory.get("TestLogger");
        LogBuilder builder = logger.info("Message");

        // Null array should return the same builder
        assertDoesNotThrow(() -> {
            LogBuilder result = builder.fields((Object[]) null);
            assertNotNull(result);
            result.emit();
        });
    }

    @Test
    @DisplayName("LogBuilder.fields() with valid key-value pairs")
    void logBuilderFieldsValidPairs() {
        ByteArrayOutputStream output = captureOutput(() -> {
            Logging.configure(config -> config.level(Level.INFO));
            Logger logger = LoggerFactory.get("TestLogger");
            logger.info("Message")
                    .fields("key1", "value1", "key2", 42, "key3", true)
                    .emit();
        });

        String result = output.toString();
        assertTrue(result.contains("key1") && result.contains("value1"));
        assertTrue(result.contains("42"));
        assertTrue(result.contains("true") || result.contains("TRUE"));
    }

    @Test
    @DisplayName("LogBuilder.fields() with odd number of arguments throws exception")
    void logBuilderFieldsOddNumberOfArguments() {
        Logger logger = LoggerFactory.get("TestLogger");
        LogBuilder builder = logger.info("Message");

        // Odd number of arguments should throw IllegalArgumentException
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> builder.fields("key1", "value1", "key2")
        );
        assertTrue(ex.getMessage().contains("even length"));
    }

    @Test
    @DisplayName("LogBuilder.fields() can be chained with field()")
    void logBuilderFieldsChaining() {
        Logger logger = LoggerFactory.get("TestLogger");

        assertDoesNotThrow(() -> logger.info("Message")
                .fields("a", 1, "b", 2)
                .field("c", 3)
                .emit());
    }

    @Test
    @DisplayName("LogBuilder.fields() with single pair")
    void logBuilderFieldsSinglePair() {
        Logger logger = LoggerFactory.get("TestLogger");

        assertDoesNotThrow(() -> logger.info("Message")
                .fields("single", "value")
                .emit());
    }

    @Test
    @DisplayName("LogBuilder.fields() with many pairs")
    void logBuilderFieldsManyPairs() {
        Logger logger = LoggerFactory.get("TestLogger");

        // Create 10 pairs
        Object[] pairs = new Object[20];
        for (int i = 0; i < 10; i++) {
            pairs[i * 2] = "key" + i;
            pairs[i * 2 + 1] = i;
        }

        assertDoesNotThrow(() -> logger.info("Message")
                .fields(pairs)
                .emit());
    }

    @Test
    @DisplayName("LogBuilder.fields() with null values")
    void logBuilderFieldsWithNullValues() {
        Logger logger = LoggerFactory.get("TestLogger");

        assertDoesNotThrow(() -> logger.info("Message")
                .fields("nullKey", null, "validKey", "value")
                .emit());
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

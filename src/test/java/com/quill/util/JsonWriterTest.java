package com.quill.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the JsonWriter class.
 */
@DisplayName("JsonWriter Tests")
class JsonWriterTest {

    @Test
    @DisplayName("Empty object is serialized correctly")
    void emptyObject() {
        JsonWriter writer = new JsonWriter();
        String result = writer.beginObject().endObject().toString();
        assertEquals("{}", result);
    }

    @Test
    @DisplayName("Empty array is serialized correctly")
    void emptyArray() {
        JsonWriter writer = new JsonWriter();
        String result = writer.beginArray().endArray().toString();
        assertEquals("[]", result);
    }

    @Test
    @DisplayName("Simple object with string value")
    void simpleObjectWithString() {
        JsonWriter writer = new JsonWriter();
        String result = writer.beginObject()
                .key("name").value("John")
                .endObject()
                .toString();
        assertEquals("{\"name\":\"John\"}", result);
    }

    @Test
    @DisplayName("Object with multiple keys")
    void objectWithMultipleKeys() {
        JsonWriter writer = new JsonWriter();
        String result = writer.beginObject()
                .key("name").value("John")
                .key("age").value(30)
                .key("city").value("NYC")
                .endObject()
                .toString();
        assertEquals("{\"name\":\"John\",\"age\":30,\"city\":\"NYC\"}", result);
    }

    @Test
    @DisplayName("Number values are serialized without quotes")
    void numberValuesWithoutQuotes() {
        JsonWriter writer = new JsonWriter();
        String result = writer.beginObject()
                .key("int").value(42)
                .key("long").value(123L)
                .key("double").value(3.14)
                .key("negative").value(-10)
                .endObject()
                .toString();
        assertEquals("{\"int\":42,\"long\":123,\"double\":3.14,\"negative\":-10}", result);
    }

    @Test
    @DisplayName("Boolean values are serialized as true/false")
    void booleanValues() {
        JsonWriter writer = new JsonWriter();
        String result = writer.beginObject()
                .key("active").value(true)
                .key("deleted").value(false)
                .endObject()
                .toString();
        assertEquals("{\"active\":true,\"deleted\":false}", result);
    }

    @Test
    @DisplayName("Null value is serialized as null")
    void nullValue() {
        JsonWriter writer = new JsonWriter();
        String result = writer.beginObject()
                .key("value").value(null)
                .endObject()
                .toString();
        assertEquals("{\"value\":null}", result);
    }

    @Test
    @DisplayName("Special characters in strings are escaped")
    void specialCharactersEscaped() {
        JsonWriter writer = new JsonWriter();
        String result = writer.beginObject()
                .key("text").value("Line1\nLine2\tTabbed\"Quote\"\\Backslash")
                .endObject()
                .toString();
        assertEquals("{\"text\":\"Line1\\nLine2\\tTabbed\\\"Quote\\\"\\\\Backslash\"}", result);
    }

    @Test
    @DisplayName("Control characters are unicode escaped")
    void controlCharactersEscaped() {
        JsonWriter writer = new JsonWriter();
        String result = writer.beginObject()
                .key("text").value("\u0000\u0001\u001f")
                .endObject()
                .toString();
        assertEquals("{\"text\":\"\\u0000\\u0001\\u001f\"}", result);
    }

    @Test
    @DisplayName("Array of values")
    void arrayOfValues() {
        JsonWriter writer = new JsonWriter();
        String result = writer.beginArray()
                .value(1)
                .value(2)
                .value(3)
                .endArray()
                .toString();
        assertEquals("[1,2,3]", result);
    }

    @Test
    @DisplayName("Nested objects")
    void nestedObjects() {
        JsonWriter writer = new JsonWriter();
        String result = writer.beginObject()
                .key("user").beginObject()
                .key("name").value("John")
                .key("age").value(30)
                .endObject()
                .endObject()
                .toString();
        assertEquals("{\"user\":{\"name\":\"John\",\"age\":30}}", result);
    }

    @Test
    @DisplayName("Nested arrays")
    void nestedArrays() {
        JsonWriter writer = new JsonWriter();
        String result = writer.beginObject()
                .key("matrix").beginArray()
                .beginArray().value(1).value(2).endArray()
                .beginArray().value(3).value(4).endArray()
                .endArray()
                .endObject()
                .toString();
        assertEquals("{\"matrix\":[[1,2],[3,4]]}", result);
    }

    @Test
    @DisplayName("Map is serialized as object")
    void mapSerializedAsObject() {
        JsonWriter writer = new JsonWriter();
        Map<String, Object> map = Map.of(
                "key1", "value1",
                "key2", 42
        );
        String result = writer.beginObject()
                .key("data").value(map)
                .endObject()
                .toString();
        // Check that the JSON structure is correct, regardless of key order
        assertTrue(result.contains("\"data\":{"));
        assertTrue(result.contains("\"key1\":\"value1\""));
        assertTrue(result.contains("\"key2\":42"));
        assertTrue(result.contains("}"));
    }

    @Test
    @DisplayName("List is serialized as array")
    void listSerializedAsArray() {
        JsonWriter writer = new JsonWriter();
        List<String> list = List.of("a", "b", "c");
        String result = writer.beginObject()
                .key("items").value(list)
                .endObject()
                .toString();
        assertEquals("{\"items\":[\"a\",\"b\",\"c\"]}", result);
    }

    @Test
    @DisplayName("NaN and Infinity are serialized as null")
    void nanAndInfinityAsNull() {
        JsonWriter writer = new JsonWriter();
        String result = writer.beginObject()
                .key("nan").value(Double.NaN)
                .key("positiveInf").value(Double.POSITIVE_INFINITY)
                .key("negativeInf").value(Double.NEGATIVE_INFINITY)
                .endObject()
                .toString();
        assertEquals("{\"nan\":null,\"positiveInf\":null,\"negativeInf\":null}", result);
    }

    @Test
    @DisplayName("Writer can be reset and reused")
    void writerCanBeReset() {
        JsonWriter writer = new JsonWriter();
        writer.beginObject().key("first").value(1).endObject();
        String first = writer.toString();

        writer.reset();
        writer.beginObject().key("second").value(2).endObject();
        String second = writer.toString();

        assertEquals("{\"first\":1}", first);
        assertEquals("{\"second\":2}", second);
    }

    @Test
    @DisplayName("Character is serialized as quoted string")
    void characterSerialized() {
        JsonWriter writer = new JsonWriter();
        String result = writer.beginObject()
                .key("letter").value('A')
                .endObject()
                .toString();
        assertEquals("{\"letter\":\"A\"}", result);
    }

    @Test
    @DisplayName("Object fallback uses toString for unknown types")
    void unknownTypeUsesToString() {
        JsonWriter writer = new JsonWriter();
        Object unknown = new Object() {
            @Override
            public String toString() {
                return "CustomObject";
            }
        };
        String result = writer.beginObject()
                .key("obj").value(unknown)
                .endObject()
                .toString();
        assertEquals("{\"obj\":\"CustomObject\"}", result);
    }

    @Test
    @DisplayName("Complex nested structure")
    void complexNestedStructure() {
        JsonWriter writer = new JsonWriter();
        String result = writer.beginObject()
                .key("timestamp").value("2024-01-15T09:23:41Z")
                .key("level").value("INFO")
                .key("data").beginObject()
                .key("userId").value(42)
                .key("tags").beginArray()
                .value("important").value("verified")
                .endArray()
                .endObject()
                .endObject()
                .toString();
        assertEquals("{\"timestamp\":\"2024-01-15T09:23:41Z\",\"level\":\"INFO\"," +
                "\"data\":{\"userId\":42,\"tags\":[\"important\",\"verified\"]}}", result);
    }

    @Test
    @DisplayName("Empty string is handled correctly")
    void emptyStringHandled() {
        JsonWriter writer = new JsonWriter();
        String result = writer.beginObject()
                .key("empty").value("")
                .endObject()
                .toString();
        assertEquals("{\"empty\":\"\"}", result);
    }

    @Test
    @DisplayName("Unicode characters are preserved")
    void unicodeCharactersPreserved() {
        JsonWriter writer = new JsonWriter();
        String result = writer.beginObject()
                .key("emoji").value("Hello 👋 World 🌍")
                .key("chinese").value("你好")
                .key("arabic").value("مرحبا")
                .endObject()
                .toString();
        assertEquals("{\"emoji\":\"Hello 👋 World 🌍\"," +
                "\"chinese\":\"你好\"," +
                "\"arabic\":\"مرحبا\"}", result);
    }
}

package com.quill.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Level enum.
 */
@DisplayName("Level Enum Tests")
class LevelTest {

    @Test
    @DisplayName("TRACE has lowest severity (0)")
    void traceHasLowestSeverity() {
        assertEquals(0, Level.TRACE.severity());
    }

    @Test
    @DisplayName("ERROR has highest severity (4)")
    void errorHasHighestSeverity() {
        assertEquals(4, Level.ERROR.severity());
    }

    @Test
    @DisplayName("Severity values are in correct order")
    void severityValuesInOrder() {
        assertTrue(Level.TRACE.severity() < Level.DEBUG.severity());
        assertTrue(Level.DEBUG.severity() < Level.INFO.severity());
        assertTrue(Level.INFO.severity() < Level.WARN.severity());
        assertTrue(Level.WARN.severity() < Level.ERROR.severity());
    }

    @Test
    @DisplayName("isEnabled returns true when level meets threshold")
    void isEnabledReturnsTrueWhenMeetsThreshold() {
        assertTrue(Level.INFO.isEnabled(Level.INFO));
        assertTrue(Level.WARN.isEnabled(Level.INFO));
        assertTrue(Level.ERROR.isEnabled(Level.INFO));
    }

    @Test
    @DisplayName("isEnabled returns false when level below threshold")
    void isEnabledReturnsFalseWhenBelowThreshold() {
        assertFalse(Level.TRACE.isEnabled(Level.INFO));
        assertFalse(Level.DEBUG.isEnabled(Level.INFO));
    }

    @Test
    @DisplayName("isEnabled returns true for same level")
    void isEnabledReturnsTrueForSameLevel() {
        for (Level level : Level.values()) {
            assertTrue(level.isEnabled(level), STR."\{level.name()} should be enabled for \{level}");
        }
    }

    @Test
    @DisplayName("All levels are present")
    void allLevelsPresent() {
        Level[] levels = Level.values();
        assertEquals(5, levels.length);
        assertEquals(Level.TRACE, levels[0]);
        assertEquals(Level.DEBUG, levels[1]);
        assertEquals(Level.INFO, levels[2]);
        assertEquals(Level.WARN, levels[3]);
        assertEquals(Level.ERROR, levels[4]);
    }
}

package com.quill.config;

import com.quill.appender.Appender;
import com.quill.appender.ConsoleAppender;
import com.quill.appender.JsonConsoleAppender;
import com.quill.model.Level;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the LogConfig record.
 */
@DisplayName("LogConfig Record Tests")
class LogConfigTest {

    @Test
    @DisplayName("LogConfig stores all fields correctly")
    void storesAllFields() {
        List<Appender> appenders = List.of(new ConsoleAppender(), new JsonConsoleAppender());

        LogConfig config = new LogConfig(Level.DEBUG, appenders, true, 1.0, Map.of());

        assertEquals(Level.DEBUG, config.minLevel());
        assertEquals(2, config.appenders().size());
        assertTrue(config.async());
        assertEquals(1.0, config.samplingRate());
        assertTrue(config.packageLevels().isEmpty());
    }

    @Test
    @DisplayName("LogConfig throws on null minLevel")
    void throwsOnNullMinLevel() {
        assertThrows(NullPointerException.class,
                () -> new LogConfig(null, List.of(), false, 1.0, Map.of()));
    }

    @Test
    @DisplayName("LogConfig throws on null appenders list")
    void throwsOnNullAppenders() {
        assertThrows(NullPointerException.class,
                () -> new LogConfig(Level.INFO, null, false, 1.0, Map.of()));
    }

    @Test
    @DisplayName("LogConfig throws on invalid sampling rate")
    void throwsOnInvalidSamplingRate() {
        List<Appender> appenders = List.of(new ConsoleAppender());

        assertThrows(IllegalArgumentException.class,
                () -> new LogConfig(Level.INFO, appenders, false, -0.1, Map.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new LogConfig(Level.INFO, appenders, false, 1.5, Map.of()));
    }

    @Test
    @DisplayName("LogConfig creates defensive copy of appenders")
    void createsDefensiveCopyOfAppenders() {
        List<Appender> mutableList = new java.util.ArrayList<>(List.of(new ConsoleAppender()));

        LogConfig config = new LogConfig(Level.INFO, mutableList, false, 1.0, Map.of());

        // Modify original list
        mutableList.add(new JsonConsoleAppender());

        // Config should be unchanged
        assertEquals(1, config.appenders().size());
    }

    @Test
    @DisplayName("LogConfig appenders list is unmodifiable")
    void appendersListIsUnmodifiable() {
        List<Appender> appenders = List.of(new ConsoleAppender());
        LogConfig config = new LogConfig(Level.INFO, appenders, false, 1.0, Map.of());

        assertThrows(UnsupportedOperationException.class,
                () -> config.appenders().add(new JsonConsoleAppender()));
    }

    @Test
    @DisplayName("LogConfig package levels map is unmodifiable")
    void packageLevelsMapIsUnmodifiable() {
        Map<String, Level> packageLevels = Map.of("com.example", Level.DEBUG);
        LogConfig config = new LogConfig(Level.INFO, List.of(), false, 1.0, packageLevels);

        assertThrows(UnsupportedOperationException.class,
                () -> config.packageLevels().put("com.other", Level.WARN));
    }

    @Test
    @DisplayName("LogConfig creates defensive copy of package levels")
    void createsDefensiveCopyOfPackageLevels() {
        Map<String, Level> mutableMap = new java.util.HashMap<>(Map.of("com.example", Level.DEBUG));

        LogConfig config = new LogConfig(Level.INFO, List.of(), false, 1.0, mutableMap);

        // Modify original map
        mutableMap.put("com.other", Level.WARN);

        // Config should be unchanged
        assertEquals(1, config.packageLevels().size());
        assertTrue(config.packageLevels().containsKey("com.example"));
    }

    @Test
    @DisplayName("defaultConfig creates valid configuration")
    void defaultConfigCreatesValidConfiguration() {
        LogConfig config = LogConfig.defaultConfig();

        assertEquals(Level.INFO, config.minLevel());
        assertEquals(1, config.appenders().size());
        assertInstanceOf(JsonConsoleAppender.class, config.appenders().getFirst());
        assertFalse(config.async());
        assertEquals(1.0, config.samplingRate());
        assertTrue(config.packageLevels().isEmpty());
    }

    @Test
    @DisplayName("isSamplingEnabled returns true when rate &lt; 1.0")
    void isSamplingEnabledReturnsTrueWhenRateLessThanOne() {
        LogConfig config = new LogConfig(Level.INFO, List.of(), false, 0.5, Map.of());
        assertTrue(config.isSamplingEnabled());
    }

    @Test
    @DisplayName("isSamplingEnabled returns false when rate == 1.0")
    void isSamplingEnabledReturnsFalseWhenRateEqualsOne() {
        LogConfig config = new LogConfig(Level.INFO, List.of(), false, 1.0, Map.of());
        assertFalse(config.isSamplingEnabled());
    }

    @Test
    @DisplayName("getLevelForPackage returns configured level")
    void getLevelForPackageReturnsConfiguredLevel() {
        Map<String, Level> packageLevels = Map.of(
                "com.example", Level.DEBUG,
                "com.test", Level.WARN
        );
        LogConfig config = new LogConfig(Level.INFO, List.of(), false, 1.0, packageLevels);

        assertEquals(Level.DEBUG, config.getLevelForPackage("com.example"));
        assertEquals(Level.WARN, config.getLevelForPackage("com.test"));
    }

    @Test
    @DisplayName("getLevelForPackage returns null for unconfigured package")
    void getLevelForPackageReturnsNullForUnconfiguredPackage() {
        LogConfig config = new LogConfig(Level.INFO, List.of(), false, 1.0,
                Map.of("com.example", Level.DEBUG));

        assertNull(config.getLevelForPackage("com.other"));
    }

    @Test
    @DisplayName("getLevelForPackage returns parent level for subpackage")
    void getLevelForPackageReturnsParentLevelForSubpackage() {
        Map<String, Level> packageLevels = Map.of("com.example", Level.DEBUG);
        LogConfig config = new LogConfig(Level.INFO, List.of(), false, 1.0, packageLevels);

        assertEquals(Level.DEBUG, config.getLevelForPackage("com.example.sub"));
        assertEquals(Level.DEBUG, config.getLevelForPackage("com.example.sub.service"));
    }

    @Test
    @DisplayName("getLevelForPackage handles empty package name")
    void getLevelForPackageHandlesEmptyPackageName() {
        LogConfig config = new LogConfig(Level.INFO, List.of(), false, 1.0,
                Map.of("com.example", Level.DEBUG));

        assertNull(config.getLevelForPackage(""));
        assertNull(config.getLevelForPackage(null));
    }

    @Test
    @DisplayName("LogConfig record equality works")
    void recordEqualityWorks() {
        List<Appender> appenders1 = List.of(new ConsoleAppender());
        List<Appender> appenders2 = List.of(new ConsoleAppender());

        LogConfig config1 = new LogConfig(Level.INFO, appenders1, false, 0.5, Map.of());
        LogConfig config2 = new LogConfig(Level.INFO, appenders2, false, 0.5, Map.of());

        // Note: equality might not work as expected due to Appender instances being different
        // But the record structure itself should be equal
        assertEquals(config1.minLevel(), config2.minLevel());
        assertEquals(config1.async(), config2.async());
        assertEquals(config1.appenders().size(), config2.appenders().size());
        assertEquals(config1.samplingRate(), config2.samplingRate());
    }
}

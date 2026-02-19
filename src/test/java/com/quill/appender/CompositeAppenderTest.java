package com.quill.appender;

import com.quill.model.Level;
import com.quill.model.LogEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for CompositeAppender.
 */
class CompositeAppenderTest {

    @Test
    void testDelegatesToAllAppenders() {
        List<String> results = new ArrayList<>();

        Appender appender1 = event -> results.add(STR."appender1:\{event.message()}");
        Appender appender2 = event -> results.add(STR."appender2:\{event.message()}");
        Appender appender3 = event -> results.add(STR."appender3:\{event.message()}");

        CompositeAppender composite = new CompositeAppender(appender1, appender2, appender3);

        LogEvent event = new LogEvent(
                Instant.now(),
                Level.INFO,
                "Test",
                Map.of(),
                Map.of(),
                "main",
                "Test"
        );

        composite.append(event);

        assertEquals(3, results.size());
        assertTrue(results.contains("appender1:Test"));
        assertTrue(results.contains("appender2:Test"));
        assertTrue(results.contains("appender3:Test"));
    }

    @Test
    void testDelegatesToList() {
        List<String> results = new ArrayList<>();

        List<Appender> appenders = List.of(
                event -> results.add("a"),
                event -> results.add("b"),
                event -> results.add("c")
        );

        CompositeAppender composite = new CompositeAppender(appenders);

        LogEvent event = new LogEvent(
                Instant.now(),
                Level.INFO,
                "Test",
                Map.of(),
                Map.of(),
                "main",
                "Test"
        );

        composite.append(event);

        assertEquals(3, results.size());
    }

    @Test
    void testSingleAppender() {
        List<String> results = new ArrayList<>();

        Appender appender = event -> results.add(event.message());

        CompositeAppender composite = new CompositeAppender(appender);

        LogEvent event = new LogEvent(
                Instant.now(),
                Level.INFO,
                "Single",
                Map.of(),
                Map.of(),
                "main",
                "Test"
        );

        composite.append(event);

        assertEquals(1, results.size());
        assertEquals("Single", results.getFirst());
    }

    @Test
    void testRequiresAtLeastOneAppender() {
        assertThrows(IllegalArgumentException.class, CompositeAppender::new);
    }

    @Test
    void testRequiresNonEmptyList() {
        assertThrows(IllegalArgumentException.class, () -> new CompositeAppender(List.of()));
    }

    @Test
    void testRequiresNonNullAppenders() {
        assertThrows(NullPointerException.class, () -> new CompositeAppender((Appender[]) null));
    }

    @Test
    void testRequiresNonNullList() {
        assertThrows(NullPointerException.class, () -> new CompositeAppender((List<Appender>) null));
    }

    @Test
    void testContinuesAfterOneAppenderFails() {
        List<String> results = new ArrayList<>();

        Appender failingAppender = event -> {
            throw new RuntimeException("Intentional failure");
        };

        Appender workingAppender = event -> results.add(STR."working:\{event.message()}");

        CompositeAppender composite = new CompositeAppender(failingAppender, workingAppender);

        LogEvent event = new LogEvent(
                Instant.now(),
                Level.INFO,
                "Test",
                Map.of(),
                Map.of(),
                "main",
                "Test"
        );

        // Should not throw
        assertDoesNotThrow(() -> composite.append(event));

        // Working appender should still receive event
        assertEquals(1, results.size());
        assertEquals("working:Test", results.getFirst());
    }

    @Test
    void testGetDelegates() {
        Appender appender1 = event -> {
        };
        Appender appender2 = event -> {
        };

        CompositeAppender composite = new CompositeAppender(appender1, appender2);

        List<Appender> delegates = composite.getDelegates();

        assertEquals(2, delegates.size());
        assertTrue(delegates.contains(appender1));
        assertTrue(delegates.contains(appender2));
    }

    @Test
    void testGetDelegatesReturnsUnmodifiableList() {
        Appender appender = event -> {
        };
        CompositeAppender composite = new CompositeAppender(appender);

        List<Appender> delegates = composite.getDelegates();

        assertThrows(UnsupportedOperationException.class, () -> delegates.add(appender));
    }

    @Test
    void testAddAppender() {
        List<String> results = new ArrayList<>();

        CompositeAppender composite = new CompositeAppender(event -> results.add("first"));

        LogEvent event = new LogEvent(
                Instant.now(),
                Level.INFO,
                "Test",
                Map.of(),
                Map.of(),
                "main",
                "Test"
        );

        composite.append(event);
        assertEquals(1, results.size());

        composite.add(event1 -> results.add("second"));

        composite.append(event);
        assertEquals(3, results.size()); // 1 + 2 = 3
        assertTrue(results.contains("first"));
        assertTrue(results.contains("second"));
    }

    @Test
    void testAddReturnsThisForChaining() {
        CompositeAppender composite = new CompositeAppender(event -> {
        });

        CompositeAppender result = composite.add(event -> {
        });

        assertSame(composite, result);
    }

    @Test
    void testChainsWithAppendersFactory() {
        List<String> results = new ArrayList<>();

        Appender chained = Appenders.chain(
                event -> results.add("console"),
                event -> results.add("file")
        );

        LogEvent event = new LogEvent(
                Instant.now(),
                Level.INFO,
                "Test",
                Map.of(),
                Map.of(),
                "main",
                "Test"
        );

        chained.append(event);

        assertEquals(2, results.size());
        assertTrue(results.contains("console"));
        assertTrue(results.contains("file"));
    }

    @Test
    void testNestedCompositeAppenders() {
        List<String> results = new ArrayList<>();

        Appender inner1 = event -> results.add("inner1");
        Appender inner2 = event -> results.add("inner2");
        Appender outer1 = event -> results.add("outer1");

        CompositeAppender inner = new CompositeAppender(inner1, inner2);
        CompositeAppender outer = new CompositeAppender(inner, outer1);

        LogEvent event = new LogEvent(
                Instant.now(),
                Level.INFO,
                "Test",
                Map.of(),
                Map.of(),
                "main",
                "Test"
        );

        outer.append(event);

        assertEquals(3, results.size());
        assertTrue(results.contains("inner1"));
        assertTrue(results.contains("inner2"));
        assertTrue(results.contains("outer1"));
    }

    @Test
    void testWithAsyncAppenderInChain() throws InterruptedException {
        List<String> results = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(2);

        AsyncAppender async1 = Appenders.async(event -> {
            results.add("async1");
            latch.countDown();
        });

        AsyncAppender async2 = Appenders.async(event -> {
            results.add("async2");
            latch.countDown();
        });

        CompositeAppender composite = new CompositeAppender(async1, async2);

        LogEvent event = new LogEvent(
                Instant.now(),
                Level.INFO,
                "Test",
                Map.of(),
                Map.of(),
                "main",
                "Test"
        );

        composite.append(event);

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Both async appenders should receive event");
        // Give a moment for results list to be updated
        Thread.sleep(50);
        assertTrue(results.contains("async1"), "Results should contain async1");
        assertTrue(results.contains("async2"), "Results should contain async2");

        async1.shutdown();
        async2.shutdown();
    }

    @Test
    void testContinuesWhenMultipleAppendersFail() {
        List<String> results = new ArrayList<>();

        Appender failingAppender1 = event -> {
            throw new RuntimeException("Failure 1");
        };

        Appender failingAppender2 = event -> {
            throw new RuntimeException("Failure 2");
        };

        Appender workingAppender = event -> results.add("working");

        CompositeAppender composite = new CompositeAppender(
                failingAppender1,
                failingAppender2,
                workingAppender
        );

        LogEvent event = new LogEvent(
                Instant.now(),
                Level.INFO,
                "Test",
                Map.of(),
                Map.of(),
                "main",
                "Test"
        );

        // Should not throw
        assertDoesNotThrow(() -> composite.append(event));

        // Working appender should still receive event
        assertEquals(1, results.size());
        assertEquals("working", results.getFirst());
    }

    @Test
    void testAllAppendersFail() {
        Appender failingAppender1 = event -> {
            throw new RuntimeException("Failure 1");
        };

        Appender failingAppender2 = event -> {
            throw new RuntimeException("Failure 2");
        };

        CompositeAppender composite = new CompositeAppender(
                failingAppender1,
                failingAppender2
        );

        LogEvent event = new LogEvent(
                Instant.now(),
                Level.INFO,
                "Test",
                Map.of(),
                Map.of(),
                "main",
                "Test"
        );

        // Should not throw even when all fail
        assertDoesNotThrow(() -> composite.append(event));
    }

    @Test
    void testAddRequiresNonNullAppender() {
        CompositeAppender composite = new CompositeAppender(event -> {
        });

        assertThrows(NullPointerException.class, () -> composite.add(null));
    }

    @Test
    void testAddAppenderThenAppend() {
        List<String> results = new ArrayList<>();

        CompositeAppender composite = new CompositeAppender(evt -> results.add("original"));

        LogEvent event = new LogEvent(
                Instant.now(),
                Level.INFO,
                "Test",
                Map.of(),
                Map.of(),
                "main",
                "Test"
        );

        // First append - only original
        composite.append(event);
        assertEquals(1, results.size());
        assertEquals("original", results.getFirst());

        // Add new appender
        composite.add(evt -> results.add("added"));

        // Clear and append again
        results.clear();
        composite.append(event);

        assertEquals(2, results.size());
        assertTrue(results.contains("original"));
        assertTrue(results.contains("added"));
    }
}

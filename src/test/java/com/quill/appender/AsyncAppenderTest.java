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
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AsyncAppender.
 */
class AsyncAppenderTest {

    @Test
    void testProcessesEventsAsynchronously() throws InterruptedException {
        List<LogEvent> capturedEvents = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        Appender delegate = event -> {
            capturedEvents.add(event);
            latch.countDown();
        };

        AsyncAppender async = new AsyncAppender(delegate);

        LogEvent event = new LogEvent(
                Instant.now(),
                Level.INFO,
                "Test",
                Map.of(),
                Map.of(),
                "main",
                "Test"
        );

        async.append(event);

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Event should be processed");
        assertEquals(1, capturedEvents.size());
        assertEquals("Test", capturedEvents.getFirst().message());

        async.shutdown();
    }

    @Test
    void testProcessesMultipleEvents() throws InterruptedException {
        List<LogEvent> capturedEvents = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(5);

        Appender delegate = event -> {
            capturedEvents.add(event);
            latch.countDown();
        };

        AsyncAppender async = new AsyncAppender(delegate);

        for (int i = 0; i < 5; i++) {
            async.append(new LogEvent(
                    Instant.now(),
                    Level.INFO,
                    STR."Message \{i}",
                    Map.of("index", i),
                    Map.of(),
                    "main",
                    "Test"
            ));
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS), "All events should be processed");
        assertEquals(5, capturedEvents.size());

        async.shutdown();
    }

    @Test
    void testDropsEventsWhenQueueIsFull() throws InterruptedException {
        AtomicInteger processedCount = new AtomicInteger(0);
        CountDownLatch processingLatch = new CountDownLatch(1);

        // Delegate that blocks first event to fill queue
        Appender blockingDelegate = event -> {
            try {
                processingLatch.await(); // Block until we signal
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            processedCount.incrementAndGet();
        };

        // Small queue size
        AsyncAppender async = new AsyncAppender(blockingDelegate, 3, true);

        // Fill the queue - first event blocks, queue takes 2 more (capacity 3, one being processed)
        for (int i = 0; i < 10; i++) {
            async.append(new LogEvent(
                    Instant.now(),
                    Level.INFO,
                    STR."Msg \{i}",
                    Map.of(),
                    Map.of(),
                    "main",
                    "Test"
            ));
        }

        // Some events should be dropped (queue capacity is only 3)
        Thread.sleep(100); // Give time for queue operations
        assertTrue(async.getQueueSize() <= 3, "Queue should not exceed capacity");

        // Release the block
        processingLatch.countDown();

        // Wait for processing to complete
        Thread.sleep(500);

        // Should have processed fewer than 10 events
        assertTrue(processedCount.get() < 10, "Some events should have been dropped");

        async.shutdown();
    }

    @Test
    void testBlocksWhenQueueIsFull() throws InterruptedException {
        AtomicInteger processedCount = new AtomicInteger(0);
        CountDownLatch processingLatch = new CountDownLatch(1);

        Appender blockingDelegate = event -> {
            try {
                processingLatch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            processedCount.incrementAndGet();
        };

        // Blocking mode (dropOnFull = false)
        AsyncAppender async = new AsyncAppender(blockingDelegate, 2, false);

        Thread loggerThread = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                async.append(new LogEvent(
                        Instant.now(),
                        Level.INFO,
                        STR."Msg \{i}",
                        Map.of(),
                        Map.of(),
                        "main",
                        "Test"
                ));
            }
        });

        loggerThread.start();

        // Release the block
        Thread.sleep(100);
        processingLatch.countDown();

        loggerThread.join(5000);

        // In blocking mode, all events should be processed
        assertEquals(5, processedCount.get());

        async.shutdown();
    }

    @Test
    void testGetQueueSize() {
        CountDownLatch latch = new CountDownLatch(1);

        Appender blockingDelegate = event -> {
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        AsyncAppender async = new AsyncAppender(blockingDelegate, 10, true);

        // Add events
        for (int i = 0; i < 5; i++) {
            async.append(new LogEvent(
                    Instant.now(),
                    Level.INFO,
                    STR."Msg \{i}",
                    Map.of(),
                    Map.of(),
                    "main",
                    "Test"
            ));
        }

        // Queue should have some events (one might be processing)
        assertTrue(async.getQueueSize() > 0);

        latch.countDown();
        async.shutdown();
    }

    @Test
    void testGetQueueCapacity() {
        List<LogEvent> capturedEvents = new ArrayList<>();
        AsyncAppender async = new AsyncAppender(capturedEvents::add, 500, true);

        assertEquals(500, async.getQueueCapacity());
        async.shutdown();
    }

    @Test
    void testShutdownWaitsForCompletion() {
        List<LogEvent> capturedEvents = new ArrayList<>();
        AtomicInteger slowCount = new AtomicInteger(0);

        Appender slowDelegate = event -> {
            try {
                Thread.sleep(50);
                slowCount.incrementAndGet();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            capturedEvents.add(event);
        };

        AsyncAppender async = new AsyncAppender(slowDelegate);

        // Add several events
        for (int i = 0; i < 3; i++) {
            async.append(new LogEvent(
                    Instant.now(),
                    Level.INFO,
                    STR."Msg \{i}",
                    Map.of(),
                    Map.of(),
                    "main",
                    "Test"
            ));
        }

        // Shutdown should wait for completion
        async.shutdown(5000);

        assertEquals(3, slowCount.get(), "All events should be processed");
    }

    @Test
    void testGracefulHandlesDelegateException() {
        Appender failingDelegate = event -> {
            throw new RuntimeException("Intentional error");
        };

        AsyncAppender async = new AsyncAppender(failingDelegate);

        // Should not throw even though delegate fails
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 5; i++) {
                async.append(new LogEvent(
                        Instant.now(),
                        Level.INFO,
                        STR."Msg \{i}",
                        Map.of(),
                        Map.of(),
                        "main",
                        "Test"
                ));
            }
        });

        async.shutdown();
    }

    @Test
    void testDaemonThreadDoesNotPreventJvmShutdown() {
        List<LogEvent> capturedEvents = new ArrayList<>();

        AsyncAppender async = new AsyncAppender(capturedEvents::add);

        async.append(new LogEvent(
                Instant.now(),
                Level.INFO,
                "Test",
                Map.of(),
                Map.of(),
                "main",
                "Test"
        ));

        // Thread should be daemon
        Thread[] threads = new Thread[10];
        Thread.enumerate(threads);
        boolean foundDaemon = false;
        for (Thread t : threads) {
            if (t != null && t.getName().contains("quill-async-appender") && t.isDaemon()) {
                foundDaemon = true;
                break;
            }
        }
        assertTrue(foundDaemon, "Async appender thread should be a daemon thread");

        async.shutdown();
    }
}

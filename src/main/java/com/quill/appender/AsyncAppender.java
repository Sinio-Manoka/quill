package com.quill.appender;

import com.quill.model.LogEvent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Asynchronous appender wrapper that processes log events in a background thread.
 * This prevents logging I/O from blocking the application thread.
 * <p>
 * Uses a bounded queue to prevent unbounded memory growth. When the queue is full,
 * log events are dropped (configurable behavior).
 */
public class AsyncAppender implements Appender {

    private final Appender delegate;
    private final BlockingQueue<LogEvent> queue;
    private final ExecutorService executor;
    private final AtomicBoolean running;
    private final int queueSize;
    private final boolean dropOnFull;

    /**
     * Creates an async appender with a default queue size of 1000.
     * When the queue is full, new log events are dropped.
     *
     * @param delegate the underlying appender to delegate to
     */
    public AsyncAppender(Appender delegate) {
        this(delegate, 1000, true);
    }

    /**
     * Creates an async appender with the specified queue size.
     *
     * @param delegate   the underlying appender to delegate to
     * @param queueSize  the maximum number of events to queue
     * @param dropOnFull if true, drop events when queue is full; if false, block
     */
    public AsyncAppender(Appender delegate, int queueSize, boolean dropOnFull) {
        this.delegate = delegate;
        this.queueSize = queueSize;
        this.dropOnFull = dropOnFull;
        this.queue = new ArrayBlockingQueue<>(queueSize);
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "quill-async-appender");
            thread.setDaemon(true); // Don't prevent JVM shutdown
            return thread;
        });
        this.running = new AtomicBoolean(true);

        startProcessor();
    }

    private void startProcessor() {
        executor.submit(() -> {
            while (running.get() || !queue.isEmpty()) {
                try {
                    LogEvent event = queue.take();
                    delegate.append(event);
                } catch (InterruptedException e) {
                    if (running.get()) {
                        System.err.println(STR."Async appender processor interrupted: \{e.getMessage()}");
                    }
                    break;
                } catch (Exception e) {
                    System.err.println(STR."Error in async appender: \{e.getMessage()}");
                }
            }
        });
    }

    @Override
    public void append(LogEvent event) {
        if (!running.get()) {
            // Fallback to synchronous if not running
            delegate.append(event);
            return;
        }

        if (dropOnFull) {
            // Non-blocking: offer returns false if queue is full
            if (!queue.offer(event)) {
                // Event dropped - could add metrics here
                System.err.println(STR."Async appender queue full, dropping log event: \{event.message()}");
            }
        } else {
            // Blocking: put waits for space
            try {
                queue.put(event);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                delegate.append(event); // Fallback to synchronous
            }
        }
    }

    /**
     * Shuts down the async appender gracefully.
     * Processes remaining queued events before stopping.
     * Waits up to the specified timeout for completion.
     *
     * @param timeoutMs maximum time to wait in milliseconds
     */
    public void shutdown(long timeoutMs) {
        running.set(false);
        executor.shutdown();
        try {
            if (!executor.awaitTermination(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS)) {
                System.err.println("Async appender did not terminate gracefully, forcing shutdown");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Shuts down the async appender with a default timeout of 5 seconds.
     */
    public void shutdown() {
        shutdown(5000);
    }

    /**
     * Returns the current queue size (number of pending events).
     *
     * @return the number of events waiting to be processed
     */
    public int getQueueSize() {
        return queue.size();
    }

    /**
     * Returns the maximum queue capacity.
     *
     * @return the maximum number of events that can be queued
     */
    public int getQueueCapacity() {
        return queueSize;
    }
}

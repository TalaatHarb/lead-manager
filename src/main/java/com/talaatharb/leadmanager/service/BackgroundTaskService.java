package com.talaatharb.leadmanager.service;

import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Application-level service that manages a shared background thread pool for
 * executing JavaFX {@link Task} objects off the UI thread.
 * <p>
 * Using a shared executor ensures that background work is bounded, cancellable,
 * and properly tracked rather than being spread across ad-hoc daemon threads.
 */
public final class BackgroundTaskService implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(BackgroundTaskService.class);
    private static final int CORE_POOL_SIZE = 4;

    private final ExecutorService executor;

    public BackgroundTaskService() {
        this.executor = Executors.newFixedThreadPool(CORE_POOL_SIZE, new NamedDaemonThreadFactory());
    }

    /**
     * Submits a JavaFX {@link Task} for execution on the background thread pool.
     *
     * @param task the task to execute
     * @param <T>  the task result type
     */
    public <T> void submit(Task<T> task) {
        executor.submit(task);
    }

    /**
     * Shuts down the underlying executor and cancels any running tasks.
     */
    @Override
    public void close() {
        executor.shutdownNow();
        log.info("BackgroundTaskService shut down");
    }

    // ---- Thread factory ----

    private static final class NamedDaemonThreadFactory implements ThreadFactory {
        private final AtomicInteger counter = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "bg-task-" + counter.getAndIncrement());
            t.setDaemon(true);
            return t;
        }
    }
}

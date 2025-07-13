package org.emjay.task_retry;

import org.emjay.task_retry.domain.Task;

import java.util.Objects;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * This is simulated retry process using java constructs
 * A blocking queue to hold failed task. So that when a task fails, it is added to the Queue
 * A RetryScheduler that uses the ScheduledExecutorService to poll the Failed Task Queue and retry it.
 * Uses a thread pool of 3 threads. Failed tasks are submitted to any idle thread in the thread pool
 */

public class RetryScheduler {
    private static final Logger log = Logger.getLogger(String.valueOf(RetryScheduler.class));
    private final BlockingQueue<Task> failedTaskQueue = new LinkedBlockingQueue<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ExecutorService threadPool = Executors.newFixedThreadPool(2);

    /**
     * Retries failed task every 10 seconds
     */
    public void start() {
        scheduler.scheduleAtFixedRate(submitTaskForRetry(), 5, 10, TimeUnit.SECONDS);
        registerShutDownHook();
    }

    /**
     * simulates task execution that randomly passes or fails
     * logs success or failure
     * @param task task to be executed
     */
    public boolean doTask(Task task) {
        log.info("[TaskRunner] executing task with id " + task.getTaskId());
        boolean result = Math.random() > 0.5;
        if (!result) enqueueTask(task);

        return result;
    }

    /**
     * @return a runnable that polls the failed task retry queue and submits a retry task to the thread pool if any exists
     */
    private Runnable submitTaskForRetry() {
        return () -> {
            Task task = failedTaskQueue.poll();
            if (Objects.nonNull(task)) {
                log.info(String.format(
                        "[TaskRunner] [Thread: %s] submitting task [%s] for retrying",
                        Thread.currentThread().getName(),
                        task.getTaskId()));
                try {
                    threadPool.submit(retryTask(task));
                } catch (RejectedExecutionException e) {
                    log.warning("Retry task submission rejected for task: " + task.getTaskId());
                }
            } else {
                log.info("There are no failed tasks");
            }
        };
    }

    /**
     * Performs task retry
     * @param task task to be completed
     * @return runnable
     */
    private Runnable retryTask(Task task) {
        return () -> {
            if (task.getRetryCount() >= 3) {
                // ideally, a task that has exhausted the designated retry count should be added to a DLQ but that is not implemented yet.
                log.info("Giving up on task " + task.getTaskId() + " after " + task.getRetryCount() + " attempts.");
            }

            log.info(String.format("::::[TaskRunner] [Thread: %s] about to retry task [%s]::::",
                    Thread.currentThread().getName(), task.getTaskId()));
            task.incrementRetryCount();
            // task enqueues itself if it fails
            boolean taskCompleted = doTask(task);

            if (taskCompleted) {
                log.info(String.format("::::[ Thread: %s ] Task with id [%s] is completed!::::", Thread.currentThread().getName(), task.getTaskId()));
            }
        };
    }

    /**
     * adding a task to the retry queue
     * logs success or failure
     * @param failedTask task to be queued
     */
    private void enqueueTask(Task failedTask) {
        boolean submitted = failedTaskQueue.offer(failedTask);

        if (!submitted) {
            log.info(String.format("Queuing failed task [%s] was unsuccessful: %s", failedTask, false));
            return;
        }

        log.info(String.format("Queued failed task [%s] successfully: %s", failedTask, true));
    }

    private void registerShutDownHook() {
        log.info("[Task Shutdown] Shutting down scheduler and thread pool");
        Thread shutDownHook = new Thread(() -> {
            threadPool.shutdown();
            scheduler.shutdown();

            try {
                if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                }

                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Runtime.getRuntime().addShutdownHook(shutDownHook);
    }
}


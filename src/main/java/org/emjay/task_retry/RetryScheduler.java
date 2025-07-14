package org.emjay.task_retry;

import org.emjay.task_retry.queue.FailedTaskQueue;
import org.emjay.task_retry.queue.TaskDeadLetterQueue;
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
    private static final TaskExecutor taskExecutor = new TaskExecutor();
    private final FailedTaskQueue taskQueue = new FailedTaskQueue();
    private final TaskDeadLetterQueue deadLetterQueue = new TaskDeadLetterQueue();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ExecutorService threadPool = Executors.newFixedThreadPool(2);

    /**
     * Retries failed task every 10 seconds
     */
    public void start() {
        scheduler.scheduleAtFixedRate(submitTaskForRetry(), 5, 10, TimeUnit.SECONDS);
        registerShutDownHook();
    }

    public static String getDelayString(Task task) {
        long delayInMillis = task.getNextRetryAt() - System.currentTimeMillis();
        return delayInMillis < 0
                ? String.format("%sms", Math.abs(delayInMillis))
                : String.format("%ss", TimeUnit.MILLISECONDS.toSeconds(delayInMillis));
    }


    /**
     * @return a runnable that polls the failed task retry queue and submits a retry task to the thread pool if any exists
     */
    private Runnable submitTaskForRetry() {
        return () -> {
            Task task = taskQueue.dequeue();

            if (Objects.nonNull(task)) {
                long now = System.currentTimeMillis();

                if (now >= task.getNextRetryAt()) {
                    log.info(String.format("[TaskRunner] [Thread: %s] submitting task [%s] for retrying",
                        Thread.currentThread().getName(), task.getTaskId()));
                    try {
                        threadPool.submit(retryTask(task));
                    } catch (RejectedExecutionException e) {
                        log.warning("Retry task submission rejected for task: " + task.getTaskId());
                    }
                } else {
                    taskQueue.enqueue(task);
                    log.info("Too early. Will retry in " + getDelayString(task));
                }
            } else {
                log.info("Task Queue is empty");
            }
        };
    }

    /**
     * Performs task retry. Moves persistently failing tasks to a Dead letter Queue
     * @param task task to be completed
     * @return runnable
     */
    private Runnable retryTask(Task task) {
        return () -> {
            if (task.getRetryCount() >= 3) {
                moveToDLQ(task);
                return;
            }

            log.info(String.format("::::[TaskRunner] [Thread: %s] Retrying task [%s].......::::",
                    Thread.currentThread().getName(), task.getTaskId()));

            task.incrementRetryCount();
            taskExecutor.doTask(task);
        };
    }

    /**
     * moves persistently failing task to a dead letter queue
     * @param task task to be completed
     */
    private void moveToDLQ(Task task) {
        log.info(":::::Moving task to DLQ " + task.getTaskId() + " after " + task.getRetryCount() + " attempts.:::::");
        boolean isSubmitted = deadLetterQueue.enqueue(task);

        if (!isSubmitted) {
            log.warning("Dead Letter Queue is full!");
        }

        log.info(String.format(":::Task [%s] has been placed in DLQ successfully", task.getTaskId()));
    }

    /**
     * Cleans up the threads after JVM is shut down
     */
    private void registerShutDownHook() {
        log.info("[Task Shutdown] registering shutdown hook");
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


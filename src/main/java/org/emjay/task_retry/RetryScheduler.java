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
     * simulates failure by simply adding a task to the retry queue
     * logs success or failure
     * @param taskId unique identifier for the task
     */
    public void simulateFailure(String taskId) {
       boolean submitted = failedTaskQueue.offer(new Task(taskId));
       log.info(() -> String.format("Queued failed task [%s] successfully: %s", taskId, submitted));
    }

    /**
     * @return a runnable that polls the failed task retry queue and submits a retry task to the thread pool if any exists
     */
    private Runnable submitTaskForRetry() {
        return () -> {
            Task task = failedTaskQueue.poll();
            if (Objects.nonNull(task)) {
                try {
                    threadPool.submit(doTask(task));
                } catch (RejectedExecutionException e) {
                    log.warning("Retry task submission rejected for task: " + task.getTaskId());
                }
            }
        };
    }

    /**
     * Does a task and queues it for retry if it fails.
     * @param task task to be completed
     * @return runnable
     */
    private Runnable doTask(Task task) {
        return () -> {
            log.info(() -> String.format("[TaskRunner] [Thread: %s] Retrying task [%s]", Thread.currentThread().getName(), task.getTaskId()));
            boolean success = executeTask(task.getTaskId());
            int taskRetryCount = task.incrementRetryCount();

            if (!success) {
                if (taskRetryCount < 3) {
                    log.info(() -> String.format("[TaskQueue] Retry failed. Re-queuing task with id [%s], retry count: [%s]", task.getTaskId(), taskRetryCount));
                    boolean submitted = failedTaskQueue.offer(task);
                    if (!submitted) {
                        log.warning(String.format("[TaskQueue] Re-queuing task with id [%s] failed: [%s]", task.getTaskId(), false));
                        return;
                    }
                    log.info(() -> String.format("[TaskQueue] Task [%s] Re-queue status: [%s]", task.getTaskId(), true));
                } else {
                    // ideally, a task that has exhausted the designated retry count should be added to a DLQ but that is not implemented yet.
                    log.info(() -> "Giving up on task " + task.getTaskId() + " after " + taskRetryCount + " attempts.");
                }
            } else {
                log.info(String.format("::::[ Thread: %s ] Task with id [%s] is completed!::::",
                        Thread.currentThread().getName(),
                        task.getTaskId()));
            }
        };
    }

    private boolean executeTask(String taskId) {
        // Simulate random success or failure
        log.info("[TaskRunner] executing task with id " + taskId);
        return Math.random() > 0.5;
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


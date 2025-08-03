package org.emjay.task_retry;

import org.emjay.task_retry.queue.FailedTaskQueue;
import org.emjay.task_retry.domain.Task;

import java.util.logging.Logger;

public class TaskExecutor {
    private static final Logger log = Logger.getLogger(String.valueOf(TaskExecutor.class));

    /**
     * simulates task execution that randomly passes or fails
     * logs success or failure
     * @param task task to be executed
     */
    public void doTask(Task task) {
        log.info("[TaskRunner] Executing task with id " + task.getTaskId());
        boolean result = Math.random() > 0.5;
        if (!result) {
            enqueueTask(task);
        }
        else {
            log.info(String.format("::::[ Thread: %s ] Task with id [%s] is completed after [#%s] retries!::::",
                Thread.currentThread().getName(), task.getTaskId(), task.getRetryCount()));
        }
    }

    /**
     * adding a task to the retry queue
     * logs success or failure
     * @param failedTask task to be queued
     */
    private void enqueueTask(Task failedTask) {
        boolean submitted = new FailedTaskQueue().enqueue(failedTask);

        if (!submitted) {
            log.warning("Retry queue is full! Dropping task: " + failedTask.getTaskId());
            return;
        }

        log.info(String.format("Queued failed task [%s] successfully. Will retry in [%s]",
                failedTask.getTaskId(), RetryScheduler.getDelayString(failedTask)));
    }
}

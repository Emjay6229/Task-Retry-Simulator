package org.emjay.task_retry;

import org.emjay.task_retry.domain.Task;

public final class TaskRetrySchedulerApplication {
    private TaskRetrySchedulerApplication() {
        // No args constructor
    }

    public static void main(String[] args) {
        RetryScheduler retryScheduler = new RetryScheduler();
        retryScheduler.start();

        retryScheduler.doTask(new Task("task-101"));
        retryScheduler.doTask(new Task("task-102"));
        retryScheduler.doTask(new Task("task-103"));
    }
}
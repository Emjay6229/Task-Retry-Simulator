package org.emjay.task_retry;

public final class TaskRetrySchedulerApplication {
    private TaskRetrySchedulerApplication() {
        // No args constructor
    }

    public static void main(String[] args) {
        RetryScheduler retryScheduler = new RetryScheduler();
        retryScheduler.start();

        // Simulate initial failed tasks
        retryScheduler.simulateFailure("task-101");
        retryScheduler.simulateFailure("task-102");
        retryScheduler.simulateFailure("task-103");
    }
}
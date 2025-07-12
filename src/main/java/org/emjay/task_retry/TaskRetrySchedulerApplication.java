package org.emjay.payment_retry;

public final class TaskRetrySchedulerApplication {

    private TaskRetrySchedulerApplication() {}

    public static void main(String[] args) {
        RetryScheduler retryScheduler = new RetryScheduler();
        retryScheduler.start();

        // Simulate initial failed payments
        retryScheduler.simulateFailure("task-101");
        retryScheduler.simulateFailure("task-102");
        retryScheduler.simulateFailure("task-103");
    }
}
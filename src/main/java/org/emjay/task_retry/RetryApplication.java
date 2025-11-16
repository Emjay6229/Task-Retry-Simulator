package org.emjay.task_retry;

import org.emjay.task_retry.domain.Task;

public final class RetryApplication {
    private RetryApplication() {
        // No args constructor
    }

    public static void main(String[] args) {
        RetryWorker worker = new RetryWorker();
        TaskRunner taskRunner = new TaskRunner();

        taskRunner.doTask(new Task("task-101"));
        taskRunner.doTask(new Task("task-102"));
        taskRunner.doTask(new Task("task-103"));

        worker.start();
    }
}

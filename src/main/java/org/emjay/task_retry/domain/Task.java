package org.emjay.task_retry.domain;

import java.util.concurrent.atomic.AtomicInteger;

public class Task {
    private final String taskId;
    private final AtomicInteger retryCount;

    public Task(String paymentId) {
        this.taskId = paymentId;
        this.retryCount = new AtomicInteger(0);
    }

    public String getTaskId() {
        return taskId;
    }

    public int incrementRetryCount() {
        return retryCount.incrementAndGet();
    }

    public int getRetryCount() {
        return retryCount.get();
    }

    @Override
    public String toString() {
        return "Task{" +
                "taskId='" + taskId + '\'' +
                ", retryCount=" + retryCount +
                '}';
    }
}


package org.emjay.task_retry.domain;

import java.util.concurrent.atomic.AtomicInteger;

public class Task {
    private final String taskId;
    private final AtomicInteger retryCount;
    private long nextRetryAt;

    public Task(String taskId) {
        this.taskId = taskId;
        this.retryCount = new AtomicInteger(0);
        this.nextRetryAt = System.currentTimeMillis();
    }

    public String getTaskId() {
        return taskId;
    }

    public void incrementRetryCount() {
        retryCount.incrementAndGet();
        updateNextRetryAt();
    }

    public int getRetryCount() {
        return retryCount.get();
    }

    public long getNextRetryAt() { return nextRetryAt; }

    private void updateNextRetryAt() {
        long delay = (long) Math.pow(2, retryCount.doubleValue()) * 1000; // 2^n seconds in millis
        this.nextRetryAt = System.currentTimeMillis() + delay;
    }

    @Override
    public String toString() {
        return "Task{" +
                "taskId='" + taskId + '\'' +
                ", retryCount=" + retryCount +
                ", nextRetryAt=" + nextRetryAt +
                '}';
    }
}


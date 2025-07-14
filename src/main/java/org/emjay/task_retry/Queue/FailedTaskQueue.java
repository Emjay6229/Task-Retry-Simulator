package org.emjay.task_retry.Queue;

import org.emjay.task_retry.domain.Task;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public final class FailedTaskQueue implements TaskQueue<Task> {
    private static final BlockingQueue<Task> queue = new LinkedBlockingQueue<>();

    public BlockingQueue<Task> getQueue() {
        return queue;
    }

    public boolean enqueue(Task task) {
        return queue.offer(task);
    }

    public Task dequeue() {
        return queue.poll();
    }
}

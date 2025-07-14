package org.emjay.task_retry.queue;

import org.emjay.task_retry.domain.Task;
import org.emjay.task_retry.queue.interfaces.ITaskQueue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public final class FailedTaskQueue implements ITaskQueue<Task> {
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

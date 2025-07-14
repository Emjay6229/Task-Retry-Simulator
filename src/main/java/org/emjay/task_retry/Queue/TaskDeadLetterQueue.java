package org.emjay.task_retry.Queue;

import org.emjay.task_retry.domain.Task;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class TaskDeadLetterQueue implements TaskQueue<Task> {
   private static final BlockingQueue<Task> queue = new ArrayBlockingQueue<>(100);

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

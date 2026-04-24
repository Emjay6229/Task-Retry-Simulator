package org.emjay.task_retry.queue;

import org.emjay.task_retry.domain.Task;
import org.emjay.task_retry.queue.interfaces.ITaskQueue;

import java.util.concurrent.BlockingQueue;

public class BaseTaskQueue implements ITaskQueue<Task> {
    protected static BaseTaskQueue instance;
    protected static BlockingQueue<Task> queue;

    @Override
    public BlockingQueue<Task> getQueue() {
        return queue;
    }

    @Override
    public boolean enqueue(Task task) {
        return queue.offer(task);
    }

    @Override
    public Task dequeue() {
        return queue.poll();
    }

    public static synchronized BaseTaskQueue getInstance() {
        if (instance == null) {
            instance = new BaseTaskQueue();
        }
        return instance;
    }
}

package org.emjay.task_retry.queue.interfaces;

import org.emjay.task_retry.domain.Task;

import java.util.concurrent.BlockingQueue;

public interface ITaskQueue<E> {
    BlockingQueue<E> getQueue();

    boolean enqueue(Task task);

    Task dequeue();
}

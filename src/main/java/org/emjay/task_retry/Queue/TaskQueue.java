package org.emjay.task_retry.Queue;

import org.emjay.task_retry.domain.Task;

import java.util.concurrent.BlockingQueue;

public interface TaskQueue<E> {
    BlockingQueue<E> getQueue();

    boolean enqueue(Task task);

    Task dequeue();
}

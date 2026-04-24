package org.emjay.task_retry.queue;

import org.emjay.task_retry.domain.Task;
import org.emjay.task_retry.queue.interfaces.ITaskQueue;

public final class RetryQueue extends BaseTaskQueue implements ITaskQueue<Task> { }

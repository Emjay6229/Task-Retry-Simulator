package org.emjay.task_retry.queue;

import org.emjay.task_retry.domain.Task;
import org.emjay.task_retry.queue.interfaces.ITaskQueue;

public class DeadLetterQueue extends BaseTaskQueue implements ITaskQueue<Task> { }

# RetryScheduler (Java)

A simple task retry scheduler built using **Java concurrency primitives**.  
This project simulates a retry mechanism that retries failed tasks using a thread-safe queue and a scheduled retry loop.

## 🚀 Project Overview

This project demonstrates how to implement a **retry system** in core Java using:

- `BlockingQueue` to buffer failed tasks
- `ScheduledExecutorService` to periodically poll the queue
- `ExecutorService` to retry tasks using worker threads
- A **shutdown hook** to gracefully terminate the system
- **Retry logic** that gives up after 3 failed attempts

Each task has a retry count and is either:
- Re-queued on failure (up to 3 times)
- Dropped or redirected to a Dead Letter Queue (DLQ) after max retries (currently just logged)

---

## 🛠 Technologies Used

| Tool                       | Description |
|----------------------------|-------------|
| Java 21+                   | Language for core logic |
| Maven                      | Build automation and dependency management |
| `java.util.logging` | Logging (default is `java.util.logging`) |

> ✅ **No third-party libraries required** — pure Java!

---


---

## ⚙️ How It Works

1. Tasks are simulated via `simulateFailure("taskId")`.
2. These are placed into a `LinkedBlockingQueue`.
3. A scheduled task polls this queue every 10 seconds.
4. If a task is present, it's submitted to a thread pool for execution.
5. If the task fails, it's retried up to 3 times.
6. After 3 failed retries, the task is logged as permanently failed (can be extended to support DLQ).

---

## ▶️ How to Run the Project

### ✅ Prerequisites

- Java 21 or higher
- Maven 3.x

### 📦 Build with Maven

```bash
mvn clean install
```

### 🚀 Run the Application
You can run the main class using maven (if you have maven installed):

```bash
mvn exec:java -Dexec.mainClass="org.emjay.task_retry.TaskRetrySchedulerApplication"
```

Or run using your IDE run configuration

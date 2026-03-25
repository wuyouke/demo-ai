package com.example.demo_ai.service;

import com.example.demo_ai.model.Task;
import com.example.demo_ai.model.Task.TaskStatus;
import com.example.demo_ai.tools.ToolExecutor;
import com.example.demo_ai.tools.ToolRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * 任务执行器 - 按照依赖关系执行任务
 */
public class TaskExecutor {

    private final ToolRegistry toolRegistry;
    private final int maxConcurrentTasks;
    private final ExecutorService executorService;
    private final Map<String, Task> taskMap;
    private final List<TaskExecutionListener> listeners;

    /**
     * 任务执行监听器接口
     */
    public interface TaskExecutionListener {
        void onTaskStarted(Task task);
        void onTaskCompleted(Task task);
        void onTaskFailed(Task task, Exception e);
    }

    public TaskExecutor(ToolRegistry toolRegistry) {
        this(toolRegistry, 3);
    }

    public TaskExecutor(ToolRegistry toolRegistry, int maxConcurrentTasks) {
        this.toolRegistry = toolRegistry;
        this.maxConcurrentTasks = maxConcurrentTasks;
        this.executorService = Executors.newFixedThreadPool(maxConcurrentTasks);
        this.taskMap = new ConcurrentHashMap<>();
        this.listeners = Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * 添加执行监听器
     */
    public void addListener(TaskExecutionListener listener) {
        listeners.add(listener);
    }

    /**
     * 执行任务列表（异步，后台执行）
     */
    public List<Task> executeTasks(List<Task> tasks) {
        // 构建任务映射
        for (Task task : tasks) {
            taskMap.put(task.getTaskId(), task);
        }

        // 在后台线程中异步执行所有任务
        executorService.submit(() -> {
            try {
                executeTasksInBackground(tasks);
            } catch (Exception e) {
                System.err.println("后台任务执行异常: " + e.getMessage());
                e.printStackTrace();
            }
        });

        // 立即返回任务列表（状态为 PENDING 或 EXECUTING）
        return tasks;
    }

    /**
     * 后台异步执行任务
     */
    private void executeTasksInBackground(List<Task> tasks) {
        List<Future<Task>> futures = new ArrayList<>();
        Set<String> completedTasks = new HashSet<>();

        // 持续执行，直到所有任务完成
        while (completedTasks.size() < tasks.size()) {
            // 获取所有可执行的任务（依赖已完成）
            List<Task> readyTasks = getReadyTasks(tasks, completedTasks);

            if (readyTasks.isEmpty() && completedTasks.size() < tasks.size()) {
                // 检查是否存在循环依赖或错误
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("后台任务被中断");
                    break;
                }
                continue;
            }

            // 提交任务执行
            for (Task task : readyTasks) {
                if (task.getStatus() == TaskStatus.PENDING) {
                    Future<Task> future = executorService.submit(() -> executeTask(task));
                    futures.add(future);
                    task.setStatus(TaskStatus.EXECUTING);
                }
            }

            // 等待至少一个任务完成
            if (!futures.isEmpty()) {
                try {
                    Future<Task> completedFuture = FutureUtils.waitForAny(futures, 30, TimeUnit.SECONDS);
                    if (completedFuture != null) {
                        Task completedTask = completedFuture.get();
                        completedTasks.add(completedTask.getTaskId());
                        futures.remove(completedFuture);
                    }
                } catch (TimeoutException e) {
                    System.err.println("任务执行超时");
                    // 移除超时的任务，继续执行其他任务
                    for (Future<Task> f : new ArrayList<>(futures)) {
                        if (f.isDone()) {
                            futures.remove(f);
                        }
                    }
                } catch (ExecutionException e) {
                    System.err.println("任务执行异常: " + e.getMessage());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("后台任务被中断");
                    break;
                }
            }
        }
    }

    /**
     * 获取所有可执行的任务（依赖已完成）
     */
    private List<Task> getReadyTasks(List<Task> tasks, Set<String> completedTasks) {
        return tasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.PENDING)
                .filter(task -> allDependenciesCompleted(task, completedTasks))
                .collect(Collectors.toList());
    }

    /**
     * 检查所有依赖是否已完成
     */
    private boolean allDependenciesCompleted(Task task, Set<String> completedTasks) {
        if (task.getDependencies().isEmpty()) {
            return true;
        }
        return completedTasks.containsAll(task.getDependencies());
    }

    /**
     * 执行单个任务
     */
    private Task executeTask(Task task) {
        task.setStartedAt(System.currentTimeMillis());

        try {
            // 通知监听器任务开始
            for (TaskExecutionListener listener : listeners) {
                listener.onTaskStarted(task);
            }

            // 获取工具执行器
            ToolExecutor executor = toolRegistry.getExecutor(task.getToolName());
            if (executor == null) {
                throw new IllegalArgumentException("找不到工具: " + task.getToolName());
            }

            // 修复工具参数（参数映射适配）
            Map<String, Object> adjustedParams = adjustToolParams(task.getToolName(), task.getToolParams());

            // 执行工具
            com.example.demo_ai.model.ToolResult toolResult = executor.execute(adjustedParams);
            Object result = toolResult != null ? toolResult.getData() : null;

            // 设置任务结果
            task.setResult(result);
            task.setStatus(TaskStatus.COMPLETED);

            // 通知监听器任务完成
            for (TaskExecutionListener listener : listeners) {
                listener.onTaskCompleted(task);
            }

        } catch (Exception e) {
            task.setStatus(TaskStatus.FAILED);
            task.setErrorMessage(e.getMessage());

            // 通知监听器任务失败
            for (TaskExecutionListener listener : listeners) {
                listener.onTaskFailed(task, e);
            }

            System.err.println("任务执行失败 [" + task.getName() + "]: " + e.getMessage());
        } finally {
            task.setCompletedAt(System.currentTimeMillis());
            task.setExecutionTime(task.getCompletedAt() - task.getStartedAt());
        }

        return task;
    }

    /**
     * 顺序执行任务（按依赖关系）
     */
    public List<Task> executeTasksSequentially(List<Task> tasks) {
        for (Task task : tasks) {
            taskMap.put(task.getTaskId(), task);
        }

        Set<String> completedTasks = new HashSet<>();

        for (Task task : tasks) {
            // 等待所有依赖完成
            while (!allDependenciesCompleted(task, completedTasks)) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            // 执行任务
            executeTask(task);
            if (task.getStatus() == TaskStatus.COMPLETED) {
                completedTasks.add(task.getTaskId());
            }
        }

        return tasks;
    }

    /**
     * 获取任务执行统计
     */
    public ExecutionStats getExecutionStats(List<Task> tasks) {
        ExecutionStats stats = new ExecutionStats();

        for (Task task : tasks) {
            stats.totalTasks++;

            switch (task.getStatus()) {
                case COMPLETED:
                    stats.completedTasks++;
                    stats.totalExecutionTime += task.getExecutionTime();
                    break;
                case FAILED:
                    stats.failedTasks++;
                    break;
                case EXECUTING:
                    stats.executingTasks++;
                    break;
                case PENDING:
                    stats.pendingTasks++;
                    break;
                case SKIPPED:
                    stats.skippedTasks++;
                    break;
            }
        }

        return stats;
    }

    /**
     * 关闭执行器
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 调整工具参数（参数映射适配）
     * 处理不同工具对参数名的不同期望
     */
    private Map<String, Object> adjustToolParams(String toolName, Map<String, Object> originalParams) {
        if (originalParams == null) {
            return new HashMap<>();
        }

        // 创建参数副本
        Map<String, Object> params = new HashMap<>(originalParams);

        // 景点工具参数映射：location -> city
        if ("tourist_attraction".equals(toolName)) {
            if (params.containsKey("location") && !params.containsKey("city")) {
                params.put("city", params.get("location"));
                System.out.println("参数映射：tourist_attraction - location -> city");
            }
        }

        return params;
    }

    /**
     * 执行统计信息
     */
    public static class ExecutionStats {
        public int totalTasks = 0;
        public int completedTasks = 0;
        public int failedTasks = 0;
        public int executingTasks = 0;
        public int pendingTasks = 0;
        public int skippedTasks = 0;
        public long totalExecutionTime = 0;

        public double getSuccessRate() {
            if (totalTasks == 0) return 0;
            return (double) completedTasks / totalTasks * 100;
        }

        @Override
        public String toString() {
            return String.format(
                    "总任务数: %d, 已完成: %d, 失败: %d, 成功率: %.1f%%, 总耗时: %dms",
                    totalTasks, completedTasks, failedTasks, getSuccessRate(), totalExecutionTime
            );
        }
    }

    /**
     * Future 工具类
     */
    private static class FutureUtils {
        public static <T> Future<T> waitForAny(List<Future<T>> futures, long timeout, TimeUnit unit)
                throws InterruptedException, TimeoutException {

            long deadline = System.nanoTime() + unit.toNanos(timeout);

            while (System.nanoTime() < deadline) {
                for (Future<T> future : futures) {
                    if (future.isDone()) {
                        return future;
                    }
                }
                Thread.sleep(50);
            }

            throw new TimeoutException("等待任务完成超时");
        }
    }
}


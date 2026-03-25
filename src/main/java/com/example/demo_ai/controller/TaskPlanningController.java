package com.example.demo_ai.controller;

import com.example.demo_ai.model.Task;
import com.example.demo_ai.service.ConversationService;
import com.example.demo_ai.service.TaskExecutor;
import com.example.demo_ai.service.TaskExecutor.ExecutionStats;
import com.example.demo_ai.service.TaskPlanner;
import com.example.demo_ai.tools.ToolRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 任务规划 REST API
 * 提供任务分解、执行和管理功能
 */
@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TaskPlanningController {

    private static final Logger logger = LoggerFactory.getLogger(TaskPlanningController.class);

    /**
     * 全局的任务执行状态缓存（用于前端轮询查询）
     * Key: 任务ID，Value: 任务对象
     */
    private static final Map<String, Task> GLOBAL_TASK_STATE = new ConcurrentHashMap<>();

    @Autowired
    private ToolRegistry toolRegistry;

    @Autowired
    private ConversationService conversationService;

    /**
     * 规划任务 - 将用户目标分解为子任务
     *
     * @param request 请求体，包含 goal 和可选的 context
     * @return 任务列表
     */
    @PostMapping("/plan")
    public ResponseEntity<Map<String, Object>> planTasks(@RequestBody Map<String, Object> request) {
        try {
            String goal = (String) request.get("goal");
            if (goal == null || goal.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        createErrorResponse("目标不能为空"));
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> context = (Map<String, Object>) request.get("context");
            if (context == null) {
                context = new HashMap<>();
            }

            logger.info("规划任务：{}", goal);

            // 创建任务规划器
            TaskPlanner planner = new TaskPlanner(conversationService);

            // 执行任务规划
            List<Task> tasks = planner.planTasks(goal, context);

            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("goal", goal);
            response.put("tasks", tasks);
            response.put("taskCount", tasks.size());
            response.put("plan", planner.formatTaskPlan(tasks));

            logger.info("任务规划完成：共 {} 个子任务", tasks.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("任务规划失败", e);
            return ResponseEntity.badRequest().body(
                    createErrorResponse("任务规划失败：" + e.getMessage()));
        }
    }

    /**
     * 执行任务列表
     *
     * @param request 请求体，包含 tasks 和可选的 sequential（是否顺序执行）
     * @return 执行结果
     */
    @PostMapping("/execute")
    public ResponseEntity<Map<String, Object>> executeTasks(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> taskMaps = (List<Map<String, Object>>) request.get("tasks");
            if (taskMaps == null || taskMaps.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        createErrorResponse("任务列表不能为空"));
            }

            // 转换为 Task 对象
            List<Task> tasks = convertToTasks(taskMaps);

            // 检查是否顺序执行
            boolean sequential = request.containsKey("sequential") &&
                               (boolean) request.get("sequential");

            logger.info("开始执行 {} 个任务，顺序执行：{}", tasks.size(), sequential);

            // 创建任务执行器
            TaskExecutor executor = new TaskExecutor(toolRegistry, 3);

            // 添加监听器（用于日志 + 全局状态更新）
            executor.addListener(new TaskExecutor.TaskExecutionListener() {
                @Override
                public void onTaskStarted(Task task) {
                    logger.info("任务开始执行：{}", task.getName());
                    // 更新全局状态缓存
                    GLOBAL_TASK_STATE.put(task.getTaskId(), task);
                }

                @Override
                public void onTaskCompleted(Task task) {
                    logger.info("任务完成：{} (耗时: {}ms)", task.getName(), task.getExecutionTime());
                    // 更新全局状态缓存
                    GLOBAL_TASK_STATE.put(task.getTaskId(), task);
                }

                @Override
                public void onTaskFailed(Task task, Exception e) {
                    logger.error("任务执行失败：{}", task.getName(), e);
                    // 更新全局状态缓存
                    GLOBAL_TASK_STATE.put(task.getTaskId(), task);
                }
            });

            // 执行任务（异步执行模式下会立即返回）
            List<Task> executedTasks;
            if (sequential) {
                // 顺序执行：同步模式，等待完成
                executedTasks = executor.executeTasksSequentially(tasks);
                // 顺序执行完毕后关闭
                executor.shutdown();
            } else {
                // 并行执行：异步模式，立即返回，后台执行
                executedTasks = executor.executeTasks(tasks);

                // 异步关闭 executor（不阻塞）
                // 延迟关闭，让后台任务有足够时间执行
                new Thread(() -> {
                    try {
                        // 等待一段时间让后台任务执行
                        Thread.sleep(15000);
                        executor.shutdown();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            }

            // 获取统计信息
            ExecutionStats stats = executor.getExecutionStats(executedTasks);

            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("tasks", executedTasks);

            Map<String, Object> statsMap = new HashMap<>();
            statsMap.put("totalTasks", stats.totalTasks);
            statsMap.put("completedTasks", stats.completedTasks);
            statsMap.put("failedTasks", stats.failedTasks);
            statsMap.put("executingTasks", stats.executingTasks);
            statsMap.put("pendingTasks", stats.pendingTasks);
            statsMap.put("successRate", String.format("%.1f%%", stats.getSuccessRate()));
            statsMap.put("totalExecutionTime", stats.totalExecutionTime);
            response.put("stats", statsMap);

            logger.info("任务已提交到后台执行：{}", stats);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("任务执行失败", e);
            return ResponseEntity.badRequest().body(
                    createErrorResponse("任务执行失败：" + e.getMessage()));
        }
    }

    /**
     * 获取最新的任务执行状态
     * 前端可以轮询此接口获取实时的任务执行状态
     * 从全局缓存中读取最新的任务状态
     */
    @PostMapping("/status")
    public ResponseEntity<Map<String, Object>> getTaskStatus(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> taskMaps = (List<Map<String, Object>>) request.get("tasks");
            if (taskMaps == null || taskMaps.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        createErrorResponse("任务列表不能为空"));
            }

            // 转换任务
            List<Task> tasks = convertToTasks(taskMaps);

            // 从全局缓存中更新任务状态
            for (Task task : tasks) {
                Task cachedTask = GLOBAL_TASK_STATE.get(task.getTaskId());
                if (cachedTask != null) {
                    // 使用全局缓存中的最新状态
                    task.setStatus(cachedTask.getStatus());
                    task.setResult(cachedTask.getResult());
                    task.setErrorMessage(cachedTask.getErrorMessage());
                    task.setExecutionTime(cachedTask.getExecutionTime());
                    task.setStartedAt(cachedTask.getStartedAt());
                    task.setCompletedAt(cachedTask.getCompletedAt());
                }
            }

            // 获取执行统计
            TaskExecutor tempExecutor = new TaskExecutor(toolRegistry);
            ExecutionStats stats = tempExecutor.getExecutionStats(tasks);

            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("tasks", tasks);

            Map<String, Object> statsMap = new HashMap<>();
            statsMap.put("totalTasks", stats.totalTasks);
            statsMap.put("completedTasks", stats.completedTasks);
            statsMap.put("failedTasks", stats.failedTasks);
            statsMap.put("executingTasks", stats.executingTasks);
            statsMap.put("pendingTasks", stats.pendingTasks);
            statsMap.put("successRate", String.format("%.1f%%", stats.getSuccessRate()));
            statsMap.put("totalExecutionTime", stats.totalExecutionTime);
            response.put("stats", statsMap);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("获取任务状态失败", e);
            return ResponseEntity.badRequest().body(
                    createErrorResponse("获取状态失败：" + e.getMessage()));
        }
    }

    /**
     * 获取任务执行顺序
     * 基于优先级和依赖关系排序
     *
     * @param request 请求体，包含 tasks
     * @return 排序后的任务列表
     */
    @PostMapping("/order")
    public ResponseEntity<Map<String, Object>> getTaskExecutionOrder(
            @RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> taskMaps = (List<Map<String, Object>>) request.get("tasks");
            if (taskMaps == null || taskMaps.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        createErrorResponse("任务列表不能为空"));
            }

            // 转换为 Task 对象
            List<Task> tasks = convertToTasks(taskMaps);

            // 创建任务规划器获取执行顺序
            TaskPlanner planner = new TaskPlanner(conversationService);
            List<Task> orderedTasks = planner.getExecutionOrder(tasks);

            List<String> orderList = new ArrayList<>();
            for (Task task : orderedTasks) {
                orderList.add(task.getName());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("tasks", orderedTasks);
            response.put("order", orderList);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("获取任务执行顺序失败", e);
            return ResponseEntity.badRequest().body(
                    createErrorResponse("获取执行顺序失败：" + e.getMessage()));
        }
    }

    /**
     * 验证任务依赖关系
     *
     * @param request 请求体，包含 tasks
     * @return 验证结果
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateTaskDependencies(
            @RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> taskMaps = (List<Map<String, Object>>) request.get("tasks");
            if (taskMaps == null || taskMaps.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        createErrorResponse("任务列表不能为空"));
            }

            List<Task> tasks = convertToTasks(taskMaps);

            // 检测循环依赖
            Map<String, Boolean> visited = new HashMap<>();
            Map<String, Boolean> recursionStack = new HashMap<>();
            boolean hasCycle = false;

            for (Task task : tasks) {
                if (hasCyclicDependency(task, tasks, visited, recursionStack)) {
                    hasCycle = true;
                    break;
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("valid", !hasCycle);
            response.put("hasCycle", hasCycle);

            if (hasCycle) {
                response.put("message", "检测到循环依赖，无法执行");
            } else {
                response.put("message", "依赖关系有效");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("验证依赖关系失败", e);
            return ResponseEntity.badRequest().body(
                    createErrorResponse("验证失败：" + e.getMessage()));
        }
    }

    /**
     * 转换 Map 列表为 Task 列表
     */
    private List<Task> convertToTasks(List<Map<String, Object>> taskMaps) {
        return taskMaps.stream()
                .map(this::mapToTask)
                .collect(Collectors.toList());
    }

    /**
     * 将 Map 转换为 Task
     */
    @SuppressWarnings("unchecked")
    private Task mapToTask(Map<String, Object> taskMap) {
        Task task = new Task();

        if (taskMap.containsKey("taskId")) {
            task.setTaskId((String) taskMap.get("taskId"));
        }

        if (taskMap.containsKey("name")) {
            task.setName((String) taskMap.get("name"));
        }

        if (taskMap.containsKey("description")) {
            task.setDescription((String) taskMap.get("description"));
        }

        if (taskMap.containsKey("type")) {
            task.setType(Task.TaskType.valueOf((String) taskMap.get("type")));
        }

        if (taskMap.containsKey("status")) {
            task.setStatus(Task.TaskStatus.valueOf((String) taskMap.get("status")));
        }

        if (taskMap.containsKey("toolName")) {
            String toolName = (String) taskMap.get("toolName");
            // 规范化工具名称
            toolName = normalizeToolName(toolName);
            task.setToolName(toolName);
        }

        if (taskMap.containsKey("toolParams")) {
            task.setToolParams((Map<String, Object>) taskMap.get("toolParams"));
        }

        if (taskMap.containsKey("priority")) {
            Object priorityObj = taskMap.get("priority");
            if (priorityObj instanceof Number) {
                task.setPriority(((Number) priorityObj).intValue());
            }
        }

        if (taskMap.containsKey("dependencies")) {
            task.setDependencies((List<String>) taskMap.get("dependencies"));
        }

        return task;
    }

    /**
     * 规范化工具名称（修复 AI 生成的不合法名称）
     */
    private String normalizeToolName(String toolName) {
        if (toolName == null || toolName.isEmpty()) {
            return "";
        }

        String lowerName = toolName.toLowerCase().trim();

        // 移除括号及其内容（例如：getCurrentWeather() 变成 getcurrentweather）
        lowerName = lowerName.replaceAll("\\([^)]*\\)", "").trim();

        logger.debug("原始工具名: {}, 规范化后: {}", toolName, lowerName);

        // 将各种变体转换为标准工具名称
        if (lowerName.contains("weather") || lowerName.contains("current")) {
            logger.debug("映射为: weather");
            return "weather";
        }
        if (lowerName.contains("attraction") || lowerName.contains("recommend") || lowerName.contains("tourist")) {
            logger.debug("映射为: tourist_attraction");
            return "tourist_attraction";
        }

        // 如果不是已知的工具名，使用原名（可能会导致执行失败，但至少不会隐式失败）
        logger.debug("未能规范化，返回原名: {}", toolName);
        return toolName;
    }

    /**
     * 检测循环依赖（DFS）
     */
    private boolean hasCyclicDependency(Task task, List<Task> allTasks,
                                       Map<String, Boolean> visited,
                                       Map<String, Boolean> recursionStack) {
        String taskId = task.getTaskId();

        visited.put(taskId, true);
        recursionStack.put(taskId, true);

        for (String depId : task.getDependencies()) {
            Task depTask = allTasks.stream()
                    .filter(t -> t.getTaskId().equals(depId))
                    .findFirst()
                    .orElse(null);

            if (depTask == null) {
                continue;
            }

            if (!visited.getOrDefault(depId, false)) {
                if (hasCyclicDependency(depTask, allTasks, visited, recursionStack)) {
                    return true;
                }
            } else if (recursionStack.getOrDefault(depId, false)) {
                return true; // 检测到循环
            }
        }

        recursionStack.put(taskId, false);
        return false;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", message);
        return error;
    }
}


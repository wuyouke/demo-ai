package com.example.demo_ai.service;

import com.example.demo_ai.model.ChatResponse;
import com.example.demo_ai.model.Task;
import com.example.demo_ai.model.Task.TaskStatus;
import com.example.demo_ai.model.Task.TaskType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 任务规划器 - 使用 AI 分解复杂任务
 * 能够将用户的高级需求分解成多个可执行的子任务
 */
public class TaskPlanner {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TaskPlanner.class);

    private final ObjectMapper objectMapper;
    private final int maxTasks;
    private final ConversationService conversationService;

    /**
     * 构造函数 - 依赖注入 ConversationService
     */
    public TaskPlanner(ConversationService conversationService) {
        this.conversationService = conversationService;
        this.maxTasks = 10;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 规划任务 - 将用户目标分解为子任务
     *
     * @param userGoal 用户的高级目标
     * @param context 上下文信息
     * @return 任务列表
     */
    public List<Task> planTasks(String userGoal, Map<String, Object> context) {
        try {
            // 构建 AI 提示词
            String prompt = buildPlanningPrompt(userGoal, context);

            // 调用 AI 进行任务分解
            ChatResponse response = conversationService.chat(prompt, UUID.randomUUID().toString());
            String planningResult = response.getResponse();

            // 解析 AI 响应，提取任务
            List<Task> tasks = parsePlanningResult(planningResult, userGoal);

            // 优化任务关系（检测依赖）
            optimizeTaskDependencies(tasks);

            // 排序任务（按优先级和依赖关系）
            List<Task> sortedTasks = sortTasksByDependencies(tasks);

            return sortedTasks;

        } catch (Exception e) {
            logger.error("任务规划失败: {}", e.getMessage());
            return createFallbackTasks(userGoal);
        }
    }

    /**
     * 构建任务规划提示词
     */
    private String buildPlanningPrompt(String userGoal, Map<String, Object> context) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个智能任务规划助手。\n\n");
        prompt.append("用户的目标是：").append(userGoal).append("\n\n");

        if (context != null && !context.isEmpty()) {
            prompt.append("上下文信息：\n");
            for (Map.Entry<String, Object> entry : context.entrySet()) {
                prompt.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
            prompt.append("\n");
        }

        prompt.append("请按照以下 JSON 格式分解这个目标为 2-5 个具体的可执行子任务。\n");
        prompt.append("每个任务应该是独立的、可测量的、并且可以通过工具执行。\n\n");
        prompt.append("【可用的工具列表】\n");
        prompt.append("1. weather: 查询天气信息\n");
        prompt.append("   - 必需参数: city (城市名，例如：北京、上海)\n");
        prompt.append("   - 示例参数: { \"city\": \"北京\" }\n");
        prompt.append("2. tourist_attraction: 推荐旅游景点\n");
        prompt.append("   - 必需参数: location (城市名)\n");
        prompt.append("   - 可选参数: preferences (用户偏好)\n");
        prompt.append("   - 示例参数: { \"location\": \"北京\" }\n\n");

        prompt.append("返回格式（返回纯 JSON 数组，不要其他内容）：\n");
        prompt.append("[\n");
        prompt.append("  {\n");
        prompt.append("    \"name\": \"任务名称\",\n");
        prompt.append("    \"description\": \"任务的详细描述\",\n");
        prompt.append("    \"type\": \"TOOL_CALL|INFORMATION_QUERY|DATA_ANALYSIS|DECISION_MAKING|PLANNING\",\n");
        prompt.append("    \"toolName\": \"weather 或 tourist_attraction\",\n");
        prompt.append("    \"toolParams\": { \"city\": \"北京\" },\n");
        prompt.append("    \"priority\": 1-10\n");
        prompt.append("  }\n");
        prompt.append("]\n");

        prompt.append("\n【严格要求】\n");
        prompt.append("1. 返回的必须是有效的 JSON 数组，不要其他任何内容\n");
        prompt.append("2. toolName 只能是：weather 或 tourist_attraction\n");
        prompt.append("3. 不要创造新的工具名称，必须使用上面提供的工具名称\n");
        prompt.append("4. 不要包含 markdown 代码块或其他文本解释\n");
        prompt.append("5. 每个任务的优先级应该按执行顺序递减\n");

        return prompt.toString();
    }

    /**
     * 解析 AI 规划结果
     */
    private List<Task> parsePlanningResult(String result, String originalGoal) {
        List<Task> tasks = new ArrayList<>();

        try {
            // 清理结果（移除 markdown 代码块）
            String cleanedResult = cleanJsonResult(result);

            // 解析 JSON 数组
            List<Map<String, Object>> taskMaps = objectMapper.readValue(cleanedResult, List.class);

            for (int i = 0; i < Math.min(taskMaps.size(), maxTasks); i++) {
                Map<String, Object> taskMap = taskMaps.get(i);
                Task task = mapToTask(taskMap, i);
                if (task != null) {
                    tasks.add(task);
                }
            }

            // 如果没有解析到任务，返回降级方案
            if (tasks.isEmpty()) {
                return createFallbackTasks(originalGoal);
            }

        } catch (Exception e) {
            System.err.println("解析任务规划结果失败: " + e.getMessage());
            return createFallbackTasks(originalGoal);
        }

        return tasks;
    }

    /**
     * 清理 JSON 结果（移除 markdown 标记）
     */
    private String cleanJsonResult(String result) {
        // 移除 ```json 和 ``` 标记
        result = result.replaceAll("```json\\s*", "");
        result = result.replaceAll("```\\s*", "");
        result = result.trim();
        return result;
    }

    /**
     * 将 Map 转换为 Task 对象
     */
    private Task mapToTask(Map<String, Object> taskMap, int index) {
        try {
            String name = (String) taskMap.getOrDefault("name", "任务" + (index + 1));
            String description = (String) taskMap.getOrDefault("description", "");
            String typeStr = (String) taskMap.getOrDefault("type", "TOOL_CALL");
            String toolName = (String) taskMap.getOrDefault("toolName", "");

            // 修复不合法的工具名称
            toolName = normalizeToolName(toolName);

            @SuppressWarnings("unchecked")
            Map<String, Object> toolParams = (Map<String, Object>) taskMap.getOrDefault("toolParams", new HashMap<>());
            Object priorityObj = taskMap.get("priority");
            int priority = priorityObj instanceof Number ? ((Number) priorityObj).intValue() : (10 - index);

            TaskType type = TaskType.valueOf(typeStr);

            Task task = Task.builder()
                    .name(name)
                    .description(description)
                    .type(type)
                    .toolName(toolName)
                    .toolParams(toolParams)
                    .priority(Math.max(1, Math.min(10, priority)))
                    .build();

            return task;
        } catch (Exception e) {
            System.err.println("转换任务失败: " + e.getMessage());
            return null;
        }
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

        System.out.println("原始工具名: " + toolName + ", 规范化后: " + lowerName);

        // 将各种变体转换为标准工具名称
        if (lowerName.contains("weather") || lowerName.contains("current")) {
            System.out.println("映射为: weather");
            return "weather";
        }
        if (lowerName.contains("attraction") || lowerName.contains("recommend") || lowerName.contains("tourist")) {
            System.out.println("映射为: tourist_attraction");
            return "tourist_attraction";
        }

        // 如果不是已知的工具名，使用原名（可能会导致执行失败，但至少不会隐式失败）
        System.out.println("未能规范化，返回原名: " + toolName);
        return toolName;
    }

    /**
     * 优化任务依赖关系
     * 检测任务之间的逻辑依赖
     */
    private void optimizeTaskDependencies(List<Task> tasks) {
        for (int i = 0; i < tasks.size(); i++) {
            Task currentTask = tasks.get(i);

            // 根据任务类型和内容推断依赖
            for (int j = 0; j < i; j++) {
                Task prevTask = tasks.get(j);

                // 如果当前任务需要前一个任务的结果，添加依赖
                if (shouldDependOn(currentTask, prevTask)) {
                    currentTask.getDependencies().add(prevTask.getTaskId());
                }
            }
        }
    }

    /**
     * 判断任务是否依赖于另一个任务
     */
    private boolean shouldDependOn(Task currentTask, Task prevTask) {
        // 如果前一个任务是信息查询，后续分析任务可能需要它的结果
        if (prevTask.getType() == TaskType.INFORMATION_QUERY
            && (currentTask.getType() == TaskType.DATA_ANALYSIS
                || currentTask.getType() == TaskType.DECISION_MAKING)) {
            return true;
        }

        // 如果前一个任务是规划，后续执行任务需要它的结果
        if (prevTask.getType() == TaskType.PLANNING
            && currentTask.getType() == TaskType.TOOL_CALL) {
            return true;
        }

        return false;
    }

    /**
     * 按依赖关系排序任务（拓扑排序）
     */
    private List<Task> sortTasksByDependencies(List<Task> tasks) {
        Map<String, Task> taskMap = new HashMap<>();
        for (Task task : tasks) {
            taskMap.put(task.getTaskId(), task);
        }

        List<Task> sortedTasks = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> visiting = new HashSet<>();

        for (Task task : tasks) {
            if (!visited.contains(task.getTaskId())) {
                topologicalSort(task, taskMap, visited, visiting, sortedTasks);
            }
        }

        return sortedTasks;
    }

    /**
     * 深度优先搜索进行拓扑排序
     */
    private void topologicalSort(Task task, Map<String, Task> taskMap, Set<String> visited,
                                 Set<String> visiting, List<Task> result) {
        String taskId = task.getTaskId();

        if (visited.contains(taskId)) {
            return;
        }

        if (visiting.contains(taskId)) {
            // 检测到循环依赖
            System.err.println("警告：检测到循环依赖，任务: " + task.getName());
            return;
        }

        visiting.add(taskId);

        // 递归排序依赖的任务
        for (String depId : task.getDependencies()) {
            Task depTask = taskMap.get(depId);
            if (depTask != null) {
                topologicalSort(depTask, taskMap, visited, visiting, result);
            }
        }

        visiting.remove(taskId);
        visited.add(taskId);
        result.add(task);
    }

    /**
     * 创建降级方案（当 AI 规划失败时）
     */
    private List<Task> createFallbackTasks(String userGoal) {
        List<Task> fallbackTasks = new ArrayList<>();

        // 创建一个通用任务
        Task task = Task.builder()
                .name("处理用户请求")
                .description("直接处理用户的请求: " + userGoal)
                .type(TaskType.TOOL_CALL)
                .toolName("general_query")
                .priority(10)
                .build();

        fallbackTasks.add(task);
        return fallbackTasks;
    }

    /**
     * 获取任务执行顺序
     * 返回按优先级和依赖排序的可执行任务列表
     */
    public List<Task> getExecutionOrder(List<Task> tasks) {
        return tasks.stream()
                .filter(task -> task.getDependencies().isEmpty() || allDependenciesCompleted(task, tasks))
                .sorted((t1, t2) -> Integer.compare(t2.getPriority(), t1.getPriority()))
                .collect(Collectors.toList());
    }

    /**
     * 检查所有依赖是否已完成
     */
    private boolean allDependenciesCompleted(Task task, List<Task> allTasks) {
        Map<String, Task> taskMap = new HashMap<>();
        for (Task t : allTasks) {
            taskMap.put(t.getTaskId(), t);
        }

        for (String depId : task.getDependencies()) {
            Task depTask = taskMap.get(depId);
            if (depTask == null || depTask.getStatus() != TaskStatus.COMPLETED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 格式化任务列表为可读的文本
     */
    public String formatTaskPlan(List<Task> tasks) {
        StringBuilder sb = new StringBuilder();
        sb.append("📋 **任务规划结果**\n\n");
        sb.append("共分解为 ").append(tasks.size()).append(" 个子任务：\n\n");

        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            sb.append((i + 1)).append(". ").append(task.getName()).append("\n");
            sb.append("   - 类型: ").append(task.getType().getChineseName()).append("\n");
            sb.append("   - 描述: ").append(task.getDescription()).append("\n");

            if (task.getToolName() != null && !task.getToolName().isEmpty()) {
                sb.append("   - 工具: ").append(task.getToolName()).append("\n");
            }

            if (!task.getDependencies().isEmpty()) {
                sb.append("   - 依赖: ").append(String.join(", ", task.getDependencies())).append("\n");
            }

            sb.append("   - 优先级: ").append(task.getPriority()).append("/10\n");
            sb.append("\n");
        }

        return sb.toString();
    }
}


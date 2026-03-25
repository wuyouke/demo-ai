package com.example.demo_ai.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 任务定义 - 表示一个可执行的任务
 */
public class Task {

    /**
     * 任务 ID
     */
    private String taskId;

    /**
     * 任务名称
     */
    private String name;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 任务类型
     */
    private TaskType type;

    /**
     * 任务状态
     */
    private TaskStatus status;

    /**
     * 执行的工具名称
     */
    private String toolName;

    /**
     * 工具参数
     */
    private Map<String, Object> toolParams;

    /**
     * 任务优先级（1-10，10 最高）
     */
    private int priority;

    /**
     * 依赖的任务 ID 列表
     */
    private List<String> dependencies;

    /**
     * 任务结果
     */
    private Object result;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 创建时间
     */
    private long createdAt;

    /**
     * 开始执行时间
     */
    private long startedAt;

    /**
     * 完成时间
     */
    private long completedAt;

    /**
     * 执行耗时（毫秒）
     */
    private long executionTime;

    /**
     * 任务类型枚举
     */
    public enum TaskType {
        TOOL_CALL("工具调用"),
        INFORMATION_QUERY("信息查询"),
        DATA_ANALYSIS("数据分析"),
        DECISION_MAKING("决策制定"),
        PLANNING("计划制定"),
        COMMUNICATION("通讯"),
        OTHER("其他");

        private final String chineseName;

        TaskType(String chineseName) {
            this.chineseName = chineseName;
        }

        public String getChineseName() {
            return chineseName;
        }
    }

    /**
     * 任务状态枚举
     */
    public enum TaskStatus {
        PENDING("待执行"),
        EXECUTING("执行中"),
        COMPLETED("已完成"),
        FAILED("失败"),
        SKIPPED("已跳过");

        private final String chineseName;

        TaskStatus(String chineseName) {
            this.chineseName = chineseName;
        }

        public String getChineseName() {
            return chineseName;
        }
    }

    // 构造函数
    public Task() {
        this.taskId = UUID.randomUUID().toString();
        this.status = TaskStatus.PENDING;
        this.priority = 5;
        this.dependencies = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
        this.toolParams = new HashMap<>();
    }

    // Getters and Setters
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TaskType getType() { return type; }
    public void setType(TaskType type) { this.type = type; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }

    public Map<String, Object> getToolParams() { return toolParams; }
    public void setToolParams(Map<String, Object> toolParams) { this.toolParams = toolParams; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public List<String> getDependencies() { return dependencies; }
    public void setDependencies(List<String> dependencies) { this.dependencies = dependencies; }

    public Object getResult() { return result; }
    public void setResult(Object result) { this.result = result; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getStartedAt() { return startedAt; }
    public void setStartedAt(long startedAt) { this.startedAt = startedAt; }

    public long getCompletedAt() { return completedAt; }
    public void setCompletedAt(long completedAt) { this.completedAt = completedAt; }

    public long getExecutionTime() { return executionTime; }
    public void setExecutionTime(long executionTime) { this.executionTime = executionTime; }

    // Builder 模式
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String description;
        private TaskType type;
        private String toolName;
        private Map<String, Object> toolParams;
        private int priority = 5;
        private List<String> dependencies;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder type(TaskType type) {
            this.type = type;
            return this;
        }

        public Builder toolName(String toolName) {
            this.toolName = toolName;
            return this;
        }

        public Builder toolParams(Map<String, Object> toolParams) {
            this.toolParams = toolParams;
            return this;
        }

        public Builder priority(int priority) {
            this.priority = priority;
            return this;
        }

        public Builder dependencies(List<String> dependencies) {
            this.dependencies = dependencies;
            return this;
        }

        public Builder addDependency(String taskId) {
            if (this.dependencies == null) {
                this.dependencies = new ArrayList<>();
            }
            this.dependencies.add(taskId);
            return this;
        }

        public Task build() {
            Task task = new Task();
            task.name = this.name;
            task.description = this.description;
            task.type = this.type;
            task.toolName = this.toolName;
            task.toolParams = this.toolParams != null ? this.toolParams : new HashMap<>();
            task.priority = this.priority;
            task.dependencies = this.dependencies != null ? this.dependencies : new ArrayList<>();
            return task;
        }
    }
}


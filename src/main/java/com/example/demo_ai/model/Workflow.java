package com.example.demo_ai.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 工作流模型 - 支持复杂的流程编排
 * 包括条件分支、循环、重试等高级特性
 */
public class Workflow {

    private String workflowId;
    private String name;
    private String description;
    private WorkflowNode startNode;
    private List<WorkflowNode> nodes;
    private Map<String, Object> context;  // 工作流上下文数据
    private WorkflowStatus status;
    private long createdAt;
    private long updatedAt;

    public enum WorkflowStatus {
        PENDING,      // 待执行
        RUNNING,      // 执行中
        COMPLETED,    // 已完成
        FAILED,       // 已失败
        PAUSED,       // 已暂停
        CANCELLED     // 已取消
    }

    public Workflow() {
        this.workflowId = UUID.randomUUID().toString();
        this.nodes = new ArrayList<>();
        this.context = new HashMap<>();
        this.status = WorkflowStatus.PENDING;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public WorkflowNode getStartNode() {
        return startNode;
    }

    public void setStartNode(WorkflowNode startNode) {
        this.startNode = startNode;
    }

    public List<WorkflowNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<WorkflowNode> nodes) {
        this.nodes = nodes;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public void setContext(Map<String, Object> context) {
        this.context = context;
    }

    public WorkflowStatus getStatus() {
        return status;
    }

    public void setStatus(WorkflowStatus status) {
        this.status = status;
        this.updatedAt = System.currentTimeMillis();
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "Workflow{" +
                "workflowId='" + workflowId + '\'' +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", nodeCount=" + nodes.size() +
                '}';
    }

    /**
     * 工作流节点 - 表示工作流中的一个步骤
     */
    public static class WorkflowNode {
        private String nodeId;
        private String name;
        private NodeType type;
        private Map<String, Object> config;      // 节点配置
        private List<String> nextNodeIds;        // 后续节点
        private NodeStatus status;
        private Object result;                    // 节点执行结果
        private String errorMessage;

        public enum NodeType {
            START,            // 开始节点
            END,              // 结束节点
            TASK,             // 任务节点（调用工具）
            DECISION,         // 决策节点（条件分支）
            LOOP,             // 循环节点
            PARALLEL,         // 并行节点
            WAIT,             // 等待节点
            RETRY             // 重试节点
        }

        public enum NodeStatus {
            PENDING,
            EXECUTING,
            COMPLETED,
            FAILED,
            SKIPPED
        }

        public WorkflowNode() {
            this.nodeId = UUID.randomUUID().toString();
            this.nextNodeIds = new ArrayList<>();
            this.status = NodeStatus.PENDING;
            this.config = new HashMap<>();
        }

        public String getNodeId() {
            return nodeId;
        }

        public void setNodeId(String nodeId) {
            this.nodeId = nodeId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public NodeType getType() {
            return type;
        }

        public void setType(NodeType type) {
            this.type = type;
        }

        public Map<String, Object> getConfig() {
            return config;
        }

        public void setConfig(Map<String, Object> config) {
            this.config = config;
        }

        public List<String> getNextNodeIds() {
            return nextNodeIds;
        }

        public void setNextNodeIds(List<String> nextNodeIds) {
            this.nextNodeIds = nextNodeIds;
        }

        public NodeStatus getStatus() {
            return status;
        }

        public void setStatus(NodeStatus status) {
            this.status = status;
        }

        public Object getResult() {
            return result;
        }

        public void setResult(Object result) {
            this.result = result;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }

    /**
     * 决策条件 - 用于条件分支
     */
    public static class DecisionCondition {
        private String field;           // 要判断的字段
        private String operator;        // 操作符: ==, !=, <, >, <=, >=, in, contains
        private Object value;           // 比较值
        private String nextNodeId;      // 条件满足时的下一个节点

        public DecisionCondition() {}

        public DecisionCondition(String field, String operator, Object value, String nextNodeId) {
            this.field = field;
            this.operator = operator;
            this.value = value;
            this.nextNodeId = nextNodeId;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getOperator() {
            return operator;
        }

        public void setOperator(String operator) {
            this.operator = operator;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public String getNextNodeId() {
            return nextNodeId;
        }

        public void setNextNodeId(String nextNodeId) {
            this.nextNodeId = nextNodeId;
        }
    }

    /**
     * 循环配置
     */
    public static class LoopConfig {
        private String collection;      // 集合的字段名
        private String itemVar;         // 循环变量名
        private String bodyNodeId;      // 循环体节点
        private int maxIterations;      // 最大迭代次数
        private int currentIteration;   // 当前迭代次数

        public LoopConfig() {
            this.maxIterations = 100;   // 防止无限循环
            this.currentIteration = 0;
        }

        public String getCollection() {
            return collection;
        }

        public void setCollection(String collection) {
            this.collection = collection;
        }

        public String getItemVar() {
            return itemVar;
        }

        public void setItemVar(String itemVar) {
            this.itemVar = itemVar;
        }

        public String getBodyNodeId() {
            return bodyNodeId;
        }

        public void setBodyNodeId(String bodyNodeId) {
            this.bodyNodeId = bodyNodeId;
        }

        public int getMaxIterations() {
            return maxIterations;
        }

        public void setMaxIterations(int maxIterations) {
            this.maxIterations = maxIterations;
        }

        public int getCurrentIteration() {
            return currentIteration;
        }

        public void setCurrentIteration(int currentIteration) {
            this.currentIteration = currentIteration;
        }
    }

    /**
     * 重试配置
     */
    public static class RetryConfig {
        private int maxRetries;         // 最大重试次数
        private long delayMs;           // 重试延迟（毫秒）
        private double backoffMultiplier; // 指数退避倍数
        private int currentRetry;       // 当前重试次数

        public RetryConfig() {
            this.maxRetries = 3;
            this.delayMs = 1000;        // 默认 1 秒
            this.backoffMultiplier = 1.5;
            this.currentRetry = 0;
        }

        public int getMaxRetries() {
            return maxRetries;
        }

        public void setMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
        }

        public long getDelayMs() {
            return delayMs;
        }

        public void setDelayMs(long delayMs) {
            this.delayMs = delayMs;
        }

        public double getBackoffMultiplier() {
            return backoffMultiplier;
        }

        public void setBackoffMultiplier(double backoffMultiplier) {
            this.backoffMultiplier = backoffMultiplier;
        }

        public int getCurrentRetry() {
            return currentRetry;
        }

        public void setCurrentRetry(int currentRetry) {
            this.currentRetry = currentRetry;
        }
    }
}


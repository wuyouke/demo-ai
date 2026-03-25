package com.example.demo_ai.service;

import com.example.demo_ai.model.Workflow;
import com.example.demo_ai.model.Workflow.DecisionCondition;
import com.example.demo_ai.model.Workflow.LoopConfig;
import com.example.demo_ai.model.Workflow.RetryConfig;
import com.example.demo_ai.model.Workflow.WorkflowNode;
import com.example.demo_ai.model.Workflow.WorkflowNode.NodeStatus;
import com.example.demo_ai.tools.ToolRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 工作流执行引擎 - 支持复杂的工作流编排和执行
 */
public class WorkflowEngine {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowEngine.class);
    private final ToolRegistry toolRegistry;
    private final ExecutorService executorService;

    public WorkflowEngine(ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
        this.executorService = Executors.newFixedThreadPool(5);
    }

    /**
     * 执行工作流
     */
    public void executeWorkflow(Workflow workflow) {
        logger.info("开始执行工作流: {}", workflow.getName());
        workflow.setStatus(Workflow.WorkflowStatus.RUNNING);

        // 在后台线程中执行
        executorService.submit(() -> {
            try {
                // 从起始节点开始执行
                String currentNodeId = workflow.getStartNode().getNodeId();
                while (currentNodeId != null) {
                    WorkflowNode node = findNode(workflow, currentNodeId);
                    if (node == null) break;

                    currentNodeId = executeNode(workflow, node);
                }

                workflow.setStatus(Workflow.WorkflowStatus.COMPLETED);
                logger.info("工作流执行完成: {}", workflow.getName());
            } catch (Exception e) {
                workflow.setStatus(Workflow.WorkflowStatus.FAILED);
                logger.error("工作流执行失败: {}", workflow.getName(), e);
            }
        });
    }

    /**
     * 执行单个节点
     */
    private String executeNode(Workflow workflow, WorkflowNode node) throws Exception {
        logger.info("执行节点: {} (类型: {})", node.getName(), node.getType());
        node.setStatus(NodeStatus.EXECUTING);

        String nextNodeId = null;

        try {
            switch (node.getType()) {
                case START:
                    nextNodeId = executeStartNode(node);
                    break;
                case END:
                    nextNodeId = executeEndNode(node);
                    break;
                case TASK:
                    nextNodeId = executeTaskNode(workflow, node);
                    break;
                case DECISION:
                    nextNodeId = executeDecisionNode(workflow, node);
                    break;
                case LOOP:
                    nextNodeId = executeLoopNode(workflow, node);
                    break;
                case RETRY:
                    nextNodeId = executeRetryNode(workflow, node);
                    break;
                case WAIT:
                    nextNodeId = executeWaitNode(node);
                    break;
                case PARALLEL:
                    nextNodeId = executeParallelNode(workflow, node);
                    break;
            }

            node.setStatus(NodeStatus.COMPLETED);
            logger.info("节点执行完成: {}", node.getName());
        } catch (Exception e) {
            node.setStatus(NodeStatus.FAILED);
            node.setErrorMessage(e.getMessage());
            logger.error("节点执行失败: {}", node.getName(), e);
            throw e;
        }

        return nextNodeId;
    }

    /**
     * 执行开始节点
     */
    private String executeStartNode(WorkflowNode node) {
        if (node.getNextNodeIds().isEmpty()) return null;
        return node.getNextNodeIds().get(0);
    }

    /**
     * 执行结束节点
     */
    private String executeEndNode(WorkflowNode node) {
        return null;  // 工作流结束
    }

    /**
     * 执行任务节点 - 调用工具
     */
    private String executeTaskNode(Workflow workflow, WorkflowNode node) throws Exception {
        String toolName = (String) node.getConfig().get("toolName");
        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) node.getConfig().get("params");

        if (toolName == null || params == null) {
            throw new IllegalArgumentException("任务节点必须指定 toolName 和 params");
        }

        logger.info("调用工具: {}", toolName);
        // 这里可以集成工具调用逻辑
        node.setResult("工具执行结果");

        if (node.getNextNodeIds().isEmpty()) return null;
        return node.getNextNodeIds().get(0);
    }

    /**
     * 执行决策节点 - 条件分支
     */
    private String executeDecisionNode(Workflow workflow, WorkflowNode node) throws Exception {
        @SuppressWarnings("unchecked")
        List<DecisionCondition> conditions = (List<DecisionCondition>) node.getConfig().get("conditions");
        String defaultNextNodeId = (String) node.getConfig().get("defaultNextNodeId");

        if (conditions == null || conditions.isEmpty()) {
            return defaultNextNodeId;
        }

        // 逐个检查条件
        for (DecisionCondition condition : conditions) {
            if (evaluateCondition(workflow.getContext(), condition)) {
                logger.info("条件满足: {} {} {}", condition.getField(), condition.getOperator(), condition.getValue());
                return condition.getNextNodeId();
            }
        }

        // 所有条件都不满足，使用默认路径
        return defaultNextNodeId;
    }

    /**
     * 执行循环节点
     */
    private String executeLoopNode(Workflow workflow, WorkflowNode node) throws Exception {
        LoopConfig loopConfig = (LoopConfig) node.getConfig().get("loopConfig");
        if (loopConfig == null) {
            throw new IllegalArgumentException("循环节点必须指定 loopConfig");
        }

        @SuppressWarnings("unchecked")
        List<Object> collection = (List<Object>) workflow.getContext().get(loopConfig.getCollection());
        if (collection == null || collection.isEmpty()) {
            // 集合为空，跳过循环
            if (node.getNextNodeIds().isEmpty()) return null;
            return node.getNextNodeIds().get(0);
        }

        // 遍历集合中的每个元素
        for (int i = 0; i < collection.size() && i < loopConfig.getMaxIterations(); i++) {
            Object item = collection.get(i);
            workflow.getContext().put(loopConfig.getItemVar(), item);
            loopConfig.setCurrentIteration(i + 1);

            logger.info("循环迭代 {}/{}", i + 1, collection.size());
            // 执行循环体
            // ...
        }

        node.setResult("循环完成，迭代次数: " + loopConfig.getCurrentIteration());
        if (node.getNextNodeIds().isEmpty()) return null;
        return node.getNextNodeIds().get(0);
    }

    /**
     * 执行重试节点
     */
    private String executeRetryNode(Workflow workflow, WorkflowNode node) throws Exception {
        RetryConfig retryConfig = (RetryConfig) node.getConfig().get("retryConfig");
        if (retryConfig == null) {
            retryConfig = new RetryConfig();
            node.getConfig().put("retryConfig", retryConfig);
        }

        String targetNodeId = (String) node.getConfig().get("targetNodeId");
        if (targetNodeId == null) {
            throw new IllegalArgumentException("重试节点必须指定 targetNodeId");
        }

        for (int i = 0; i <= retryConfig.getMaxRetries(); i++) {
            try {
                retryConfig.setCurrentRetry(i);
                logger.info("尝试执行 (第 {}/{} 次)", i + 1, retryConfig.getMaxRetries() + 1);

                WorkflowNode targetNode = findNode(workflow, targetNodeId);
                if (targetNode != null) {
                    executeNode(workflow, targetNode);
                    node.setResult("重试成功");
                    break;
                }
            } catch (Exception e) {
                if (i < retryConfig.getMaxRetries()) {
                    long delay = (long) (retryConfig.getDelayMs() * Math.pow(retryConfig.getBackoffMultiplier(), i));
                    logger.info("重试延迟: {}ms", delay);
                    Thread.sleep(delay);
                } else {
                    throw e;
                }
            }
        }

        if (node.getNextNodeIds().isEmpty()) return null;
        return node.getNextNodeIds().get(0);
    }

    /**
     * 执行等待节点
     */
    private String executeWaitNode(WorkflowNode node) throws Exception {
        Long delayMs = (Long) node.getConfig().get("delayMs");
        if (delayMs == null) delayMs = 5000L;

        logger.info("等待 {}ms", delayMs);
        Thread.sleep(delayMs);
        node.setResult("等待完成");

        if (node.getNextNodeIds().isEmpty()) return null;
        return node.getNextNodeIds().get(0);
    }

    /**
     * 执行并行节点
     */
    private String executeParallelNode(Workflow workflow, WorkflowNode node) throws Exception {
        @SuppressWarnings("unchecked")
        List<String> parallelNodeIds = (List<String>) node.getConfig().get("nodeIds");
        if (parallelNodeIds == null || parallelNodeIds.isEmpty()) {
            if (node.getNextNodeIds().isEmpty()) return null;
            return node.getNextNodeIds().get(0);
        }

        logger.info("并行执行 {} 个节点", parallelNodeIds.size());
        // TODO: 使用线程池并行执行这些节点

        node.setResult("并行执行完成");
        if (node.getNextNodeIds().isEmpty()) return null;
        return node.getNextNodeIds().get(0);
    }

    /**
     * 评估条件
     */
    private boolean evaluateCondition(Map<String, Object> context, DecisionCondition condition) {
        Object fieldValue = context.get(condition.getField());
        Object compareValue = condition.getValue();

        switch (condition.getOperator()) {
            case "==":
                return fieldValue != null && fieldValue.equals(compareValue);
            case "!=":
                return fieldValue == null || !fieldValue.equals(compareValue);
            case "<":
                return compare(fieldValue, compareValue) < 0;
            case ">":
                return compare(fieldValue, compareValue) > 0;
            case "<=":
                return compare(fieldValue, compareValue) <= 0;
            case ">=":
                return compare(fieldValue, compareValue) >= 0;
            case "in":
                return compareValue instanceof List && ((List<?>) compareValue).contains(fieldValue);
            case "contains":
                return fieldValue instanceof String && compareValue instanceof String &&
                        ((String) fieldValue).contains((String) compareValue);
            default:
                return false;
        }
    }

    /**
     * 比较两个值
     */
    @SuppressWarnings("unchecked")
    private int compare(Object a, Object b) {
        if (a instanceof Comparable && b instanceof Comparable) {
            return ((Comparable<Object>) a).compareTo(b);
        }
        return 0;
    }

    /**
     * 查找节点
     */
    private WorkflowNode findNode(Workflow workflow, String nodeId) {
        for (WorkflowNode node : workflow.getNodes()) {
            if (node.getNodeId().equals(nodeId)) {
                return node;
            }
        }
        return null;
    }

    /**
     * 关闭引擎
     */
    public void shutdown() {
        executorService.shutdown();
    }
}


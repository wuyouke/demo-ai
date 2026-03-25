package com.example.demo_ai.controller;

import com.example.demo_ai.model.Workflow;
import com.example.demo_ai.model.Workflow.WorkflowNode;
import com.example.demo_ai.service.WorkflowEngine;
import com.example.demo_ai.tools.ToolRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工作流 REST API 控制器
 */
@RestController
@RequestMapping("/api/workflows")
@CrossOrigin(origins = "*", maxAge = 3600)
public class WorkflowController {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowController.class);

    @Autowired
    private ToolRegistry toolRegistry;

    // 存储所有工作流（实际应该用数据库）
    private static final Map<String, Workflow> workflows = new ConcurrentHashMap<>();
    private static final Map<String, WorkflowEngine> engines = new ConcurrentHashMap<>();

    /**
     * 创建工作流
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createWorkflow(@RequestBody Map<String, Object> request) {
        try {
            String name = (String) request.get("name");
            String description = (String) request.get("description");

            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("工作流名称不能为空"));
            }

            Workflow workflow = new Workflow();
            workflow.setName(name);
            workflow.setDescription(description);

            workflows.put(workflow.getWorkflowId(), workflow);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("workflowId", workflow.getWorkflowId());
            response.put("name", workflow.getName());

            logger.info("工作流已创建: {} (ID: {})", name, workflow.getWorkflowId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("创建工作流失败", e);
            return ResponseEntity.badRequest().body(createErrorResponse("创建失败：" + e.getMessage()));
        }
    }

    /**
     * 添加节点
     */
    @PostMapping("/{workflowId}/nodes")
    public ResponseEntity<Map<String, Object>> addNode(
            @PathVariable String workflowId,
            @RequestBody Map<String, Object> nodeData) {
        try {
            Workflow workflow = workflows.get(workflowId);
            if (workflow == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("工作流不存在"));
            }

            WorkflowNode node = new WorkflowNode();
            node.setName((String) nodeData.get("name"));
            node.setType(WorkflowNode.NodeType.valueOf((String) nodeData.get("type")));

            @SuppressWarnings("unchecked")
            Map<String, Object> config = (Map<String, Object>) nodeData.get("config");
            if (config != null) {
                node.setConfig(config);
            }

            // 如果是开始节点，设为起始节点
            if (node.getType() == WorkflowNode.NodeType.START) {
                workflow.setStartNode(node);
            }

            workflow.getNodes().add(node);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("nodeId", node.getNodeId());
            response.put("name", node.getName());

            logger.info("节点已添加: {}", node.getName());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("添加节点失败", e);
            return ResponseEntity.badRequest().body(createErrorResponse("添加失败：" + e.getMessage()));
        }
    }

    /**
     * 连接节点
     */
    @PostMapping("/{workflowId}/connect")
    public ResponseEntity<Map<String, Object>> connectNodes(
            @PathVariable String workflowId,
            @RequestBody Map<String, Object> connection) {
        try {
            Workflow workflow = workflows.get(workflowId);
            if (workflow == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("工作流不存在"));
            }

            String fromNodeId = (String) connection.get("fromNodeId");
            String toNodeId = (String) connection.get("toNodeId");

            WorkflowNode fromNode = findNode(workflow, fromNodeId);
            if (fromNode == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("源节点不存在"));
            }

            fromNode.getNextNodeIds().add(toNodeId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "节点已连接");

            logger.info("节点已连接: {} -> {}", fromNodeId, toNodeId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("连接节点失败", e);
            return ResponseEntity.badRequest().body(createErrorResponse("连接失败：" + e.getMessage()));
        }
    }

    /**
     * 执行工作流
     */
    @PostMapping("/{workflowId}/execute")
    public ResponseEntity<Map<String, Object>> executeWorkflow(@PathVariable String workflowId) {
        try {
            Workflow workflow = workflows.get(workflowId);
            if (workflow == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("工作流不存在"));
            }

            if (workflow.getStartNode() == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("工作流没有开始节点"));
            }

            WorkflowEngine engine = new WorkflowEngine(toolRegistry);
            engines.put(workflowId, engine);
            engine.executeWorkflow(workflow);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("workflowId", workflowId);
            response.put("status", workflow.getStatus().toString());

            logger.info("工作流已启动执行: {}", workflowId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("执行工作流失败", e);
            return ResponseEntity.badRequest().body(createErrorResponse("执行失败：" + e.getMessage()));
        }
    }

    /**
     * 获取工作流状态
     */
    @GetMapping("/{workflowId}/status")
    public ResponseEntity<Map<String, Object>> getWorkflowStatus(@PathVariable String workflowId) {
        try {
            Workflow workflow = workflows.get(workflowId);
            if (workflow == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("工作流不存在"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("workflowId", workflowId);
            response.put("name", workflow.getName());
            response.put("status", workflow.getStatus().toString());
            response.put("nodeCount", workflow.getNodes().size());

            // 收集节点信息
            Map<String, Object> nodesInfo = new HashMap<>();
            for (WorkflowNode node : workflow.getNodes()) {
                Map<String, Object> nodeInfo = new HashMap<>();
                nodeInfo.put("name", node.getName());
                nodeInfo.put("type", node.getType().toString());
                nodeInfo.put("status", node.getStatus().toString());
                nodeInfo.put("result", node.getResult());
                nodesInfo.put(node.getNodeId(), nodeInfo);
            }
            response.put("nodes", nodesInfo);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("获取工作流状态失败", e);
            return ResponseEntity.badRequest().body(createErrorResponse("获取失败：" + e.getMessage()));
        }
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

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", message);
        return error;
    }
}


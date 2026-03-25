package com.example.demo_ai.controller;

import com.example.demo_ai.model.Tool;
import com.example.demo_ai.model.ToolResult;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工具注册表 REST API
 */
@RestController
@RequestMapping("/api/tools")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ToolController {

    private static final Logger logger = LoggerFactory.getLogger(ToolController.class);

    @Autowired
    private ToolRegistry toolRegistry;

    /**
     * 获取所有可用工具列表
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getAllTools() {
        try {
            List<Tool> tools = toolRegistry.getAllTools();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("tools", tools);
            response.put("count", tools.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取工具列表失败", e);
            return ResponseEntity.badRequest().body(createErrorResponse("获取工具列表失败: " + e.getMessage()));
        }
    }

    /**
     * 根据分类获取工具
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<Map<String, Object>> getToolsByCategory(
            @PathVariable String category) {
        try {
            Tool.ToolCategory categoryEnum = Tool.ToolCategory.valueOf(category.toUpperCase());
            List<Tool> tools = toolRegistry.getToolsByCategory(categoryEnum);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("tools", tools);
            response.put("count", tools.size());
            response.put("category", category);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse("无效的分类: " + category));
        } catch (Exception e) {
            logger.error("按分类获取工具失败", e);
            return ResponseEntity.badRequest().body(createErrorResponse("获取失败：" + e.getMessage()));
        }
    }

    /**
     * 搜索工具
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchTools(
            @RequestParam String q) {
        try {
            List<Tool> tools = toolRegistry.searchTools(q);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("tools", tools);
            response.put("count", tools.size());
            response.put("query", q);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("搜索工具失败", e);
            return ResponseEntity.badRequest().body(createErrorResponse("搜索失败：" + e.getMessage()));
        }
    }

    /**
     * 执行工具
     */
    @PostMapping("/{toolName}/execute")
    public ResponseEntity<Map<String, Object>> executeTool(
            @PathVariable String toolName,
            @RequestBody Map<String, Object> params) {
        try {
            logger.info("执行工具：{}, 参数：{}", toolName, params);

            ToolResult result = toolRegistry.executeTool(toolName, params);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("data", result.getData());
            response.put("summary", result.getSummary());
            response.put("executionTime", result.getExecutionTime());

            if (!result.isSuccess()) {
                response.put("error", result.getErrorMessage());
                return ResponseEntity.badRequest().body(response);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("执行工具失败：{}", toolName, e);
            return ResponseEntity.internalServerError().body(
                    createErrorResponse("执行失败：" + e.getMessage()));
        }
    }

    /**
     * 获取工具详情
     */
    @GetMapping("/{toolName}")
    public ResponseEntity<Map<String, Object>> getToolDetail(
            @PathVariable String toolName) {
        try {
            List<Tool> tools = toolRegistry.getAllTools();
            Tool targetTool = tools.stream()
                    .filter(t -> t.getName().equalsIgnoreCase(toolName))
                    .findFirst()
                    .orElse(null);

            if (targetTool == null) {
                return ResponseEntity.badRequest().body(
                        createErrorResponse("工具不存在：" + toolName));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("tool", targetTool);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取工具详情失败", e);
            return ResponseEntity.badRequest().body(createErrorResponse("获取失败：" + e.getMessage()));
        }
    }

    /**
     * 获取工具统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getToolStats() {
        try {
            List<Tool> allTools = toolRegistry.getAllTools();

            Map<String, Long> categoryCount = new HashMap<>();
            for (Tool tool : allTools) {
                String categoryName = tool.getCategory() != null ?
                        tool.getCategory().name() : "OTHER";
                categoryCount.put(categoryName,
                        categoryCount.getOrDefault(categoryName, 0L) + 1);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalTools", allTools.size());
            response.put("categories", categoryCount);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取统计信息失败", e);
            return ResponseEntity.badRequest().body(createErrorResponse("获取失败：" + e.getMessage()));
        }
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", message);
        return error;
    }
}


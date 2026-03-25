package com.example.demo_ai.tools;

import com.example.demo_ai.model.Tool;
import com.example.demo_ai.model.ToolResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 工具注册表 - 管理所有可用的工具
 */
@Component
public class ToolRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ToolRegistry.class);

    /**
     * 所有注册的执行器
     */
    private final Map<String, ToolExecutor> executors = new HashMap<>();

    /**
     * 自动注入所有 ToolExecutor 实现
     */
    @Autowired(required = false)
    public void setExecutors(List<ToolExecutor> executorList) {
        if (executorList != null) {
            for (ToolExecutor executor : executorList) {
                register(executor);
            }
        }
    }

    /**
     * 注册工具执行器
     */
    public void register(ToolExecutor executor) {
        try {
            Tool toolDef = executor.getToolDefinition();
            if (toolDef != null && toolDef.getName() != null) {
                executors.put(toolDef.getName().toLowerCase(), executor);
                logger.info("工具已注册：{} ({})", toolDef.getName(), toolDef.getCategory());
            }
        } catch (Exception e) {
            logger.error("注册工具失败：{}", executor.getName(), e);
        }
    }

    /**
     * 根据名称获取工具
     */
    public ToolExecutor getExecutor(String toolName) {
        return executors.get(toolName.toLowerCase());
    }

    /**
     * 执行工具
     */
    public ToolResult executeTool(String toolName, Map<String, Object> params) {
        ToolExecutor executor = getExecutor(toolName);
        if (executor == null) {
            return ToolResult.failure(toolName, "未找到工具：" + toolName);
        }

        long startTime = System.currentTimeMillis();
        try {
            logger.info("执行工具：{}, 参数：{}", toolName, params);
            ToolResult result = executor.execute(params);
            long endTime = System.currentTimeMillis();
            result.setExecutionTime(endTime - startTime);
            return result;
        } catch (Exception e) {
            logger.error("执行工具失败：{}", toolName, e);
            return ToolResult.failure(toolName, "执行失败：" + e.getMessage());
        }
    }

    /**
     * 获取所有可用工具
     */
    public List<Tool> getAllTools() {
        return executors.values().stream()
                .map(ToolExecutor::getToolDefinition)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 根据分类筛选工具
     */
    public List<Tool> getToolsByCategory(Tool.ToolCategory category) {
        return executors.values().stream()
                .map(ToolExecutor::getToolDefinition)
                .filter(t -> t != null && t.getCategory() == category)
                .collect(Collectors.toList());
    }

    /**
     * 搜索工具（根据描述）
     */
    public List<Tool> searchTools(String query) {
        String lowerQuery = query.toLowerCase();
        return executors.values().stream()
                .map(ToolExecutor::getToolDefinition)
                .filter(t -> t != null && (
                    t.getName().toLowerCase().contains(lowerQuery) ||
                    t.getDescription().toLowerCase().contains(lowerQuery)
                ))
                .collect(Collectors.toList());
    }

    /**
     * 获取工具数量
     */
    public int getToolCount() {
        return executors.size();
    }
}


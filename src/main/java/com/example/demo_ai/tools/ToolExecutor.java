package com.example.demo_ai.tools;

import com.example.demo_ai.model.Tool;
import com.example.demo_ai.model.ToolResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 工具执行器接口
 */
@Component
public interface ToolExecutor {

    Logger logger = LoggerFactory.getLogger(ToolExecutor.class);

    /**
     * 执行工具
     * @param params 工具参数
     * @return 执行结果
     */
    ToolResult execute(Map<String, Object> params);

    /**
     * 获取工具定义
     */
    Tool getToolDefinition();

    /**
     * 工具名称
     */
    String getName();
}


package com.example.demo_ai.model;

import java.util.Map;

/**
 * 工具执行结果
 */
public class ToolResult {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 结果数据
     */
    private Object data;

    /**
     * 错误信息（如果失败）
     */
    private String errorMessage;

    /**
     * 执行耗时（毫秒）
     */
    private long executionTime;

    /**
     * 工具名称
     */
    private String toolName;

    /**
     * 结果摘要（用于快速预览）
     */
    private String summary;

    /**
     * 附加信息
     */
    private Map<String, Object> metadata;

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public long getExecutionTime() { return executionTime; }
    public void setExecutionTime(long executionTime) { this.executionTime = executionTime; }

    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    // Helper methods
    public static ToolResult success(String toolName, Object data) {
        ToolResult result = new ToolResult();
        result.setSuccess(true);
        result.setToolName(toolName);
        result.setData(data);
        result.setSummary(data != null ? data.toString() : "执行成功");
        return result;
    }

    public static ToolResult failure(String toolName, String errorMessage) {
        ToolResult result = new ToolResult();
        result.setSuccess(false);
        result.setToolName(toolName);
        result.setErrorMessage(errorMessage);
        return result;
    }

    public Builder toBuilder() {
        Builder builder = new Builder();
        builder.success = this.success;
        builder.data = this.data;
        builder.errorMessage = this.errorMessage;
        builder.executionTime = this.executionTime;
        builder.toolName = this.toolName;
        builder.summary = this.summary;
        builder.metadata = this.metadata;
        return builder;
    }

    public static class Builder {
        private boolean success;
        private Object data;
        private String errorMessage;
        private long executionTime;
        private String toolName;
        private String summary;
        private Map<String, Object> metadata;

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder data(Object data) {
            this.data = data;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder executionTime(long executionTime) {
            this.executionTime = executionTime;
            return this;
        }

        public Builder toolName(String toolName) {
            this.toolName = toolName;
            return this;
        }

        public Builder summary(String summary) {
            this.summary = summary;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public ToolResult build() {
            ToolResult result = new ToolResult();
            result.success = this.success;
            result.data = this.data;
            result.errorMessage = this.errorMessage;
            result.executionTime = this.executionTime;
            result.toolName = this.toolName;
            result.summary = this.summary;
            result.metadata = this.metadata;
            return result;
        }
    }
}


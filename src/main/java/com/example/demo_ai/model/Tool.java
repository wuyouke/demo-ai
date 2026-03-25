package com.example.demo_ai.model;

import java.util.List;

/**
 * 工具定义 - 描述 AI 可以使用的工具
 */
public class Tool {

    /**
     * 工具名称（唯一标识）
     */
    private String name;

    /**
     * 工具描述（用于 AI 理解何时使用）
     */
    private String description;

    /**
     * 工具分类
     */
    private ToolCategory category;

    /**
     * 参数定义
     */
    private List<ParameterDefinition> parameters;

    /**
     * 返回值类型描述
     */
    private String returnType;

    /**
     * 使用示例
     */
    private String example;

    /**
     * 是否启用
     */
    private boolean enabled;

    /**
     * 工具分类枚举
     */
    public enum ToolCategory {
        INFORMATION("信息查询"),
        ANALYSIS("数据分析"),
        COMMUNICATION("通讯"),
        PRODUCTIVITY("生产力"),
        MEDIA("媒体处理"),
        SYSTEM("系统工具"),
        OTHER("其他");

        private final String chineseName;

        ToolCategory(String chineseName) {
            this.chineseName = chineseName;
        }

        public String getChineseName() {
            return chineseName;
        }
    }

    /**
     * 参数定义
     */
    public static class ParameterDefinition {
        /**
         * 参数名称
         */
        private String name;

        /**
         * 参数类型
         */
        private String type;

        /**
         * 是否必需
         */
        private boolean required;

        /**
         * 参数描述
         */
        private String description;

        /**
         * 默认值
         */
        private Object defaultValue;

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public boolean isRequired() { return required; }
        public void setRequired(boolean required) { this.required = required; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Object getDefaultValue() { return defaultValue; }
        public void setDefaultValue(Object defaultValue) { this.defaultValue = defaultValue; }

        // Builder
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String name;
            private String type;
            private boolean required;
            private String description;
            private Object defaultValue;

            public Builder name(String name) {
                this.name = name;
                return this;
            }

            public Builder type(String type) {
                this.type = type;
                return this;
            }

            public Builder required(boolean required) {
                this.required = required;
                return this;
            }

            public Builder description(String description) {
                this.description = description;
                return this;
            }

            public Builder defaultValue(Object defaultValue) {
                this.defaultValue = defaultValue;
                return this;
            }

            public ParameterDefinition build() {
                ParameterDefinition def = new ParameterDefinition();
                def.name = this.name;
                def.type = this.type;
                def.required = this.required;
                def.description = this.description;
                def.defaultValue = this.defaultValue;
                return def;
            }
        }
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ToolCategory getCategory() { return category; }
    public void setCategory(ToolCategory category) { this.category = category; }

    public List<ParameterDefinition> getParameters() { return parameters; }
    public void setParameters(List<ParameterDefinition> parameters) { this.parameters = parameters; }

    public String getReturnType() { return returnType; }
    public void setReturnType(String returnType) { this.returnType = returnType; }

    public String getExample() { return example; }
    public void setExample(String example) { this.example = example; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String description;
        private ToolCategory category;
        private List<ParameterDefinition> parameters;
        private String returnType;
        private String example;
        private boolean enabled;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder category(ToolCategory category) {
            this.category = category;
            return this;
        }

        public Builder parameters(List<ParameterDefinition> parameters) {
            this.parameters = parameters;
            return this;
        }

        public Builder returnType(String returnType) {
            this.returnType = returnType;
            return this;
        }

        public Builder example(String example) {
            this.example = example;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Tool build() {
            Tool tool = new Tool();
            tool.name = this.name;
            tool.description = this.description;
            tool.category = this.category;
            tool.parameters = this.parameters;
            tool.returnType = this.returnType;
            tool.example = this.example;
            tool.enabled = this.enabled;
            return tool;
        }
    }
}


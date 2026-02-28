package com.example.demo_ai.model;

/**
 * 提示词模板模型，包含角色信息和对应的系统提示词
 */
public class PromptTemplate {
    /**
     * 角色ID（英文标识）
     */
    private String id;

    /**
     * 角色名称（中文显示）
     */
    private String name;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 系统提示词（System Prompt）
     */
    private String systemPrompt;

    /**
     * 角色表情符号
     */
    private String emoji;

    // 构造函数
    public PromptTemplate() {
    }

    public PromptTemplate(String id, String name, String description, String systemPrompt, String emoji) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.systemPrompt = systemPrompt;
        this.emoji = emoji;
    }

    // Getter & Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    // Builder Pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String name;
        private String description;
        private String systemPrompt;
        private String emoji;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder systemPrompt(String systemPrompt) {
            this.systemPrompt = systemPrompt;
            return this;
        }

        public Builder emoji(String emoji) {
            this.emoji = emoji;
            return this;
        }

        public PromptTemplate build() {
            return new PromptTemplate(id, name, description, systemPrompt, emoji);
        }
    }
}


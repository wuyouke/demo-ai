package com.example.demo_ai.model;

/**
 * 对话请求模型
 */
public class ChatRequest {

    /**
     * 用户输入的消息
     */
    private String message;

    /**
     * 会话ID（用于保持上下文，可选）
     */
    private String sessionId;

    /**
     * 系统提示词（可选）
     */
    private String systemPrompt;

    public ChatRequest() {
    }

    public ChatRequest(String message) {
        this.message = message;
    }

    public ChatRequest(String message, String sessionId) {
        this.message = message;
        this.sessionId = sessionId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    @Override
    public String toString() {
        return "ChatRequest{" +
                "message='" + message + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", systemPrompt='" + systemPrompt + '\'' +
                '}';
    }
}


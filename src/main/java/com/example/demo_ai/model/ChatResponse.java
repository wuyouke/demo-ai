package com.example.demo_ai.model;

/**
 * 对话响应模型
 */
public class ChatResponse {

    /**
     * AI 的回复内容
     */
    private String response;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 错误信息（如果失败）
     */
    private String errorMessage;

    public ChatResponse() {
        this.success = true;
    }

    public ChatResponse(String response) {
        this.response = response;
        this.success = true;
    }

    public ChatResponse(String response, String sessionId) {
        this.response = response;
        this.sessionId = sessionId;
        this.success = true;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "ChatResponse{" +
                "response='" + response + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", success=" + success +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}


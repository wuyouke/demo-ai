package com.example.demo_ai.service;

import com.example.demo_ai.model.ChatRequest;
import com.example.demo_ai.model.ChatResponse;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * AI 对话服务
 */
@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    private final ChatLanguageModel chatLanguageModel;

    @Autowired
    public ChatService(ChatLanguageModel chatLanguageModel) {
        this.chatLanguageModel = chatLanguageModel;
    }

    /**
     * 简单对话（无上下文）
     */
    public ChatResponse chat(String message) {
        try {
            logger.info("收到用户消息: {}", message);
            String response = chatLanguageModel.generate(message);
            logger.info("AI 回复: {}", response);
            return new ChatResponse(response);
        } catch (Exception e) {
            logger.error("对话发生错误", e);
            ChatResponse errorResponse = new ChatResponse();
            errorResponse.setSuccess(false);
            errorResponse.setErrorMessage("对话失败: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * 带系统提示的对话
     */
    public ChatResponse chatWithSystemPrompt(String message, String systemPrompt) {
        try {
            logger.info("收到用户消息（系统提示: {}）: {}", systemPrompt, message);

            // 创建系统消息
            SystemMessage systemMessage = SystemMessage.from(systemPrompt);
            // 创建用户消息
            UserMessage userMessage = UserMessage.from(message);

            List<dev.langchain4j.data.message.ChatMessage> messages = new ArrayList<>();
            messages.add(systemMessage);
            messages.add(userMessage);

            dev.langchain4j.model.output.Response<AiMessage> responseObj = chatLanguageModel.generate(messages);
            String response = responseObj.content().text();
            logger.info("AI 回复: {}", response);

            return new ChatResponse(response);
        } catch (Exception e) {
            logger.error("对话发生错误", e);
            ChatResponse errorResponse = new ChatResponse();
            errorResponse.setSuccess(false);
            errorResponse.setErrorMessage("对话失败: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * 完整对话（支持自定义参数）
     */
    public ChatResponse chat(ChatRequest request) {
        try {
            String sessionId = request.getSessionId();
            if (sessionId == null || sessionId.isEmpty()) {
                sessionId = UUID.randomUUID().toString();
            }

            logger.info("会话ID: {}, 收到用户消息: {}", sessionId, request.getMessage());

            String response;
            if (request.getSystemPrompt() != null && !request.getSystemPrompt().isEmpty()) {
                response = chatWithSystemPrompt(request.getMessage(), request.getSystemPrompt()).getResponse();
            } else {
                response = chatLanguageModel.generate(request.getMessage());
            }

            logger.info("AI 回复: {}", response);

            ChatResponse chatResponse = new ChatResponse(response, sessionId);
            chatResponse.setSessionId(sessionId);

            return chatResponse;
        } catch (Exception e) {
            logger.error("对话发生错误", e);
            ChatResponse errorResponse = new ChatResponse();
            errorResponse.setSuccess(false);
            errorResponse.setErrorMessage("对话失败: " + e.getMessage());
            if (request.getSessionId() != null) {
                errorResponse.setSessionId(request.getSessionId());
            }
            return errorResponse;
        }
    }

    /**
     * 获取模型信息
     */
    public String getModelInfo() {
        if (chatLanguageModel instanceof OpenAiChatModel) {
            OpenAiChatModel model = (OpenAiChatModel) chatLanguageModel;
            return "OpenAI 模型: " + model.modelName();
        }
        return chatLanguageModel.getClass().getSimpleName();
    }
}


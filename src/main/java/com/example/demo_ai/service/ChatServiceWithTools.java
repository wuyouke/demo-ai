package com.example.demo_ai.service;

import com.example.demo_ai.model.ChatRequest;
import com.example.demo_ai.model.ChatResponse;
import com.example.demo_ai.tools.TouristAttractionToolManager;
import com.example.demo_ai.tools.WeatherToolManager;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.zhipu.ZhipuAiChatModel;
import dev.langchain4j.service.AiServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * AI 对话服务（支持 Function Calling/Tools）
 */
@Service
public class ChatServiceWithTools {

    private static final Logger logger = LoggerFactory.getLogger(ChatServiceWithTools.class);

    @Autowired
    private ChatLanguageModel chatLanguageModel;

    @Autowired
    private WeatherToolManager weatherToolManager;

    @Autowired
    private TouristAttractionToolManager touristAttractionToolManager;

    private AssistantWithTools assistant;

    /**
     * 初始化方法，在依赖注入完成后执行
     */
    @PostConstruct
    public void init() {
        // 创建支持 Tools 的 AI Assistant
        this.assistant = AiServices.builder(AssistantWithTools.class)
                .chatLanguageModel(chatLanguageModel)
                .tools(weatherToolManager, touristAttractionToolManager)
                .build();
        logger.info("ChatServiceWithTools 初始化完成，AI Assistant 已创建（集成了真实 API 天气和旅游景点工具）");
    }

    /**
     * 简单对话（无上下文，支持 Function Calling）
     */
    public ChatResponse chat(String message) {
        try {
            logger.info("收到用户消息: {}", message);
            String response = assistant.chat(message);
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
     * 带系统提示的对话（支持 Function Calling）
     */
    public ChatResponse chatWithSystemPrompt(String message, String systemPrompt) {
        try {
            logger.info("收到用户消息（系统提示: {}）: {}", systemPrompt, message);

            // 创建系统消息
            SystemMessage systemMessage = SystemMessage.from(systemPrompt);
            // 创建用户消息
            UserMessage userMessage = UserMessage.from(message);

            List<ChatMessage> messages = new ArrayList<>();
            messages.add(systemMessage);
            messages.add(userMessage);

            // 注意：使用 AiServices 构建的 assistant 不支持直接传递消息列表
            // 这里我们退回到使用基本的 ChatLanguageModel
            ChatResponse response = chatWithoutTools(messages);

            return response;
        } catch (Exception e) {
            logger.error("对话发生错误", e);
            ChatResponse errorResponse = new ChatResponse();
            errorResponse.setSuccess(false);
            errorResponse.setErrorMessage("对话失败: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * 完整对话（支持自定义参数和 Function Calling）
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
                response = assistant.chat(request.getMessage());
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
     * 不使用 Tools 的基本对话
     */
    private ChatResponse chatWithoutTools(List<ChatMessage> messages) {
        try {
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
     * 获取模型信息
     */
    public String getModelInfo() {
        if (chatLanguageModel instanceof OpenAiChatModel) {
            OpenAiChatModel model = (OpenAiChatModel) chatLanguageModel;
            return "OpenAI 模型: " + model.modelName();
        } else if (chatLanguageModel instanceof ZhipuAiChatModel) {
            ZhipuAiChatModel model = (ZhipuAiChatModel) chatLanguageModel;
            return "智谱 AI 模型";
        }
        return chatLanguageModel.getClass().getSimpleName();
    }

    /**
     * AI Assistant 接口
     */
    public interface AssistantWithTools {
        String chat(String userMessage);
    }
}


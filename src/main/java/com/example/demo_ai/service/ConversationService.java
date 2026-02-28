package com.example.demo_ai.service;

import com.example.demo_ai.model.ChatRequest;
import com.example.demo_ai.model.ChatResponse;
import com.example.demo_ai.tools.TouristAttractionToolManager;
import com.example.demo_ai.tools.WeatherToolManager;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 支持多轮对话和上下文保持的对话服务
 */
@Service
public class ConversationService {

    private static final Logger logger = LoggerFactory.getLogger(ConversationService.class);

    @Autowired
    private ChatLanguageModel chatLanguageModel;

    @Autowired
    private WeatherToolManager weatherToolManager;

    @Autowired
    private TouristAttractionToolManager touristAttractionToolManager;

    /**
     * 记忆助手接口（带记忆ID）
     */
    public interface MemoryAssistant {
        String chat(@MemoryId String memoryId, @UserMessage String userMessage);
    }

    private MemoryAssistant assistant;

    /**
     * 聊天记忆存储（按 memoryId 组织）
     */
    private final Map<Object, ChatMemory> memoryMap = new HashMap<>();

    /**
     * 会话到用户的映射（sessionId -> userId）
     */
    private final Map<String, String> sessionToUserMap = new HashMap<>();

    /**
     * 用户到会话的映射（userId -> List of sessionIds）
     */
    private final Map<String, List<String>> userToSessionsMap = new HashMap<>();

    /**
     * 最大历史消息数量
     */
    private static final int MAX_MESSAGES = 20;

    /**
     * 默认系统提示
     */
    private static final String DEFAULT_SYSTEM_PROMPT =
            "你是一个友好、专业的AI助手。你的职责是帮助用户查询天气、推荐旅游景点。\n" +
            "重要：请始终记住用户提供的个人信息，包括但不限于：\n" +
            "- 用户的姓名\n" +
            "- 用户想要去的地点/目的地\n" +
            "- 用户的其他偏好信息\n" +
            "在后续对话中，当用户提到相关话题时，要主动引用这些信息。\n" +
            "例如：如果用户说'我是谁'，你应该回答用户的名字和目的地（如果已知）。\n" +
            "始终保持对话的连贯性和上下文的完整性。";

    @PostConstruct
    public void init() {
        // 创建带记忆 Provider 的 Assistant
        this.assistant = AiServices.builder(MemoryAssistant.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemoryProvider(memoryId -> {
                    logger.debug("获取或创建记忆: {}", memoryId);
                    if (!memoryMap.containsKey(memoryId)) {
                        memoryMap.put(memoryId, MessageWindowChatMemory.builder()
                                .maxMessages(MAX_MESSAGES)
                                .build());
                    }
                    return memoryMap.get(memoryId);
                })
                .tools(weatherToolManager, touristAttractionToolManager)
                .build();

        logger.info("ConversationService 初始化完成，支持多轮对话和上下文保持");
    }

    /**
     * 发送消息（自定义系统提示和用户 ID）
     */
    public ChatResponse chat(String message, String sessionId, String userId, String systemPrompt) {
        try {
            // 如果会话 ID 为空，生成新的
            if (sessionId == null || sessionId.isEmpty()) {
                sessionId = UUID.randomUUID().toString();
                logger.info("创建新会话: {}", sessionId);
            }

            // 如果提供了 userId，关联会话到用户
            if (userId != null && !userId.isEmpty()) {
                // 检查是否已经存在这个会话的用户绑定
                if (!sessionToUserMap.containsKey(sessionId)) {
                    sessionToUserMap.put(sessionId, userId);

                    // 在用户的会话列表中添加
                    userToSessionsMap.computeIfAbsent(userId, k -> new ArrayList<>()).add(sessionId);
                    logger.info("会话 [{}] 已绑定到用户 [{}]", sessionId, userId);
                }
            }

            logger.info("会话 [{}]: {}", sessionId, message);

            // 使用 @MemoryId 作为标识符
            String response = assistant.chat(sessionId, message);

            logger.info("会话 [{}] AI 回复: {}", sessionId, response);

            return new ChatResponse(response, sessionId);

        } catch (Exception e) {
            logger.error("对话发生错误", e);
            ChatResponse errorResponse = new ChatResponse();
            errorResponse.setSuccess(false);
            errorResponse.setErrorMessage("对话失败: " + e.getMessage());
            if (sessionId != null) {
                errorResponse.setSessionId(sessionId);
            }
            return errorResponse;
        }
    }

    /**
     * 发送消息（使用会话 ID 进行记忆）
     */
    public ChatResponse chat(String message, String sessionId) {
        return chat(message, sessionId, null, DEFAULT_SYSTEM_PROMPT);
    }

    /**
     * 发送消息（自定义系统提示）
     */
    public ChatResponse chatWithSystemPrompt(String message, String sessionId, String systemPrompt) {
        return chat(message, sessionId, null, systemPrompt);
    }

    /**
     * 发送消息（绑定用户的会话）
     */
    public ChatResponse chatWithUser(String message, String sessionId, String userId) {
        return chat(message, sessionId, userId, DEFAULT_SYSTEM_PROMPT);
    }

    /**
     * 发送消息（使用 ChatRequest）
     */
    public ChatResponse chat(ChatRequest request) {
        String systemPrompt = request.getSystemPrompt();
        if (systemPrompt == null || systemPrompt.isEmpty()) {
            systemPrompt = DEFAULT_SYSTEM_PROMPT;
        }

        return chat(request.getMessage(), request.getSessionId(), null, systemPrompt);
    }

    /**
     * 清除指定会话的历史记录
     */
    public void clearHistory(String sessionId) {
        if (sessionId != null && !sessionId.isEmpty()) {
            memoryMap.remove(sessionId);

            // 移除会话到用户的映射
            String userId = sessionToUserMap.remove(sessionId);

            // 从用户的会话列表中移除
            if (userId != null) {
                List<String> userSessions = userToSessionsMap.get(userId);
                if (userSessions != null) {
                    userSessions.remove(sessionId);
                }
            }

            logger.info("已清除会话 [{}] 的历史记录", sessionId);
        }
    }

    /**
     * 获取会话的历史记录
     */
    public List<String> getHistory(String sessionId) {
        List<String> history = new ArrayList<>();
        // ChatMemory 不直接提供访问消息的方法
        // 这里只是为了保持接口兼容性
        return history;
    }

    /**
     * 获取所有活跃的会话 ID
     */
    public List<String> getActiveSessions() {
        List<String> sessionIds = new ArrayList<>();
        for (Object key : memoryMap.keySet()) {
            if (key instanceof String) {
                sessionIds.add((String) key);
            }
        }
        return sessionIds;
    }

    /**
     * 获取会话数量
     */
    public int getSessionCount() {
        return memoryMap.size();
    }

    /**
     * 获取指定用户的所有会话
     */
    public List<String> getUserSessions(String userId) {
        if (userId == null || userId.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> sessions = userToSessionsMap.get(userId);
        return sessions != null ? new ArrayList<>(sessions) : new ArrayList<>();
    }

    /**
     * 获取指定会话的用户 ID
     */
    public String getSessionUserId(String sessionId) {
        return sessionToUserMap.get(sessionId);
    }

    /**
     * 检查用户是否拥有指定会话
     */
    public boolean userOwnSession(String userId, String sessionId) {
        String owner = sessionToUserMap.get(sessionId);
        return owner != null && owner.equals(userId);
    }

    /**
     * 删除指定用户的所有会话
     */
    public void deleteUserAllSessions(String userId) {
        if (userId == null || userId.isEmpty()) {
            return;
        }

        List<String> sessions = userToSessionsMap.remove(userId);
        if (sessions != null) {
            for (String sessionId : sessions) {
                memoryMap.remove(sessionId);
                sessionToUserMap.remove(sessionId);
            }
            logger.info("已删除用户 [{}] 的所有 {} 个会话", userId, sessions.size());
        }
    }
}


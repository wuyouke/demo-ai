package com.example.demo_ai.service;

import com.example.demo_ai.model.ChatRequest;
import com.example.demo_ai.model.ChatResponse;
import com.example.demo_ai.tools.ImageToolManager;
import com.example.demo_ai.tools.TouristAttractionToolManager;
import com.example.demo_ai.tools.WeatherToolManager;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.TokenStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    @Qualifier("streamingChatLanguageModel")
    private StreamingChatLanguageModel streamingChatLanguageModel;

    @Autowired
    @Qualifier("visionStreamingChatLanguageModel")
    private StreamingChatLanguageModel visionStreamingChatLanguageModel;

    @Autowired
    private WeatherToolManager weatherToolManager;

    @Autowired
    private TouristAttractionToolManager touristAttractionToolManager;

    @Autowired
    private ImageToolManager imageToolManager;

    @Autowired
    private PersonaManager personaManager;

    /**
     * 记忆助手接口（带记忆ID）
     */
    public interface MemoryAssistant {
        String chat(@MemoryId String memoryId, @dev.langchain4j.service.UserMessage String userMessage);
    }

    /**
     * 流式记忆助手接口（支持动态系统提示）
     */
    @dev.langchain4j.service.SystemMessage("你是一个友好、专业的AI助手。你的职责是帮助用户查询天气、推荐旅游景点。")
    public interface StreamingMemoryAssistant {
        TokenStream chat(@MemoryId String memoryId, @dev.langchain4j.service.UserMessage String userMessage);
    }

    /**
     * 视觉流式记忆助手接口
     */
    @dev.langchain4j.service.SystemMessage("你是一个精确的图像分析AI助手。\n" +
            "【重要指示】\n" +
            "1. 当用户要求计数时（如'几个人'、'几只狗'、'多少人'等），必须:\n" +
            "   - 🔍 FIRST: 仔细逐一识别图像中的每一个对象，包括部分可见的对象\n" +
            "   - 📝 THEN: 明确列举出所有识别的对象及其位置\n" +
            "     例如: '我看到：左后位置的中年男性、中年女性、中间的女孩、左前的女孩、中间偏右的女孩、右侧的女孩、右前的女孩、右后的女孩、最右侧的女孩'\n" +
            "   - ✅ FINALLY: 给出准确的总数\n" +
            "   - 重点关注：所有头部可见或部分可见的人物都应该被计数\n" +
            "   - 注意：不要遗漏背景中或边缘的人物\n" +
            "\n" +
            "2. 描述图像内容时要详细准确，特别是人物数量\n" +
            "3. 记住用户提供的个人信息和历史上下文\n" +
            "4. 如果用户要求生成图片，请使用generateImage工具\n" +
            "5. 对于其他查询（天气、景点），使用相应工具帮助用户")
    public interface VisionStreamingMemoryAssistant {
        TokenStream chat(@MemoryId String memoryId, @dev.langchain4j.service.UserMessage String userMessage);
    }

    private MemoryAssistant assistant;
    private StreamingMemoryAssistant streamingAssistant;
    private VisionStreamingMemoryAssistant visionStreamingAssistant;

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
     * 存储每个会话当前的角色ID（sessionId -> personaId）
     */
    private final Map<String, String> sessionPersonaMap = new HashMap<>();

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

    /**
     * 视觉模型系统提示（用于图片分析任务）
     */
    private static final String VISION_SYSTEM_PROMPT =
            "你是一个精确的图像分析AI助手。\n" +
            "【重要指示】\n" +
            "1. 当用户要求计数（如'几个人'、'几只狗'）时，必须:\n" +
            "   - 仔细逐个识别和计数图像中的对象\n" +
            "   - 在回答前明确列举出所有识别的对象（如'我看到：父亲、母亲、孩子1、孩子2、孩子3，共5人'）\n" +
            "   - 确保计数准确无误后再给出最终答案\n" +
            "   - 如果有不确定的对象，要说明理由\n" +
            "\n" +
            "2. 描述图像内容时要详细准确\n" +
            "3. 记住用户提供的个人信息和历史上下文\n" +
            "4. 如果用户要求生成图片，请使用generateImage工具\n" +
            "5. 对于其他查询（天气、景点），使用相应工具帮助用户";

    @PostConstruct
    public void init() {
        // 创建带记忆 Provider 的 Assistant (阻塞式)
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
                .tools(weatherToolManager, touristAttractionToolManager, imageToolManager)
                .build();

        // 创建带记忆 Provider 的 Streaming Assistant (流式)
        this.streamingAssistant = AiServices.builder(StreamingMemoryAssistant.class)
                .streamingChatLanguageModel(streamingChatLanguageModel)
                .chatMemoryProvider(memoryId -> {
                    if (!memoryMap.containsKey(memoryId)) {
                        memoryMap.put(memoryId, MessageWindowChatMemory.builder()
                                .maxMessages(MAX_MESSAGES)
                                .build());
                    }
                    return memoryMap.get(memoryId);
                })
                .tools(weatherToolManager, touristAttractionToolManager, imageToolManager)
                .build();

        // 创建带记忆 Provider 的 Vision Streaming Assistant (视觉流式，支持工具)
        this.visionStreamingAssistant = AiServices.builder(VisionStreamingMemoryAssistant.class)
                .streamingChatLanguageModel(visionStreamingChatLanguageModel)
                .chatMemoryProvider(memoryId -> {
                    if (!memoryMap.containsKey(memoryId)) {
                        memoryMap.put(memoryId, MessageWindowChatMemory.builder()
                                .maxMessages(MAX_MESSAGES)
                                .build());
                    }
                    return memoryMap.get(memoryId);
                })
                .tools(weatherToolManager, touristAttractionToolManager, imageToolManager)
                .build();

        logger.info("ConversationService 初始化完成，支持多轮对话和上下文保持 (包含流式输出和视觉能力)");
    }

    /**
     * 流式发送消息
     */
    public TokenStream streamChat(String message, String sessionId, String userId, String imageBase64) {
        // 如果会话 ID 为空，生成新的
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
            logger.info("创建新流式会话: {}", sessionId);
        }

        // 如果提供了 userId，关联会话到用户
        if (userId != null && !userId.isEmpty()) {
            if (!sessionToUserMap.containsKey(sessionId)) {
                sessionToUserMap.put(sessionId, userId);
                userToSessionsMap.computeIfAbsent(userId, k -> new ArrayList<>()).add(sessionId);
                logger.info("流式会话 [{}] 已绑定到用户 [{}]", sessionId, userId);
            }
        }

        // 获取当前角色并注入其系统提示
        String currentPersonaId = personaManager.getCurrentPersonaId();
        com.example.demo_ai.model.PromptTemplate currentPersona = personaManager.getCurrentPersona();
        String systemPrompt = currentPersona != null ? currentPersona.getSystemPrompt() : DEFAULT_SYSTEM_PROMPT;

        // 确保会话的记忆已初始化
        ChatMemory memory = memoryMap.computeIfAbsent(sessionId, k ->
            MessageWindowChatMemory.builder().maxMessages(MAX_MESSAGES).build()
        );

        // 检查会话的角色是否改变（用户在同一会话中切换了角色）
        String previousPersonaId = sessionPersonaMap.getOrDefault(sessionId, currentPersonaId);
        if (!previousPersonaId.equals(currentPersonaId)) {
            logger.info("会话 [{}] 角色已从 [{}] 切换到 [{}]", sessionId, previousPersonaId, currentPersonaId);
            // 清空记忆并重新添加新的系统提示
            memoryMap.put(sessionId, MessageWindowChatMemory.builder().maxMessages(MAX_MESSAGES).build());
            memory = memoryMap.get(sessionId);
            memory.add(dev.langchain4j.data.message.SystemMessage.from(systemPrompt));
        } else if (memory.messages().isEmpty()) {
            // 新会话：将系统提示作为系统消息添加到记忆中
            logger.info("为会话 [{}] 注入角色 [{}] 的系统提示", sessionId, currentPersonaId);
            memory.add(dev.langchain4j.data.message.SystemMessage.from(systemPrompt));
        }

        // 记录当前会话的角色ID
        sessionPersonaMap.put(sessionId, currentPersonaId);

        logger.info("流式会话 [{}] (角色: {}): {}", sessionId, currentPersonaId, message);

        // 检查是否包含图片，或者是否在询问关于图片的问题
        boolean hasImage = imageBase64 != null && !imageBase64.isEmpty();
        boolean isAskingAboutImage = message != null && (
                message.contains("图片") || message.contains("图中") ||
                message.contains("这张图") || message.contains("看这") ||
                message.contains("狗") || message.contains("几只")
        );

        // 如果包含图片，或者可能在询问历史图片，使用视觉模型
        if (hasImage || isAskingAboutImage) {
            logger.info("使用视觉模型处理 (hasImage: {}, isAskingAboutImage: {})", hasImage, isAskingAboutImage);

            if (hasImage) {
                // 如果有图片，先将多模态消息添加到记忆中
                // 构建多模态消息并添加到记忆
                dev.langchain4j.data.message.UserMessage userMessage;
                if (imageBase64.startsWith("data:image")) {
                    String base64Data = imageBase64.substring(imageBase64.indexOf(",") + 1);
                    String mimeType = imageBase64.substring(5, imageBase64.indexOf(";"));
                    userMessage = dev.langchain4j.data.message.UserMessage.from(
                        TextContent.from(message),
                        ImageContent.from(base64Data, mimeType)
                    );
                } else {
                    userMessage = dev.langchain4j.data.message.UserMessage.from(
                        TextContent.from(message),
                        ImageContent.from(imageBase64, "image/jpeg")
                    );
                }
                memory.add(userMessage);

                // 然后通过视觉助手处理（它会看到记忆中的图片）
                return visionStreamingAssistant.chat(sessionId, message);
            } else {
                // 纯文本但查询图片内容，使用视觉助手
                return visionStreamingAssistant.chat(sessionId, message);
            }
        }

        // 纯文本对话，使用带工具的 Assistant
        return streamingAssistant.chat(sessionId, message);
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
            sessionPersonaMap.remove(sessionId);

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
                sessionPersonaMap.remove(sessionId);
                sessionToUserMap.remove(sessionId);
            }
            logger.info("已删除用户 [{}] 的所有 {} 个会话", userId, sessions.size());
        }
    }
}


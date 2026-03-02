package com.example.demo_ai.controller;

import com.example.demo_ai.model.ChatRequest;
import com.example.demo_ai.model.ChatResponse;
import com.example.demo_ai.model.PromptTemplate;
import com.example.demo_ai.model.StreamChatRequest;
import com.example.demo_ai.service.AuthService;
import com.example.demo_ai.service.ConversationService;
import com.example.demo_ai.service.PersonaManager;
import dev.langchain4j.service.TokenStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 多轮对话控制器
 * 支持上下文保持和会话记忆
 */
@RestController
@RequestMapping("/api/conversation")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ConversationController {

    private static final Logger logger = LoggerFactory.getLogger(ConversationController.class);

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private AuthService authService;

    @Autowired
    private PersonaManager personaManager;

    /**
     * 流式对话接口 (SSE)
     */
    @PostMapping(value = "/stream", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter streamChat(
            @RequestBody StreamChatRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        // 获取当前用户
        String userId = getCurrentUserId(authHeader);

        // 如果没有提供 sessionId，生成一个新的
        final String finalSessionId = (request.getSessionId() != null && !request.getSessionId().isEmpty()) ? request.getSessionId() : UUID.randomUUID().toString();

        // 创建 SseEmitter，设置超时时间为 5 分钟
        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L);

        try {
            // 发送初始事件，告诉前端 sessionId
            emitter.send(SseEmitter.event().name("session").data(finalSessionId));

            // 检查是否是图片生成任务
            String message = request.getMessage() != null ? request.getMessage() : "";
            boolean isImageTask = message.contains("生成") || message.contains("画") ||
                                 message.contains("图片") || message.contains("绘制");

            if (isImageTask) {
                // 为图片生成任务发送初始状态
                Map<String, String> statusData = new HashMap<>();
                statusData.put("status", "🎨 正在生成图片中，这可能需要 15-30 秒，请稍候...");
                try {
                    emitter.send(SseEmitter.event().name("status").data(statusData));
                } catch (IOException e) {
                    logger.debug("发送初始状态失败", e);
                }
            }

            // 设置选中的角色
            String personaId = request.getPersonaId() != null ? request.getPersonaId() : "assistant";
            if (!personaManager.hasPersona(personaId)) {
                personaId = "assistant";
            }
            personaManager.setCurrentPersona(personaId);

            // 调用服务获取 TokenStream
            TokenStream tokenStream = conversationService.streamChat(request.getMessage(), finalSessionId, userId, request.getImageBase64());

            // 处理流式输出
            tokenStream
                .onNext(token -> {
                    if (token == null || token.isEmpty()) {
                        return;
                    }
                    try {
                        // 将每个 token 包装成 JSON 对象发送，避免换行符被 SSE 格式化吞掉
                        Map<String, String> data = new HashMap<>();
                        data.put("text", token);
                        emitter.send(SseEmitter.event().name("message").data(data));
                    } catch (IOException e) {
                        logger.error("发送 SSE 数据失败", e);
                        emitter.completeWithError(e);
                    }
                })
                .onComplete(response -> {
                    try {
                        // 发送完成事件
                        emitter.send(SseEmitter.event().name("message").data("done"));
                        emitter.complete();
                    } catch (IOException e) {
                        logger.error("发送 SSE 完成事件失败", e);
                        emitter.completeWithError(e);
                    }
                })
                .onError(error -> {
                    logger.error("流式对话发生错误", error);
                    try {
                        emitter.send(SseEmitter.event().name("error").data(error.getMessage()));
                    } catch (IOException e) {
                        // ignore
                    }
                    emitter.completeWithError(error);
                })
                .start();

        } catch (Exception e) {
            logger.error("初始化流式对话失败", e);
            emitter.completeWithError(e);
        }

        return emitter;
    }

    /**
     * 提取 Authorization header 中的 Token
     */
    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * 获取当前用户 ID
     */
    private String getCurrentUserId(String authHeader) {
        String token = extractToken(authHeader);
        if (token != null && authService.validateToken(token)) {
            return authService.getUserIdFromToken(token);
        }
        return null;
    }

    /**
     * 发送消息（简单模式，自动管理会话 ID）
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestParam String message,
                                               @RequestParam(required = false) String sessionId,
                                               @RequestParam(required = false) String systemPrompt,
                                               @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // 获取当前用户
            String userId = getCurrentUserId(authHeader);

            // 如果有用户，绑定会话到用户
            if (userId != null) {
                ChatResponse response = conversationService.chat(message, sessionId, userId,
                        systemPrompt != null ? systemPrompt : "");
                return ResponseEntity.ok(response);
            } else {
                // 无用户认证，使用无用户模式
                ChatResponse response = conversationService.chat(message, sessionId);
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            logger.error("对话请求处理失败", e);
            ChatResponse errorResponse = new ChatResponse();
            errorResponse.setSuccess(false);
            errorResponse.setErrorMessage("处理失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 发送消息（完整模式，使用 JSON body）
     */
    @PostMapping("/chat/full")
    public ResponseEntity<ChatResponse> chatFull(@RequestBody ChatRequest request) {
        try {
            ChatResponse response = conversationService.chat(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("对话请求处理失败", e);
            ChatResponse errorResponse = new ChatResponse();
            errorResponse.setSuccess(false);
            errorResponse.setErrorMessage("处理失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 清除会话历史
     */
    @DeleteMapping("/history/{sessionId}")
    public ResponseEntity<Map<String, String>> clearHistory(@PathVariable String sessionId) {
        try {
            conversationService.clearHistory(sessionId);
            Map<String, String> result = new HashMap<>();
            result.put("message", "会话历史已清除");
            result.put("sessionId", sessionId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("清除会话历史失败", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "清除失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 获取会话历史
     */
    @GetMapping("/history/{sessionId}")
    public ResponseEntity<Map<String, Object>> getHistory(@PathVariable String sessionId) {
        try {
            List<String> history = conversationService.getHistory(sessionId);
            Map<String, Object> result = new HashMap<>();
            result.put("sessionId", sessionId);
            result.put("history", history);
            result.put("messageCount", history.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("获取会话历史失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 获取所有活跃会话
     */
    @GetMapping("/sessions")
    public ResponseEntity<Map<String, Object>> getSessions() {
        try {
            List<String> sessions = conversationService.getActiveSessions();
            Map<String, Object> result = new HashMap<>();
            result.put("sessionCount", sessions.size());
            result.put("sessions", sessions);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("获取活跃会话失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 获取当前用户的所有会话
     */
    @GetMapping("/my-sessions")
    public ResponseEntity<Map<String, Object>> getMySession(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            String userId = getCurrentUserId(authHeader);
            if (userId == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "未认证");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            List<String> sessions = conversationService.getUserSessions(userId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("userId", userId);
            result.put("sessionCount", sessions.size());
            result.put("sessions", sessions);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("获取用户会话失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "获取失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 删除用户的所有会话
     */
    @DeleteMapping("/my-sessions")
    public ResponseEntity<Map<String, Object>> deleteMySession(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            String userId = getCurrentUserId(authHeader);
            if (userId == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "未认证");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            conversationService.deleteUserAllSessions(userId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "所有会话已删除");
            result.put("userId", userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("删除用户会话失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "删除失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 获取所有可用的 AI 角色列表
     */
    @GetMapping("/personas")
    public ResponseEntity<Map<String, Object>> getPersonas() {
        try {
            List<PromptTemplate> personas = personaManager.getAllPersonas();
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("personaCount", personas.size());
            result.put("personas", personas);
            result.put("currentPersona", personaManager.getCurrentPersonaId());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("获取角色列表失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "获取失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 获取指定角色的详细信息
     */
    @GetMapping("/personas/{personaId}")
    public ResponseEntity<Map<String, Object>> getPersona(@PathVariable String personaId) {
        try {
            if (!personaManager.hasPersona(personaId)) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "角色不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            PromptTemplate persona = personaManager.getPersona(personaId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("persona", persona);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("获取角色信息失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "获取失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 设置当前角色
     */
    @PostMapping("/personas/{personaId}/select")
    public ResponseEntity<Map<String, Object>> selectPersona(@PathVariable String personaId) {
        try {
            if (!personaManager.hasPersona(personaId)) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "角色不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            personaManager.setCurrentPersona(personaId);
            PromptTemplate currentPersona = personaManager.getCurrentPersona();
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "角色已切换");
            result.put("currentPersona", currentPersona);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("切换角色失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "切换失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 启用或禁用会话的 RAG 功能
     */
    @PostMapping("/rag/{sessionId}/toggle")
    public ResponseEntity<Map<String, Object>> toggleRag(
            @PathVariable String sessionId,
            @RequestParam boolean enabled) {
        try {
            conversationService.setRagEnabled(sessionId, enabled);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "RAG 功能已" + (enabled ? "启用" : "禁用"));
            result.put("sessionId", sessionId);
            result.put("ragEnabled", enabled);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("RAG 切换失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "切换失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 获取会话的 RAG 状态
     */
    @GetMapping("/rag/{sessionId}/status")
    public ResponseEntity<Map<String, Object>> getRagStatus(@PathVariable String sessionId) {
        try {
            Map<String, Object> status = conversationService.getRagStatus(sessionId);
            status.put("success", true);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            logger.error("获取 RAG 状态失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "获取失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "ConversationService");
        health.put("activeSessions", conversationService.getSessionCount());

        // Java 8 兼容的 List 创建方式
        List<String> features = new ArrayList<>();
        features.add("多轮对话支持");
        features.add("上下文保持");
        features.add("会话管理");
        features.add("历史记录查询");
        features.add("天气查询工具");
        features.add("旅游景点工具");
        features.add("角色扮演与提示词工程");
        features.add("RAG 检索增强生成");
        features.add("文档管理");
        features.add("向量检索");
        health.put("features", features);

        return ResponseEntity.ok(health);
    }
}


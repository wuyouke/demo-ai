package com.example.demo_ai.controller;

import com.example.demo_ai.model.ChatRequest;
import com.example.demo_ai.model.ChatResponse;
import com.example.demo_ai.service.AuthService;
import com.example.demo_ai.service.ConversationService;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        health.put("features", features);

        return ResponseEntity.ok(health);
    }
}


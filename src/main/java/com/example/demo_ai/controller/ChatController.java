package com.example.demo_ai.controller;

import com.example.demo_ai.model.ChatRequest;
import com.example.demo_ai.model.ChatResponse;
import com.example.demo_ai.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AI 对话控制器
 */
@RestController
@RequestMapping(value = "/api/chat", produces = "application/json;charset=UTF-8")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private ChatService chatService;

    /**
     * 简单对话接口（GET）
     *
     * @param message 用户消息
     * @return AI 回复
     */
    @GetMapping
    public ResponseEntity<ChatResponse> simpleChat(@RequestParam String message) {
        if (message == null || message.trim().isEmpty()) {
            ChatResponse errorResponse = new ChatResponse();
            errorResponse.setSuccess(false);
            errorResponse.setErrorMessage("消息不能为空");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        ChatResponse response = chatService.chat(message);
        return ResponseEntity.ok(response);
    }

    /**
     * 完整对话接口（POST）
     *
     * @param request 对话请求（包含消息、会话ID、系统提示等）
     * @return AI 回复
     */
    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            ChatResponse errorResponse = new ChatResponse();
            errorResponse.setSuccess(false);
            errorResponse.setErrorMessage("消息不能为空");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        ChatResponse response = chatService.chat(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 带系统提示的对话接口（POST）
     *
     * @param message       用户消息
     * @param systemPrompt  系统提示词
     * @return AI 回复
     */
    @PostMapping("/with-prompt")
    public ResponseEntity<ChatResponse> chatWithPrompt(
            @RequestParam String message,
            @RequestParam(required = false) String systemPrompt) {

        if (message == null || message.trim().isEmpty()) {
            ChatResponse errorResponse = new ChatResponse();
            errorResponse.setSuccess(false);
            errorResponse.setErrorMessage("消息不能为空");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        String prompt = (systemPrompt != null && !systemPrompt.isEmpty())
                ? systemPrompt
                : "你是一个有帮助的AI助手。";

        ChatResponse response = chatService.chatWithSystemPrompt(message, prompt);
        return ResponseEntity.ok(response);
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("AI Chat Service is running!\nModel: " + chatService.getModelInfo());
    }
}


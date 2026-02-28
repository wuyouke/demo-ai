package com.example.demo_ai.controller;

import com.example.demo_ai.model.Attraction;
import com.example.demo_ai.model.ChatRequest;
import com.example.demo_ai.model.ChatResponse;
import com.example.demo_ai.service.ChatServiceWithTools;
import com.example.demo_ai.service.TouristAttractionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 旅游景点查询控制器
 * 提供旅游景点查询和智能对话（支持 Function Calling）的 API 接口
 */
@RestController
@RequestMapping("/api/attractions")
public class TouristAttractionController {

    private static final Logger logger = LoggerFactory.getLogger(TouristAttractionController.class);

    @Autowired
    private TouristAttractionService touristAttractionService;

    @Autowired
    private ChatServiceWithTools chatServiceWithTools;

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "旅游景点服务运行正常");
        response.put("service", "Tourist Attraction Service with Function Calling");
        return ResponseEntity.ok(response);
    }

    /**
     * 查询指定城市的旅游景点
     */
    @GetMapping("/query")
    public ResponseEntity<?> queryAttractions(@RequestParam String city) {
        try {
            if (city == null || city.trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "city 参数不能为空");
                return ResponseEntity.badRequest().body(error);
            }

            logger.info("查询旅游景点: {}", city);
            List<Attraction> attractions = touristAttractionService.getAttractions(city);
            return ResponseEntity.ok(attractions);
        } catch (Exception e) {
            logger.error("旅游景点查询失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "查询旅游景点失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 查询指定城市的推荐景点
     */
    @GetMapping("/recommended")
    public ResponseEntity<?> getRecommended(@RequestParam String city) {
        try {
            if (city == null || city.trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "city 参数不能为空");
                return ResponseEntity.badRequest().body(error);
            }

            logger.info("查询推荐景点: {}", city);
            List<Attraction> attractions = touristAttractionService.getRecommendedAttractions(city);
            return ResponseEntity.ok(attractions);
        } catch (Exception e) {
            logger.error("推荐景点查询失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "查询推荐景点失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 查询指定城市的免费景点
     */
    @GetMapping("/free")
    public ResponseEntity<?> getFreeAttractions(@RequestParam String city) {
        try {
            if (city == null || city.trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "city 参数不能为空");
                return ResponseEntity.badRequest().body(error);
            }

            logger.info("查询免费景点: {}", city);
            List<Attraction> attractions = touristAttractionService.getFreeAttractions(city);
            return ResponseEntity.ok(attractions);
        } catch (Exception e) {
            logger.error("免费景点查询失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "查询免费景点失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 智能对话（支持 Function Calling）
     * AI 会自动判断是否需要调用旅游景点查询工具
     */
    @PostMapping(value = "/chat", produces = "application/json;charset=UTF-8")
    public ResponseEntity<?> chatWithAttractions(@RequestBody ChatRequest request) {
        try {
            if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "message 不能为空");
                return ResponseEntity.badRequest().body(error);
            }

            logger.info("收到智能对话请求: {}", request.getMessage());
            ChatResponse response = chatServiceWithTools.chat(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("智能对话失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "对话失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 获取服务信息
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("service", "Tourist Attraction Chat Service");
        info.put("model", chatServiceWithTools.getModelInfo());
        info.put("features", new String[]{
            "旅游景点查询",
            "智能对话（自动调用景点工具）",
            "推荐景点查询",
            "免费景点查询",
            "多城市支持",
            "Function Calling 支持"
        });
        info.put("endpoints", new String[]{
            "GET /api/attractions/health - 健康检查",
            "GET /api/attractions/query?city=北京 - 查询景点",
            "GET /api/attractions/recommended?city=北京 - 查询推荐景点",
            "GET /api/attractions/free?city=北京 - 查询免费景点",
            "POST /api/attractions/chat - 智能对话（JSON）"
        });
        return ResponseEntity.ok(info);
    }
}


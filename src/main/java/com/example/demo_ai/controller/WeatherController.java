package com.example.demo_ai.controller;

import com.example.demo_ai.model.ChatRequest;
import com.example.demo_ai.model.ChatResponse;
import com.example.demo_ai.model.WeatherRequest;
import com.example.demo_ai.model.WeatherResponse;
import com.example.demo_ai.service.ChatServiceWithTools;
import com.example.demo_ai.service.MockWeatherService;
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
import java.util.Map;

/**
 * 天气查询控制器
 * 提供天气查询和智能对话（支持 Function Calling）的 API 接口
 */
@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private static final Logger logger = LoggerFactory.getLogger(WeatherController.class);

    @Autowired
    private MockWeatherService mockWeatherService;

    @Autowired
    private ChatServiceWithTools chatServiceWithTools;

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "天气服务运行正常");
        response.put("service", "Weather Service with Function Calling");
        return ResponseEntity.ok(response);
    }

    /**
     * 直接查询天气
     * GET 请求，使用查询参数
     */
    @GetMapping("/query")
    public ResponseEntity<?> queryWeather(@RequestParam String city) {
        try {
            if (city == null || city.trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "city 参数不能为空");
                return ResponseEntity.badRequest().body(error);
            }

            logger.info("查询天气: {}", city);
            WeatherResponse response = mockWeatherService.getWeather(city);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("天气查询失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "查询天气失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 直接查询天气
     * POST 请求，使用 JSON 请求体
     */
    @PostMapping(value = "/query", produces = "application/json;charset=UTF-8")
    public ResponseEntity<?> queryWeather(@RequestBody WeatherRequest request) {
        try {
            if (request.getCity() == null || request.getCity().trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "city 不能为空");
                return ResponseEntity.badRequest().body(error);
            }

            logger.info("查询天气: {}", request);
            WeatherResponse response = mockWeatherService.getWeather(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("天气查询失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "查询天气失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 智能对话（支持 Function Calling）
     * AI 会自动判断是否需要调用天气查询工具
     */
    @PostMapping(value = "/chat", produces = "application/json;charset=UTF-8")
    public ResponseEntity<?> chatWithWeather(@RequestBody ChatRequest request) {
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
     * 简化的智能对话接口
     * GET 请求，直接传递消息
     */
    @GetMapping("/chat")
    public ResponseEntity<?> chatWithWeather(@RequestParam String message) {
        try {
            if (message == null || message.trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "message 参数不能为空");
                return ResponseEntity.badRequest().body(error);
            }

            logger.info("收到智能对话请求: {}", message);
            ChatResponse response = chatServiceWithTools.chat(message);
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
        info.put("service", "Weather Chat Service");
        info.put("model", chatServiceWithTools.getModelInfo());
        info.put("features", new String[]{
            "实时天气查询",
            "智能对话（自动调用天气工具）",
            "多城市支持",
            "Function Calling 支持"
        });
        info.put("endpoints", new String[]{
            "GET /api/weather/health - 健康检查",
            "GET /api/weather/query?city=北京 - 查询天气",
            "POST /api/weather/query - 查询天气（JSON）",
            "GET /api/weather/chat?message=北京今天天气怎么样 - 智能对话",
            "POST /api/weather/chat - 智能对话（JSON）"
        });
        return ResponseEntity.ok(info);
    }
}


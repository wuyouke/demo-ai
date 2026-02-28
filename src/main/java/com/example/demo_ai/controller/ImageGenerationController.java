package com.example.demo_ai.controller;

import com.example.demo_ai.model.ImageGenerationRequest;
import com.example.demo_ai.model.ImageGenerationResponse;
import com.example.demo_ai.service.ImageGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 图像生成控制器
 * 提供文本生成图像的 API 接口
 */
@RestController
@RequestMapping("/api/image")
public class ImageGenerationController {

    @Autowired
    private ImageGenerationService imageGenerationService;

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "图像生成服务运行正常");
        response.put("service", "Image Generation Service");
        response.put("provider", "Zhipu AI CogView");
        return ResponseEntity.ok(response);
    }

    /**
     * 简单的图像生成接口
     * GET 请求，使用查询参数
     *
     * @param prompt 图像描述文本
     * @return 生成的图像 URL
     */
    @GetMapping("/generate")
    public ResponseEntity<?> generateImage(@RequestParam String prompt) {
        try {
            if (prompt == null || prompt.trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "prompt 参数不能为空");
                return ResponseEntity.badRequest().body(error);
            }

            ImageGenerationResponse response = imageGenerationService.generateImage(prompt);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            // API Key 未配置
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "生成图像时出错: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 完整的图像生成接口
     * POST 请求，使用 JSON 请求体
     *
     * @param request 图像生成请求
     * @return 生成的图像 URL
     */
    @PostMapping(value = "/generate", produces = "application/json;charset=UTF-8")
    public ResponseEntity<?> generateImage(@RequestBody ImageGenerationRequest request) {
        try {
            if (request.getPrompt() == null || request.getPrompt().trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "prompt 不能为空");
                return ResponseEntity.badRequest().body(error);
            }

            ImageGenerationResponse response = imageGenerationService.generateImage(request);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            // API Key 未配置
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "生成图像时出错: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 批量生成图像
     *
     * @param prompts 多个图像描述文本
     * @return 生成的图像 URL 列表
     */
    @PostMapping(value = "/batch", produces = "application/json;charset=UTF-8")
    public ResponseEntity<?> generateImages(@RequestBody List<String> prompts) {
        try {
            if (prompts == null || prompts.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "prompts 列表不能为空");
                return ResponseEntity.badRequest().body(error);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("total", prompts.size());
            response.put("results", new ArrayList<>());

            List<ImageGenerationResponse> results = new ArrayList<>();
            for (String prompt : prompts) {
                try {
                    ImageGenerationResponse imgResponse = imageGenerationService.generateImage(prompt);
                    results.add(imgResponse);
                } catch (Exception e) {
                    ImageGenerationResponse errorResponse = new ImageGenerationResponse();
                    errorResponse.setSuccess(false);
                    errorResponse.setErrorMessage(e.getMessage());
                    results.add(errorResponse);
                }
            }

            response.put("results", results);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "批量生成图像时出错: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 获取支持的图像尺寸
     */
    @GetMapping("/sizes")
    public ResponseEntity<Map<String, Object>> getSupportedSizes() {
        Map<String, Object> response = new HashMap<>();
        response.put("supported_sizes", Arrays.asList("1024x1024", "768x1024", "1024x768"));
        response.put("default", "1024x1024");
        response.put("message", "支持的图像尺寸");
        return ResponseEntity.ok(response);
    }
}


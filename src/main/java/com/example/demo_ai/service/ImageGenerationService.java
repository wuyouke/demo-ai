package com.example.demo_ai.service;

import com.example.demo_ai.model.ImageGenerationRequest;
import com.example.demo_ai.model.ImageGenerationResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 图像生成服务
 * 使用智谱 AI 的 CogView API 生成图像
 */
@Service
public class ImageGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(ImageGenerationService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ImageGenerationService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    private String getApiKey() {
        // 优先从环境变量读取
        String apiKey = System.getenv("ZHIPU_API_KEY");

        if (apiKey == null || apiKey.isEmpty() || apiKey.contains("your-zhipu-api-key-here")) {
            throw new IllegalStateException(
                "智谱 AI API Key 未配置！\n" +
                "方法 1：在终端设置环境变量后运行：export ZHIPU_API_KEY=your-api-key && mvn spring-boot:run\n" +
                "方法 2：在 IDEA 的 Run Configuration 中配置环境变量 ZHIPU_API_KEY"
            );
        }
        return apiKey;
    }

    /**
     * 生成图像
     *
     * @param request 图像生成请求
     * @return 生成的图像 URL 列表
     */
    public ImageGenerationResponse generateImage(ImageGenerationRequest request) {
        try {
            logger.info("收到图像生成请求: {}", request.getPrompt());

            // 智谱 AI CogView API 端点
            String url = "https://open.bigmodel.cn/api/paas/v4/images/generations";

            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + getApiKey());

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "cogview-3");
            requestBody.put("prompt", request.getPrompt());
            requestBody.put("size", request.getSize() != null ? request.getSize() : "1024x1024");
            requestBody.put("n", request.getN() != null ? request.getN() : 1);

            // 发送请求
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // 解析响应
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode dataNode = root.path("data");

                if (dataNode.isArray()) {
                    List<String> imageUrls = new ArrayList<>();
                    for (JsonNode item : dataNode) {
                        String imageUrl = item.path("url").asText();
                        imageUrls.add(imageUrl);
                    }

                    ImageGenerationResponse result = new ImageGenerationResponse(imageUrls, request.getPrompt());
                    logger.info("图像生成成功，生成 {} 张图片", imageUrls.size());
                    return result;
                } else {
                    logger.error("API 返回数据格式异常: {}", response.getBody());
                    return createErrorResponse("API 返回数据格式异常");
                }
            } else {
                logger.error("图像生成失败，状态码: {}, 响应: {}", response.getStatusCode(), response.getBody());
                return createErrorResponse("图像生成失败，HTTP 状态: " + response.getStatusCode());
            }

        } catch (Exception e) {
            logger.error("图像生成异常", e);
            return createErrorResponse("图像生成失败: " + e.getMessage());
        }
    }

    /**
     * 简单的图像生成方法
     */
    public ImageGenerationResponse generateImage(String prompt) {
        ImageGenerationRequest request = new ImageGenerationRequest(prompt);
        return generateImage(request);
    }

    /**
     * 创建错误响应
     */
    private ImageGenerationResponse createErrorResponse(String errorMessage) {
        ImageGenerationResponse response = new ImageGenerationResponse();
        response.setSuccess(false);
        response.setErrorMessage(errorMessage);
        return response;
    }
}


package com.example.demo_ai.service;

import com.example.demo_ai.model.WeatherRequest;
import com.example.demo_ai.model.WeatherResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 天气查询服务
 * 使用免费天气 API 获取天气信息
 */
@Service
public class WeatherService {

    private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);

    // 使用免费的心知天气 API（无需 API Key）
    private static final String WEATHER_API_URL = "https://api.seniverse.com/v1/weather/now.json";
    // 免费演示用的 API Key（实际项目中应该使用自己的 API Key）
    private static final String DEMO_API_KEY = "S6jzGnDdD1tjg3lD3";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public WeatherService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 查询实时天气
     *
     * @param request 天气查询请求
     * @return 天气响应
     */
    public WeatherResponse getWeather(WeatherRequest request) {
        try {
            logger.info("查询天气: {}", request);

            // 构建请求参数
            Map<String, String> params = new HashMap<>();
            params.put("key", getApiKey());
            params.put("location", buildLocationString(request));
            params.put("language", "zh-Hans"); // 简体中文
            params.put("unit", "c"); // 摄氏度

            // 构建请求 URL
            String url = buildUrlWithParams(WEATHER_API_URL, params);

            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    String.class
            );

            // 解析响应
            if (response.getStatusCode() == HttpStatus.OK) {
                return parseWeatherResponse(response.getBody(), request.getCity());
            } else {
                logger.error("天气查询失败，状态码: {}", response.getStatusCode());
                return createErrorResponse("天气查询失败: HTTP " + response.getStatusCode());
            }

        } catch (Exception e) {
            logger.error("天气查询异常", e);
            return createErrorResponse("天气查询异常: " + e.getMessage());
        }
    }

    /**
     * 查询实时天气（简化版本）
     */
    public WeatherResponse getWeather(String city) {
        return getWeather(new WeatherRequest(city));
    }

    /**
     * 解析天气 API 响应
     */
    private WeatherResponse parseWeatherResponse(String responseBody, String cityName) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            // 检查是否有错误
            JsonNode error = root.path("status_code");
            if (!error.isMissingNode() && !error.asText().equals("0")) {
                String errorMessage = root.path("status").asText();
                logger.warn("天气 API 返回错误: {}", errorMessage);
                return createErrorResponse("天气查询失败: " + errorMessage);
            }

            // 解析天气数据
            JsonNode results = root.path("results");
            if (results.isArray() && results.size() > 0) {
                JsonNode location = results.get(0).path("location");
                JsonNode now = results.get(0).path("now");

                WeatherResponse response = new WeatherResponse();
                response.setSuccess(true);
                response.setLocation(location.path("name").asText() + ", " + location.path("country").asText());
                response.setTemperature(now.path("temperature").asDouble());
                response.setWeather(now.path("text").asText());
                response.setWindDirection(now.path("wind_direction").asText());
                response.setWindSpeed(now.path("wind_speed").asDouble());
                response.setHumidity(now.path("humidity").asInt());
                response.setVisibility(now.path("visibility").asDouble());
                response.setPressure(now.path("pressure").asInt());
                response.setUpdateTime(now.path("last_update").asText());

                logger.info("天气查询成功: {}", response);
                return response;
            } else {
                return createErrorResponse("未找到城市天气信息");
            }

        } catch (Exception e) {
            logger.error("解析天气响应失败", e);
            return createErrorResponse("解析天气数据失败: " + e.getMessage());
        }
    }

    /**
     * 构建地点字符串
     */
    private String buildLocationString(WeatherRequest request) {
        StringBuilder location = new StringBuilder();

        if (request.getCity() != null && !request.getCity().isEmpty()) {
            location.append(request.getCity());
        }
        if (request.getProvince() != null && !request.getProvince().isEmpty()) {
            location.append(", ").append(request.getProvince());
        }
        if (request.getCountry() != null && !request.getCountry().isEmpty()) {
            location.append(", ").append(request.getCountry());
        }

        return location.toString();
    }

    /**
     * 构建 URL 查询参数
     */
    private String buildUrlWithParams(String baseUrl, Map<String, String> params) {
        StringBuilder url = new StringBuilder(baseUrl);
        url.append("?");

        List<String> paramList = new ArrayList<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            paramList.add(entry.getKey() + "=" + entry.getValue());
        }

        url.append(String.join("&", paramList));
        return url.toString();
    }

    /**
     * 获取 API Key
     */
    private String getApiKey() {
        // 优先从环境变量读取
        String apiKey = System.getenv("SENIVERSE_API_KEY");
        if (apiKey != null && !apiKey.isEmpty()) {
            return apiKey;
        }

        // 返回演示用的 API Key
        logger.warn("使用演示 API Key，建议注册自己的心知天气 API Key");
        return DEMO_API_KEY;
    }

    /**
     * 创建错误响应
     */
    private WeatherResponse createErrorResponse(String errorMessage) {
        WeatherResponse response = new WeatherResponse();
        response.setSuccess(false);
        response.setErrorMessage(errorMessage);
        return response;
    }
}


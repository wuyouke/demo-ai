package com.example.demo_ai.service;

import com.example.demo_ai.model.WeatherRequest;
import com.example.demo_ai.model.WeatherResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 真实天气查询服务
 * 使用和风天气 API 获取实时天气数据
 */
@Service
public class RealWeatherService {

    private static final Logger logger = LoggerFactory.getLogger(RealWeatherService.class);

    @Value("${weather.qweather-api-key:}")
    private String qweatherApiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public RealWeatherService() {
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
            logger.info("查询真实天气: {}", request);

            String apiKey = getQweatherApiKey();
            if (apiKey == null || apiKey.isEmpty()) {
                logger.warn("和风天气 API Key 未配置，使用模拟数据");
                return fallbackToMockData(request);
            }

            // 和风天气 API 端点
            // 第一步：获取城市 ID
            String cityId = getCityId(request.getCity(), apiKey);
            if (cityId == null) {
                logger.warn("未找到城市: {}", request.getCity());
                return createErrorResponse("未找到城市: " + request.getCity());
            }

            // 第二步：获取实时天气
            String url = "https://devapi.qweather.com/v7/weather/now?location=" + cityId + "&key=" + apiKey;

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return parseQweatherResponse(response.getBody(), request.getCity());
            } else {
                logger.error("天气查询失败，状态码: {}", response.getStatusCode());
                return createErrorResponse("天气查询失败: HTTP " + response.getStatusCode());
            }

        } catch (Exception e) {
            logger.error("天气查询异常", e);
            logger.warn("API 调用失败，使用模拟数据");
            return fallbackToMockData(request);
        }
    }

    /**
     * 查询实时天气（简化版本）
     */
    public WeatherResponse getWeather(String city) {
        return getWeather(new WeatherRequest(city));
    }

    /**
     * 获取城市 ID
     */
    private String getCityId(String cityName, String apiKey) {
        try {
            String url = "https://geoapi.qweather.com/v2/city/lookup?location=" + cityName + "&key=" + apiKey;

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode location = root.path("location");

                if (location.isArray() && location.size() > 0) {
                    String cityId = location.get(0).path("id").asText();
                    String name = location.get(0).path("name").asText();
                    logger.info("找到城市: {} ({})", name, cityId);
                    return cityId;
                }
            }
        } catch (Exception e) {
            logger.error("获取城市 ID 失败", e);
        }
        return null;
    }

    /**
     * 解析和风天气响应
     */
    private WeatherResponse parseQweatherResponse(String responseBody, String cityName) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            // 检查状态码
            String code = root.path("code").asText();
            if (!"200".equals(code)) {
                String message = root.path("code").asText();
                logger.warn("天气 API 返回错误: {}", message);
                return createErrorResponse("天气查询失败: " + message);
            }

            JsonNode now = root.path("now");

            WeatherResponse response = new WeatherResponse();
            response.setSuccess(true);
            response.setLocation(cityName);
            response.setTemperature(Double.parseDouble(now.path("temp").asText()));
            response.setWeather(now.path("text").asText());
            response.setHumidity(Integer.parseInt(now.path("humidity").asText()));
            response.setWindSpeed(Double.parseDouble(now.path("windSpeed").asText()));
            response.setWindDirection(now.path("windDir").asText());
            response.setPressure(Integer.parseInt(now.path("pressure").asText()));
            response.setVisibility(Double.parseDouble(now.path("vis").asText()));

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            response.setUpdateTime(sdf.format(new Date()));

            logger.info("天气查询成功: {}", response);
            return response;

        } catch (Exception e) {
            logger.error("解析天气响应失败", e);
            return createErrorResponse("解析天气数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取和风天气 API Key
     */
    private String getQweatherApiKey() {
        if (qweatherApiKey != null && !qweatherApiKey.isEmpty()) {
            return qweatherApiKey;
        }

        // 从环境变量读取
        return System.getenv("QWEATHER_API_KEY");
    }

    /**
     * 降级到模拟数据
     */
    private WeatherResponse fallbackToMockData(WeatherRequest request) {
        logger.info("使用模拟天气数据");
        MockWeatherService mockService = new MockWeatherService();
        return mockService.getWeather(request);
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


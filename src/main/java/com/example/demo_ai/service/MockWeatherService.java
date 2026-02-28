package com.example.demo_ai.service;

import com.example.demo_ai.model.WeatherRequest;
import com.example.demo_ai.model.WeatherResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 模拟天气服务
 * 用于演示 Function Calling 功能
 */
@Service
public class MockWeatherService {

    private static final Logger logger = LoggerFactory.getLogger(MockWeatherService.class);

    // 模拟的城市天气数据
    private static final Map<String, WeatherData> MOCK_DATA = new HashMap<>();

    static {
        MOCK_DATA.put("北京", new WeatherData("北京", 15.5, "晴", 45, 12.0, "西北风", 1013, 10.0, 85));
        MOCK_DATA.put("上海", new WeatherData("上海", 18.2, "多云", 65, 8.5, "东南风", 1015, 8.0, 75));
        MOCK_DATA.put("广州", new WeatherData("广州", 25.3, "小雨", 80, 6.0, "南风", 1008, 5.0, 95));
        MOCK_DATA.put("深圳", new WeatherData("深圳", 24.8, "阴", 75, 7.5, "西南风", 1010, 6.5, 90));
        MOCK_DATA.put("成都", new WeatherData("成都", 16.8, "多云", 60, 5.0, "东风", 1012, 9.0, 80));
        MOCK_DATA.put("杭州", new WeatherData("杭州", 17.5, "晴", 55, 9.0, "东北风", 1014, 9.5, 78));
        MOCK_DATA.put("Beijing", new WeatherData("北京", 15.5, "Sunny", 45, 12.0, "Northwest", 1013, 10.0, 85));
        MOCK_DATA.put("Shanghai", new WeatherData("上海", 18.2, "Cloudy", 65, 8.5, "Southeast", 1015, 8.0, 75));
        MOCK_DATA.put("Guangzhou", new WeatherData("广州", 25.3, "Light Rain", 80, 6.0, "South", 1008, 5.0, 95));
        MOCK_DATA.put("Shenzhen", new WeatherData("深圳", 24.8, "Overcast", 75, 7.5, "Southwest", 1010, 6.5, 90));
        MOCK_DATA.put("Chengdu", new WeatherData("成都", 16.8, "Cloudy", 60, 5.0, "East", 1012, 9.0, 80));
        MOCK_DATA.put("Hangzhou", new WeatherData("杭州", 17.5, "Sunny", 55, 9.0, "Northeast", 1014, 9.5, 78));
    }

    /**
     * 查询模拟天气
     */
    public WeatherResponse getWeather(WeatherRequest request) {
        try {
            String city = request.getCity();
            logger.info("查询模拟天气: {}", city);

            WeatherData data = MOCK_DATA.get(city);

            if (data == null) {
                // 如果城市不存在，生成随机数据
                logger.warn("城市 {} 不在模拟数据中，生成随机数据", city);
                data = generateRandomWeather(city);
            }

            WeatherResponse response = new WeatherResponse();
            response.setSuccess(true);
            response.setLocation(data.city);
            response.setTemperature(data.temperature);
            response.setWeather(data.weather);
            response.setHumidity(data.humidity);
            response.setWindSpeed(data.windSpeed);
            response.setWindDirection(data.windDirection);
            response.setPressure(data.pressure);
            response.setVisibility(data.visibility);
            response.setAqi(data.aqi);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            response.setUpdateTime(sdf.format(new Date()));

            logger.info("天气查询成功: {}", response);
            return response;

        } catch (Exception e) {
            logger.error("天气查询异常", e);
            return createErrorResponse("天气查询异常: " + e.getMessage());
        }
    }

    /**
     * 查询模拟天气（简化版本）
     */
    public WeatherResponse getWeather(String city) {
        return getWeather(new WeatherRequest(city));
    }

    /**
     * 生成随机天气数据
     */
    private WeatherData generateRandomWeather(String city) {
        Random random = new Random();

        String[] weatherTypes = {"晴", "多云", "阴", "小雨", "中雨", "大雨", "雪"};
        String[] windDirections = {"东", "南", "西", "北", "东北", "东南", "西南", "西北"};

        double temperature = 10 + random.nextDouble() * 20; // 10-30 度
        int humidity = 30 + random.nextInt(50); // 30-80%
        double windSpeed = 2 + random.nextDouble() * 15; // 2-17 km/h
        String weather = weatherTypes[random.nextInt(weatherTypes.length)];
        String windDirection = windDirections[random.nextInt(windDirections.length)] + "风";

        return new WeatherData(
            city,
            Math.round(temperature * 10.0) / 10.0,
            weather,
            humidity,
            Math.round(windSpeed * 10.0) / 10.0,
            windDirection,
            1005 + random.nextInt(20), // 1005-1025 hPa
            5 + random.nextDouble() * 10, // 5-15 km
            40 + random.nextInt(60) // 40-100 AQI
        );
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

    /**
     * 天气数据内部类
     */
    private static class WeatherData {
        String city;
        double temperature;
        String weather;
        int humidity;
        double windSpeed;
        String windDirection;
        int pressure;
        double visibility;
        int aqi;

        WeatherData(String city, double temperature, String weather, int humidity,
                   double windSpeed, String windDirection, int pressure, double visibility, int aqi) {
            this.city = city;
            this.temperature = temperature;
            this.weather = weather;
            this.humidity = humidity;
            this.windSpeed = windSpeed;
            this.windDirection = windDirection;
            this.pressure = pressure;
            this.visibility = visibility;
            this.aqi = aqi;
        }
    }
}


package com.example.demo_ai.tools;

import com.example.demo_ai.model.WeatherResponse;
import com.example.demo_ai.service.RealWeatherService;
import dev.langchain4j.agent.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 天气查询工具管理器
 * 优先使用真实 API，失败时降级到模拟数据
 */
@Component
public class WeatherToolManager {

    private static final Logger logger = LoggerFactory.getLogger(WeatherToolManager.class);

    @Autowired
    private RealWeatherService realWeatherService;

    /**
     * 查询指定城市的实时天气
     */
    @Tool("查询指定城市的实时天气信息，包括温度、天气状况、湿度、风速等数据")
    public String getCurrentWeather(String city) {
        try {
            logger.info("AI 调用天气查询工具，城市: {}", city);

            WeatherResponse response = realWeatherService.getWeather(city);

            if (response.isSuccess()) {
                // 格式化天气信息，便于 AI 理解和呈现
                StringBuilder weatherInfo = new StringBuilder();
                weatherInfo.append("【").append(response.getLocation()).append("】\n");
                weatherInfo.append("温度: ").append(response.getTemperature()).append("°C\n");
                weatherInfo.append("天气: ").append(response.getWeather()).append("\n");
                weatherInfo.append("湿度: ").append(response.getHumidity()).append("%\n");
                weatherInfo.append("风速: ").append(response.getWindSpeed()).append(" km/h\n");
                weatherInfo.append("风向: ").append(response.getWindDirection()).append("\n");
                weatherInfo.append("气压: ").append(response.getPressure()).append(" hPa\n");
                weatherInfo.append("能见度: ").append(response.getVisibility()).append(" km\n");
                if (response.getAqi() != null) {
                    weatherInfo.append("空气质量指数: ").append(response.getAqi()).append("\n");
                }
                weatherInfo.append("更新时间: ").append(response.getUpdateTime());
                weatherInfo.append("\n\n注: 以上数据来自和风天气 API");

                return weatherInfo.toString();
            } else {
                logger.warn("天气查询失败: {}", response.getErrorMessage());
                return "无法获取 " + city + " 的天气信息：" + response.getErrorMessage();
            }

        } catch (Exception e) {
            logger.error("天气查询工具执行失败", e);
            return "查询天气时出错：" + e.getMessage();
        }
    }

    /**
     * 查询指定城市的温度
     */
    @Tool("查询指定城市的当前温度")
    public String getTemperature(String city) {
        try {
            logger.info("AI 调用温度查询工具，城市: {}", city);

            WeatherResponse response = realWeatherService.getWeather(city);

            if (response.isSuccess()) {
                String source = response.getUpdateTime().contains("Mock") ? "（模拟数据）" : "（和风天气 API）";
                return city + " 的当前温度是 " + response.getTemperature() + " 摄氏度 " + source;
            } else {
                return "无法获取 " + city + " 的温度信息：" + response.getErrorMessage();
            }

        } catch (Exception e) {
            logger.error("温度查询工具执行失败", e);
            return "查询温度时出错：" + e.getMessage();
        }
    }

    /**
     * 查询指定城市的天气状况
     */
    @Tool("查询指定城市的天气状况，如晴、多云、雨等")
    public String getWeatherCondition(String city) {
        try {
            logger.info("AI 调用天气状况查询工具，城市: {}", city);

            WeatherResponse response = realWeatherService.getWeather(city);

            if (response.isSuccess()) {
                String source = response.getUpdateTime().contains("Mock") ? "（模拟数据）" : "（和风天气 API）";
                return city + " 的天气状况是：" + response.getWeather() + " " + source;
            } else {
                return "无法获取 " + city + " 的天气状况：" + response.getErrorMessage();
            }

        } catch (Exception e) {
            logger.error("天气状况查询工具执行失败", e);
            return "查询天气状况时出错：" + e.getMessage();
        }
    }
}


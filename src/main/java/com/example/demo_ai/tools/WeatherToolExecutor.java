package com.example.demo_ai.tools;

import com.example.demo_ai.model.Tool;
import com.example.demo_ai.model.ToolResult;
import com.example.demo_ai.service.RealWeatherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;

/**
 * 天气查询工具执行器 - 适配到新的工具注册系统
 */
@Component
public class WeatherToolExecutor implements ToolExecutor {

    private static final Logger logger = LoggerFactory.getLogger(WeatherToolExecutor.class);

    @Autowired
    private RealWeatherService realWeatherService;

    @Override
    public ToolResult execute(Map<String, Object> params) {
        long startTime = System.currentTimeMillis();

        try {
            String city = (String) params.get("city");
            if (city == null || city.trim().isEmpty()) {
                return ToolResult.failure(getName(), "城市名称不能为空");
            }

            // 调用现有的 WeatherToolManager 逻辑
            String result = queryWeather(city);

            long endTime = System.currentTimeMillis();
            return ToolResult.success(getName(), result)
                    .toBuilder()
                    .executionTime(endTime - startTime)
                    .summary("查询 " + city + " 的天气")
                    .build();

        } catch (Exception e) {
            logger.error("天气查询工具执行失败", e);
            return ToolResult.failure(getName(), "执行失败：" + e.getMessage());
        }
    }

    /**
     * 查询天气（复用现有逻辑）
     */
    private String queryWeather(String city) {
        logger.info("查询天气，城市：{}", city);

        try {
            com.example.demo_ai.model.WeatherResponse response =
                realWeatherService.getWeather(city);

            if (response.isSuccess()) {
                StringBuilder weatherInfo = new StringBuilder();
                weatherInfo.append("【").append(response.getLocation()).append("】\n");
                weatherInfo.append("温度：").append(response.getTemperature()).append("°C\n");
                weatherInfo.append("天气：").append(response.getWeather()).append("\n");
                weatherInfo.append("湿度：").append(response.getHumidity()).append("%\n");
                weatherInfo.append("风速：").append(response.getWindSpeed()).append(" km/h\n");
                weatherInfo.append("风向：").append(response.getWindDirection()).append("\n");
                weatherInfo.append("气压：").append(response.getPressure()).append(" hPa\n");
                weatherInfo.append("能见度：").append(response.getVisibility()).append(" km\n");
                if (response.getAqi() != null) {
                    weatherInfo.append("空气质量指数：").append(response.getAqi()).append("\n");
                }
                weatherInfo.append("更新时间：").append(response.getUpdateTime());
                weatherInfo.append("\n\n注：以上数据来自和风天气 API");

                return weatherInfo.toString();
            } else {
                return "无法获取 " + city + " 的天气信息：" + response.getErrorMessage();
            }
        } catch (Exception e) {
            return "查询天气时出错：" + e.getMessage();
        }
    }

    @Override
    public Tool getToolDefinition() {
        return Tool.builder()
                .name(getName())
                .description("查询指定城市的实时天气信息，包括温度、天气状况、湿度、风速、空气质量等")
                .category(Tool.ToolCategory.INFORMATION)
                .parameters(Arrays.asList(
                        Tool.ParameterDefinition.builder()
                                .name("city")
                                .type("String")
                                .required(true)
                                .description("城市名称，如'北京'、'上海'")
                                .build()
                ))
                .returnType("String")
                .example("查询北京的天气 → {\"city\": \"北京\"}")
                .enabled(true)
                .build();
    }

    @Override
    public String getName() {
        return "weather";
    }
}


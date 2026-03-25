package com.example.demo_ai.tools;

import com.example.demo_ai.model.Tool;
import com.example.demo_ai.model.ToolResult;
import com.example.demo_ai.service.RealTouristAttractionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 旅游景点查询工具执行器 - 适配到新的工具注册系统
 */
@Component
public class TouristAttractionToolExecutor implements ToolExecutor {

    private static final Logger logger = LoggerFactory.getLogger(TouristAttractionToolExecutor.class);

    @Autowired
    private RealTouristAttractionService realTouristAttractionService;

    @Override
    public ToolResult execute(Map<String, Object> params) {
        long startTime = System.currentTimeMillis();

        try {
            String city = (String) params.get("city");
            if (city == null || city.trim().isEmpty()) {
                return ToolResult.failure(getName(), "城市名称不能为空");
            }

            String action = (String) params.get("action");
            String result;

            // 处理 null action，默认查询所有景点
            if (action == null || action.isEmpty()) {
                result = queryAttractions(city);
            } else {
                switch (action) {
                    case "free":
                        result = queryFreeAttractions(city);
                        break;
                    case "recommended":
                        result = queryRecommendedAttractions(city);
                        break;
                    case "type":
                        String type = (String) params.get("type");
                        result = queryAttractionsByType(city, type);
                        break;
                    default:
                        result = queryAttractions(city);
                }
            }

            long endTime = System.currentTimeMillis();
            return ToolResult.success(getName(), result)
                    .toBuilder()
                    .executionTime(endTime - startTime)
                    .summary("查询 " + city + " 的景点信息")
                    .build();

        } catch (Exception e) {
            logger.error("景点查询工具执行失败", e);
            return ToolResult.failure(getName(), "执行失败：" + e.getMessage());
        }
    }

    private String queryAttractions(String city) {
        logger.info("查询景点，城市：{}", city);

        try {
            List<com.example.demo_ai.model.Attraction> attractions =
                realTouristAttractionService.getAttractions(city);

            if (attractions.isEmpty()) {
                return "抱歉，暂时没有找到 " + city + " 的旅游景点信息。";
            }

            StringBuilder result = new StringBuilder();
            result.append("【").append(city).append("】推荐旅游景点：\n\n");

            for (int i = 0; i < attractions.size(); i++) {
                com.example.demo_ai.model.Attraction attraction = attractions.get(i);
                result.append(i + 1).append(". ").append(attraction.getName()).append("\n");
                result.append("   类型：").append(attraction.getType()).append("\n");
                if (attraction.getDescription() != null) {
                    result.append("   描述：").append(attraction.getDescription()).append("\n");
                }
                result.append("   游玩时长：").append(attraction.getDuration()).append(" 小时\n");
                result.append("   门票：").append(attraction.getPrice() == null || attraction.getPrice() == 0 ? "免费" : attraction.getPrice() + " 元").append("\n");
                if (attraction.getRating() != null) {
                    result.append("   评分：").append(attraction.getRating()).append("/5.0\n");
                }
                result.append("\n");
            }

            return result.toString();
        } catch (Exception e) {
            return "查询景点时出错：" + e.getMessage();
        }
    }

    private String queryFreeAttractions(String city) {
        logger.info("查询免费景点，城市：{}", city);

        try {
            List<com.example.demo_ai.model.Attraction> freeAttractions =
                realTouristAttractionService.getFreeAttractions(city);

            if (freeAttractions.isEmpty()) {
                return "抱歉，" + city + " 暂时没有找到免费景点信息。";
            }

            StringBuilder result = new StringBuilder();
            result.append("【").append(city).append("】免费景点推荐：\n\n");

            for (int i = 0; i < freeAttractions.size(); i++) {
                com.example.demo_ai.model.Attraction attraction = freeAttractions.get(i);
                result.append(i + 1).append(". ").append(attraction.getName()).append("\n");
                result.append("   类型：").append(attraction.getType()).append("\n");
                if (attraction.getDescription() != null) {
                    result.append("   描述：").append(attraction.getDescription()).append("\n");
                }
                result.append("   游玩时长：").append(attraction.getDuration()).append(" 小时\n");
                if (attraction.getRating() != null) {
                    result.append("   评分：").append(attraction.getRating()).append("/5.0\n");
                }
                result.append("\n");
            }

            return result.toString();
        } catch (Exception e) {
            return "查询免费景点时出错：" + e.getMessage();
        }
    }

    private String queryRecommendedAttractions(String city) {
        logger.info("查询推荐景点，城市：{}", city);

        try {
            List<com.example.demo_ai.model.Attraction> recommended =
                realTouristAttractionService.getRecommendedAttractions(city);

            if (recommended.isEmpty()) {
                return "抱歉，暂时没有找到 " + city + " 的推荐景点信息。";
            }

            StringBuilder result = new StringBuilder();
            result.append("【").append(city).append("】强烈推荐的 3 个景点：\n\n");

            for (int i = 0; i < recommended.size(); i++) {
                com.example.demo_ai.model.Attraction attraction = recommended.get(i);
                result.append("★ ").append(attraction.getName()).append("\n");
                if (attraction.getRating() != null) {
                    result.append("  评分：").append(attraction.getRating()).append("/5.0");
                }
                result.append("  |  门票：").append(attraction.getPrice() == null || attraction.getPrice() == 0 ? "免费" : attraction.getPrice() + " 元").append("\n");
                if (attraction.getDescription() != null) {
                    result.append("  描述：").append(attraction.getDescription()).append("\n");
                }
                result.append("\n");
            }

            return result.toString();
        } catch (Exception e) {
            return "查询推荐景点时出错：" + e.getMessage();
        }
    }

    private String queryAttractionsByType(String city, String type) {
        logger.info("查询指定类型景点，城市：{}, 类型：{}", city, type);

        try {
            List<com.example.demo_ai.model.Attraction> attractions =
                realTouristAttractionService.getAttractions(city, type, 5);

            if (attractions.isEmpty()) {
                return "抱歉，" + city + " 暂时没有找到 " + type + " 类型的景点。";
            }

            StringBuilder result = new StringBuilder();
            result.append("【").append(city).append("】").append(type).append("类型景点：\n\n");

            for (int i = 0; i < attractions.size(); i++) {
                com.example.demo_ai.model.Attraction attraction = attractions.get(i);
                result.append(i + 1).append(". ").append(attraction.getName()).append("\n");
                if (attraction.getDescription() != null) {
                    result.append("   描述：").append(attraction.getDescription()).append("\n");
                }
                result.append("   门票：").append(attraction.getPrice() == null || attraction.getPrice() == 0 ? "免费" : attraction.getPrice() + " 元").append("\n");
                if (attraction.getRating() != null) {
                    result.append("   评分：").append(attraction.getRating()).append("/5.0\n");
                }
                result.append("\n");
            }

            return result.toString();
        } catch (Exception e) {
            return "查询类型景点时出错：" + e.getMessage();
        }
    }

    @Override
    public Tool getToolDefinition() {
        return Tool.builder()
                .name(getName())
                .description("查询指定城市的旅游景点信息，支持查询所有景点、免费景点、推荐景点、按类型查询等")
                .category(Tool.ToolCategory.INFORMATION)
                .parameters(Arrays.asList(
                        Tool.ParameterDefinition.builder()
                                .name("city")
                                .type("String")
                                .required(true)
                                .description("城市名称，如'北京'、'上海'")
                                .build(),
                        Tool.ParameterDefinition.builder()
                                .name("action")
                                .type("String")
                                .required(false)
                                .description("查询类型：all(全部), free(免费), recommended(推荐), type(按类型)")
                                .defaultValue("all")
                                .build(),
                        Tool.ParameterDefinition.builder()
                                .name("type")
                                .type("String")
                                .required(false)
                                .description("景点类型（当 action 为 type 时需要），如'公园'、'博物馆'、'古迹'")
                                .build()
                ))
                .returnType("String")
                .example("查询北京的景点 → {\"city\": \"北京\"}\n查询免费景点 → {\"city\": \"北京\", \"action\": \"free\"}")
                .enabled(true)
                .build();
    }

    @Override
    public String getName() {
        return "tourist_attraction";
    }
}


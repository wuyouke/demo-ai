package com.example.demo_ai.tools;

import com.example.demo_ai.model.Attraction;
import com.example.demo_ai.service.RealTouristAttractionService;
import dev.langchain4j.agent.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 旅游景点查询工具管理器
 * 优先使用真实 API，失败时降级到模拟数据
 */
@Component
public class TouristAttractionToolManager {

    private static final Logger logger = LoggerFactory.getLogger(TouristAttractionToolManager.class);

    @Autowired
    private RealTouristAttractionService realTouristAttractionService;

    /**
     * 查询指定城市的旅游景点
     */
    @Tool("查询指定城市的旅游景点推荐，包括景点名称、类型、描述、门票价格、评分等信息")
    public String getTouristAttractions(String city) {
        try {
            logger.info("AI 调用旅游景点查询工具，城市: {}", city);

            List<Attraction> attractions = realTouristAttractionService.getAttractions(city);

            if (attractions.isEmpty()) {
                return "抱歉，暂时没有找到 " + city + " 的旅游景点信息。建议查询其他城市（如：北京、上海、广州、深圳、成都、杭州）。";
            }

            // 格式化景点信息，便于 AI 理解和呈现
            StringBuilder result = new StringBuilder();
            result.append("【").append(city).append("】推荐旅游景点：\n\n");

            for (int i = 0; i < attractions.size(); i++) {
                Attraction attraction = attractions.get(i);
                result.append(i + 1).append(". ").append(attraction.getName()).append("\n");
                result.append("   类型: ").append(attraction.getType()).append("\n");
                if (attraction.getDescription() != null) {
                    result.append("   描述: ").append(attraction.getDescription()).append("\n");
                }
                result.append("   游玩时长: ").append(attraction.getDuration()).append(" 小时\n");
                result.append("   门票: ").append(attraction.getPrice() == null || attraction.getPrice() == 0 ? "免费" : attraction.getPrice() + " 元").append("\n");
                if (attraction.getRating() != null) {
                    result.append("   评分: ").append(attraction.getRating()).append("/5.0\n");
                }
                result.append("\n");
            }

            result.append("提示：以上景点信息仅供参考，实际开放时间和门票价格可能有所变动，建议出行前查询最新信息。");
            result.append("\n注: 数据来源优先使用高德地图 API，失败时使用模拟数据。");

            return result.toString();

        } catch (Exception e) {
            logger.error("旅游景点查询工具执行失败", e);
            return "查询旅游景点时出错：" + e.getMessage();
        }
    }

    /**
     * 查询指定城市的免费景点
     */
    @Tool("查询指定城市的免费旅游景点")
    public String getFreeAttractions(String city) {
        try {
            logger.info("AI 调用免费景点查询工具，城市: {}", city);

            List<Attraction> freeAttractions = realTouristAttractionService.getFreeAttractions(city);

            if (freeAttractions.isEmpty()) {
                return "抱歉，" + city + " 暂时没有找到免费景点信息。";
            }

            StringBuilder result = new StringBuilder();
            result.append("【").append(city).append("】免费景点推荐：\n\n");

            for (int i = 0; i < freeAttractions.size(); i++) {
                Attraction attraction = freeAttractions.get(i);
                result.append(i + 1).append(". ").append(attraction.getName()).append("\n");
                result.append("   类型: ").append(attraction.getType()).append("\n");
                if (attraction.getDescription() != null) {
                    result.append("   描述: ").append(attraction.getDescription()).append("\n");
                }
                result.append("   游玩时长: ").append(attraction.getDuration()).append(" 小时\n");
                if (attraction.getRating() != null) {
                    result.append("   评分: ").append(attraction.getRating()).append("/5.0\n");
                }
                result.append("\n");
            }

            result.append("提示：免费景点可能需要提前预约，建议出行前确认。");

            return result.toString();

        } catch (Exception e) {
            logger.error("免费景点查询工具执行失败", e);
            return "查询免费景点时出错：" + e.getMessage();
        }
    }

    /**
     * 查询指定城市的推荐景点（Top 3）
     */
    @Tool("查询指定城市的推荐旅游景点（Top 3）")
    public String getRecommendedAttractions(String city) {
        try {
            logger.info("AI 调用推荐景点查询工具，城市: {}", city);

            List<Attraction> recommended = realTouristAttractionService.getRecommendedAttractions(city);

            if (recommended.isEmpty()) {
                return "抱歉，暂时没有找到 " + city + " 的推荐景点信息。";
            }

            StringBuilder result = new StringBuilder();
            result.append("【").append(city).append("】强烈推荐的 3 个景点：\n\n");

            for (int i = 0; i < recommended.size(); i++) {
                Attraction attraction = recommended.get(i);
                result.append("★ ").append(attraction.getName()).append("\n");
                if (attraction.getRating() != null) {
                    result.append("  评分: ").append(attraction.getRating()).append("/5.0");
                }
                result.append("  |  门票: ").append(attraction.getPrice() == null || attraction.getPrice() == 0 ? "免费" : attraction.getPrice() + " 元").append("\n");
                if (attraction.getDescription() != null) {
                    result.append("  描述: ").append(attraction.getDescription()).append("\n");
                }
                result.append("\n");
            }

            return result.toString();

        } catch (Exception e) {
            logger.error("推荐景点查询工具执行失败", e);
            return "查询推荐景点时出错：" + e.getMessage();
        }
    }

    /**
     * 查询指定类型景点
     */
    @Tool("查询指定城市指定类型的旅游景点")
    public String getAttractionsByType(String city, String type) {
        try {
            logger.info("AI 调用类型景点查询工具，城市: {}, 类型: {}", city, type);

            List<Attraction> attractions = realTouristAttractionService.getAttractions(city, type, 5);

            if (attractions.isEmpty()) {
                return "抱歉，" + city + " 暂时没有找到 " + type + " 类型的景点。";
            }

            StringBuilder result = new StringBuilder();
            result.append("【").append(city).append("】").append(type).append("类型景点：\n\n");

            for (int i = 0; i < attractions.size(); i++) {
                Attraction attraction = attractions.get(i);
                result.append(i + 1).append(". ").append(attraction.getName()).append("\n");
                if (attraction.getDescription() != null) {
                    result.append("   描述: ").append(attraction.getDescription()).append("\n");
                }
                result.append("   门票: ").append(attraction.getPrice() == null || attraction.getPrice() == 0 ? "免费" : attraction.getPrice() + " 元").append("\n");
                if (attraction.getRating() != null) {
                    result.append("   评分: ").append(attraction.getRating()).append("/5.0\n");
                }
                result.append("\n");
            }

            return result.toString();

        } catch (Exception e) {
            logger.error("类型景点查询工具执行失败", e);
            return "查询类型景点时出错：" + e.getMessage();
        }
    }
}


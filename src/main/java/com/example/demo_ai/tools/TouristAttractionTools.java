package com.example.demo_ai.tools;

import com.example.demo_ai.model.Attraction;
import com.example.demo_ai.service.TouristAttractionService;
import dev.langchain4j.agent.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 旅游景点查询工具类
 * 使用 LangChain4j 的 @Tool 注解定义 AI 可以调用的函数
 */
@Component
public class TouristAttractionTools {

    private static final Logger logger = LoggerFactory.getLogger(TouristAttractionTools.class);

    @Autowired
    private TouristAttractionService touristAttractionService;

    /**
     * 查询指定城市的旅游景点
     *
     * @param city 城市名称（中文或英文），例如：北京、上海、Shanghai
     * @return 旅游景点信息
     */
    @Tool("查询指定城市的旅游景点推荐，包括景点名称、类型、描述、门票价格、评分等信息")
    public String getTouristAttractions(String city) {
        try {
            logger.info("AI 调用旅游景点查询工具，城市: {}", city);

            List<Attraction> attractions = touristAttractionService.getAttractions(city);

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
                result.append("   描述: ").append(attraction.getDescription()).append("\n");
                result.append("   游玩时长: ").append(attraction.getDuration()).append(" 小时\n");
                result.append("   门票: ").append(attraction.getPrice() == 0 ? "免费" : attraction.getPrice() + " 元").append("\n");
                result.append("   评分: ").append(attraction.getRating()).append("/5.0\n\n");
            }

            result.append("提示：以上景点信息仅供参考，实际开放时间和门票价格可能有所变动，建议出行前查询最新信息。");

            return result.toString();

        } catch (Exception e) {
            logger.error("旅游景点查询工具执行失败", e);
            return "查询旅游景点时出错：" + e.getMessage();
        }
    }

    /**
     * 查询指定城市的免费景点
     *
     * @param city 城市名称
     * @return 免费景点信息
     */
    @Tool("查询指定城市的免费旅游景点")
    public String getFreeAttractions(String city) {
        try {
            logger.info("AI 调用免费景点查询工具，城市: {}", city);

            List<Attraction> freeAttractions = touristAttractionService.getFreeAttractions(city);

            if (freeAttractions.isEmpty()) {
                return "抱歉，" + city + " 暂时没有找到免费景点信息。";
            }

            StringBuilder result = new StringBuilder();
            result.append("【").append(city).append("】免费景点推荐：\n\n");

            for (int i = 0; i < freeAttractions.size(); i++) {
                Attraction attraction = freeAttractions.get(i);
                result.append(i + 1).append(". ").append(attraction.getName()).append("\n");
                result.append("   类型: ").append(attraction.getType()).append("\n");
                result.append("   描述: ").append(attraction.getDescription()).append("\n");
                result.append("   游玩时长: ").append(attraction.getDuration()).append(" 小时\n");
                result.append("   评分: ").append(attraction.getRating()).append("/5.0\n\n");
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
     *
     * @param city 城市名称
     * @return 推荐景点信息
     */
    @Tool("查询指定城市的推荐旅游景点（Top 3）")
    public String getRecommendedAttractions(String city) {
        try {
            logger.info("AI 调用推荐景点查询工具，城市: {}", city);

            List<Attraction> recommended = touristAttractionService.getRecommendedAttractions(city);

            if (recommended.isEmpty()) {
                return "抱歉，暂时没有找到 " + city + " 的推荐景点信息。";
            }

            StringBuilder result = new StringBuilder();
            result.append("【").append(city).append("】强烈推荐的 3 个景点：\n\n");

            for (int i = 0; i < recommended.size(); i++) {
                Attraction attraction = recommended.get(i);
                result.append("★ ").append(attraction.getName()).append("\n");
                result.append("  评分: ").append(attraction.getRating()).append("/5.0");
                result.append("  |  门票: ").append(attraction.getPrice() == 0 ? "免费" : attraction.getPrice() + " 元").append("\n");
                result.append("  描述: ").append(attraction.getDescription()).append("\n\n");
            }

            return result.toString();

        } catch (Exception e) {
            logger.error("推荐景点查询工具执行失败", e);
            return "查询推荐景点时出错：" + e.getMessage();
        }
    }

    /**
     * 查询指定类型景点
     *
     * @param city 城市名称
     * @param type 景点类型（如：历史文化、自然风光、主题公园、博物馆等）
     * @return 景点信息
     */
    @Tool("查询指定城市指定类型的旅游景点")
    public String getAttractionsByType(String city, String type) {
        try {
            logger.info("AI 调用类型景点查询工具，城市: {}, 类型: {}", city, type);

            List<Attraction> attractions = touristAttractionService.getAttractions(city, type, 5);

            if (attractions.isEmpty()) {
                return "抱歉，" + city + " 暂时没有找到 " + type + " 类型的景点。";
            }

            StringBuilder result = new StringBuilder();
            result.append("【").append(city).append("】").append(type).append("类型景点：\n\n");

            for (int i = 0; i < attractions.size(); i++) {
                Attraction attraction = attractions.get(i);
                result.append(i + 1).append(". ").append(attraction.getName()).append("\n");
                result.append("   描述: ").append(attraction.getDescription()).append("\n");
                result.append("   门票: ").append(attraction.getPrice() == 0 ? "免费" : attraction.getPrice() + " 元").append("\n");
                result.append("   评分: ").append(attraction.getRating()).append("/5.0\n\n");
            }

            return result.toString();

        } catch (Exception e) {
            logger.error("类型景点查询工具执行失败", e);
            return "查询类型景点时出错：" + e.getMessage();
        }
    }
}


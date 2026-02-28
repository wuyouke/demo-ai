package com.example.demo_ai.service;

import com.example.demo_ai.model.Attraction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 旅游景点查询服务（模拟）
 * 用于演示多工具 Function Calling
 */
@Service
public class TouristAttractionService {

    private static final Logger logger = LoggerFactory.getLogger(TouristAttractionService.class);

    // 模拟的旅游景点数据
    private static final Map<String, List<Attraction>> ATTRACTIONS = new HashMap<>();

    static {
        // 北京景点
        List<Attraction> beijingAttractions = new ArrayList<>();
        beijingAttractions.add(new Attraction("故宫博物院", "历史文化", "明清两代皇宫，世界文化遗产", 4, 60, 4.9));
        beijingAttractions.add(new Attraction("长城", "历史文化", "中国古代军事防御工程", 5, 45, 4.8));
        beijingAttractions.add(new Attraction("天安门广场", "历史文化", "国家象征，世界最大城市广场", 1, 0, 4.7));
        beijingAttractions.add(new Attraction("颐和园", "自然风光", "皇家园林，世界文化遗产", 3, 30, 4.6));
        beijingAttractions.add(new Attraction("天坛公园", "历史文化", "明清皇帝祭天场所", 2, 15, 4.5));
        beijingAttractions.add(new Attraction("北海公园", "自然风光", "皇家园林，历史悠久的城市公园", 2, 10, 4.4));
        beijingAttractions.add(new Attraction("国家博物馆", "博物馆", "中国历史文化收藏展示", 3, 0, 4.8));
        beijingAttractions.add(new Attraction("798艺术区", "文化创意", "当代艺术中心，文创园区", 3, 0, 4.3));
        ATTRACTIONS.put("北京", beijingAttractions);
        ATTRACTIONS.put("Beijing", beijingAttractions);

        // 上海景点
        List<Attraction> shanghaiAttractions = new ArrayList<>();
        shanghaiAttractions.add(new Attraction("外滩", "历史文化", "万国建筑博览群，黄浦江畔", 2, 0, 4.8));
        shanghaiAttractions.add(new Attraction("东方明珠塔", "地标建筑", "上海地标，电视塔观光", 2, 220, 4.6));
        shanghaiAttractions.add(new Attraction("豫园", "历史文化", "明代私人园林，古典建筑", 2, 40, 4.5));
        shanghaiAttractions.add(new Attraction("上海博物馆", "博物馆", "中国古代艺术收藏", 3, 0, 4.7));
        shanghaiAttractions.add(new Attraction("迪士尼乐园", "主题公园", "全球知名主题公园", 8, 399, 4.9));
        shanghaiAttractions.add(new Attraction("南京路步行街", "商业购物", "中华商业第一街", 3, 0, 4.4));
        shanghaiAttractions.add(new Attraction("田子坊", "文化创意", "艺术街区，创意小店", 2, 0, 4.3));
        ATTRACTIONS.put("上海", shanghaiAttractions);
        ATTRACTIONS.put("Shanghai", shanghaiAttractions);

        // 广州景点
        List<Attraction> guangzhouAttractions = new ArrayList<>();
        guangzhouAttractions.add(new Attraction("广州塔", "地标建筑", "广州新地标，电视塔", 2, 150, 4.7));
        guangzhouAttractions.add(new Attraction("长隆野生动物世界", "主题公园", "大型野生动物园", 6, 300, 4.8));
        guangzhouAttractions.add(new Attraction("白云山", "自然风光", "城市绿肺，登山胜地", 4, 5, 4.6));
        guangzhouAttractions.add(new Attraction("陈家祠", "历史文化", "岭南建筑艺术精品", 1, 10, 4.5));
        guangzhouAttractions.add(new Attraction("沙面", "历史文化", "欧式建筑群，历史街区", 2, 0, 4.4));
        guangzhouAttractions.add(new Attraction("北京路步行街", "商业购物", "千年古道，购物中心", 2, 0, 4.3));
        ATTRACTIONS.put("广州", guangzhouAttractions);
        ATTRACTIONS.put("Guangzhou", guangzhouAttractions);

        // 深圳景点
        List<Attraction> shenzhenAttractions = new ArrayList<>();
        shenzhenAttractions.add(new Attraction("世界之窗", "主题公园", "微缩世界景观", 5, 200, 4.6));
        shenzhenAttractions.add(new Attraction("欢乐谷", "主题公园", "大型游乐园", 6, 250, 4.7));
        shenzhenAttractions.add(new Attraction("大梅沙海滨公园", "自然风光", "海滨浴场，沙滩", 3, 0, 4.5));
        shenzhenAttractions.add(new Attraction("东部华侨城", "主题公园", "生态旅游度假区", 5, 200, 4.6));
        shenzhenAttractions.add(new Attraction("深圳湾公园", "自然风光", "滨海休闲公园", 2, 0, 4.4));
        shenzhenAttractions.add(new Attraction("莲花山公园", "自然风光", "城市中心公园", 1, 0, 4.3));
        ATTRACTIONS.put("深圳", shenzhenAttractions);
        ATTRACTIONS.put("Shenzhen", shenzhenAttractions);

        // 成都景点
        List<Attraction> chengduAttractions = new ArrayList<>();
        chengduAttractions.add(new Attraction("都江堰", "历史文化", "古代水利工程，世界遗产", 3, 90, 4.8));
        chengduAttractions.add(new Attraction("青城山", "自然风光", "道教名山，清幽胜地", 4, 80, 4.7));
        chengduAttractions.add(new Attraction("武侯祠", "历史文化", "三国文化圣地", 2, 50, 4.6));
        chengduAttractions.add(new Attraction("锦里古街", "历史文化", "仿古商业街，民俗文化", 2, 0, 4.5));
        chengduAttractions.add(new Attraction("杜甫草堂", "历史文化", "杜甫故居，诗歌圣地", 2, 50, 4.5));
        chengduAttractions.add(new Attraction("大熊猫繁育研究基地", "自然风光", "观看大熊猫", 3, 55, 4.9));
        ATTRACTIONS.put("成都", chengduAttractions);
        ATTRACTIONS.put("Chengdu", chengduAttractions);

        // 杭州景点
        List<Attraction> hangzhouAttractions = new ArrayList<>();
        hangzhouAttractions.add(new Attraction("西湖", "自然风光", "著名风景区，世界遗产", 4, 0, 4.9));
        hangzhouAttractions.add(new Attraction("灵隐寺", "历史文化", "千年古刹，佛教圣地", 2, 75, 4.7));
        hangzhouAttractions.add(new Attraction("雷峰塔", "历史文化", "古塔景观，西湖十景", 1, 40, 4.5));
        hangzhouAttractions.add(new Attraction("宋城", "主题公园", "大型文化主题公园", 4, 300, 4.6));
        hangzhouAttractions.add(new Attraction("西溪湿地", "自然风光", "城市湿地公园", 4, 80, 4.6));
        hangzhouAttractions.add(new Attraction("河坊街", "历史文化", "历史文化街区", 2, 0, 4.4));
        ATTRACTIONS.put("杭州", hangzhouAttractions);
        ATTRACTIONS.put("Hangzhou", hangzhouAttractions);
    }

    /**
     * 查询指定城市的旅游景点
     *
     * @param city 城市名称
     * @return 景点列表
     */
    public List<Attraction> getAttractions(String city) {
        return getAttractions(city, null, 5);
    }

    /**
     * 查询指定城市的旅游景点
     *
     * @param city 城市名称
     * @param type 景点类型（可选）
     * @param limit 返回数量
     * @return 景点列表
     */
    public List<Attraction> getAttractions(String city, String type, Integer limit) {
        logger.info("查询旅游景点: 城市={}, 类型={}, 数量={}", city, type, limit);

        List<Attraction> attractions = ATTRACTIONS.get(city);

        if (attractions == null || attractions.isEmpty()) {
            logger.warn("城市 {} 没有预设景点数据", city);
            // 返回空列表，而不是生成随机数据
            return Collections.emptyList();
        }

        // 按类型筛选
        List<Attraction> filtered = attractions;
        if (type != null && !type.isEmpty()) {
            filtered = new ArrayList<>();
            for (Attraction attraction : attractions) {
                if (attraction.getType().contains(type)) {
                    filtered.add(attraction);
                }
            }
        }

        // 限制返回数量
        int count = Math.min(limit != null ? limit : 5, filtered.size());
        return filtered.subList(0, count);
    }

    /**
     * 获取景点推荐
     *
     * @param city 城市名称
     * @return 推荐的景点（返回 Top 3）
     */
    public List<Attraction> getRecommendedAttractions(String city) {
        return getAttractions(city, null, 3);
    }

    /**
     * 获取免费景点
     *
     * @param city 城市名称
     * @return 免费景点列表
     */
    public List<Attraction> getFreeAttractions(String city) {
        List<Attraction> allAttractions = ATTRACTIONS.get(city);
        if (allAttractions == null || allAttractions.isEmpty()) {
            return Collections.emptyList();
        }

        List<Attraction> freeAttractions = new ArrayList<>();
        for (Attraction attraction : allAttractions) {
            if (attraction.getPrice() == null || attraction.getPrice() == 0) {
                freeAttractions.add(attraction);
            }
        }

        return freeAttractions;
    }
}


package com.example.demo_ai.model;

/**
 * 旅游景点查询请求
 */
public class TouristAttractionRequest {

    /**
     * 城市名称
     */
    private String city;

    /**
     * 景点类型（可选）
     * 如：自然风光、历史文化、主题公园、博物馆等
     */
    private String type;

    /**
     * 推荐数量（默认返回 5 个）
     */
    private Integer limit;

    public TouristAttractionRequest() {
        this.limit = 5;
    }

    public TouristAttractionRequest(String city) {
        this.city = city;
        this.limit = 5;
    }

    public TouristAttractionRequest(String city, String type) {
        this.city = city;
        this.type = type;
        this.limit = 5;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    @Override
    public String toString() {
        return "TouristAttractionRequest{" +
                "city='" + city + '\'' +
                ", type='" + type + '\'' +
                ", limit=" + limit +
                '}';
    }
}


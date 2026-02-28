package com.example.demo_ai.model;

/**
 * 旅游景点信息
 */
public class Attraction {

    /**
     * 景点名称
     */
    private String name;

    /**
     * 景点类型
     */
    private String type;

    /**
     * 景点描述
     */
    private String description;

    /**
     * 推荐游玩时长（小时）
     */
    private Integer duration;

    /**
     * 门票价格（元）
     */
    private Integer price;

    /**
     * 评分（1-5分）
     */
    private Double rating;

    /**
     * 地址
     */
    private String address;

    /**
     * 开放时间
     */
    private String openTime;

    /**
     * 是否免费
     */
    private boolean free;

    public Attraction() {
    }

    public Attraction(String name, String type, String description, Integer duration, Integer price, Double rating) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.duration = duration;
        this.price = price;
        this.rating = rating;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getOpenTime() {
        return openTime;
    }

    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }

    public boolean isFree() {
        return free;
    }

    public void setFree(boolean free) {
        this.free = free;
    }

    @Override
    public String toString() {
        return "Attraction{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", duration=" + duration +
                ", price=" + price +
                ", rating=" + rating +
                ", address='" + address + '\'' +
                ", openTime='" + openTime + '\'' +
                ", free=" + free +
                '}';
    }
}


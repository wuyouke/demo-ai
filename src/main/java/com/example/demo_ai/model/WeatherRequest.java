package com.example.demo_ai.model;

/**
 * 天气查询请求
 */
public class WeatherRequest {

    /**
     * 城市名称（中文或英文）
     * 例如：北京、上海、Shanghai
     */
    private String city;

    /**
     * 省份（可选，用于更精确的定位）
     */
    private String province;

    /**
     * 国家（可选，默认为中国）
     */
    private String country;

    public WeatherRequest() {
    }

    public WeatherRequest(String city) {
        this.city = city;
    }

    public WeatherRequest(String city, String province) {
        this.city = city;
        this.province = province;
    }

    public WeatherRequest(String city, String province, String country) {
        this.city = city;
        this.province = province;
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public String toString() {
        return "WeatherRequest{" +
                "city='" + city + '\'' +
                ", province='" + province + '\'' +
                ", country='" + country + '\'' +
                '}';
    }
}


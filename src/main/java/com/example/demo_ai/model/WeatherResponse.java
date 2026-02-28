package com.example.demo_ai.model;

import java.util.List;

/**
 * 天气查询响应
 */
public class WeatherResponse {

    /**
     * 查询是否成功
     */
    private boolean success;

    /**
     * 城市/地区名称
     */
    private String location;

    /**
     * 当前温度（摄氏度）
     */
    private Double temperature;

    /**
     * 天气状况（如：晴、多云、雨等）
     */
    private String weather;

    /**
     * 湿度（百分比）
     */
    private Integer humidity;

    /**
     * 风速（公里/小时）
     */
    private Double windSpeed;

    /**
     * 风向（如：东北风、西南风等）
     */
    private String windDirection;

    /**
     * 气压（百帕）
     */
    private Integer pressure;

    /**
     * 能见度（公里）
     */
    private Double visibility;

    /**
     * 空气质量指数（AQI）
     */
    private Integer aqi;

    /**
     * 更新时间
     */
    private String updateTime;

    /**
     * 错误信息（如果查询失败）
     */
    private String errorMessage;

    /**
     * 预报信息（未来几天）
     */
    private List<Forecast> forecast;

    public WeatherResponse() {
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public Integer getHumidity() {
        return humidity;
    }

    public void setHumidity(Integer humidity) {
        this.humidity = humidity;
    }

    public Double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(Double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(String windDirection) {
        this.windDirection = windDirection;
    }

    public Integer getPressure() {
        return pressure;
    }

    public void setPressure(Integer pressure) {
        this.pressure = pressure;
    }

    public Double getVisibility() {
        return visibility;
    }

    public void setVisibility(Double visibility) {
        this.visibility = visibility;
    }

    public Integer getAqi() {
        return aqi;
    }

    public void setAqi(Integer aqi) {
        this.aqi = aqi;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public List<Forecast> getForecast() {
        return forecast;
    }

    public void setForecast(List<Forecast> forecast) {
        this.forecast = forecast;
    }

    /**
     * 天气预报内部类
     */
    public static class Forecast {
        private String date;
        private Double maxTemp;
        private Double minTemp;
        private String weather;

        public Forecast() {
        }

        public Forecast(String date, Double maxTemp, Double minTemp, String weather) {
            this.date = date;
            this.maxTemp = maxTemp;
            this.minTemp = minTemp;
            this.weather = weather;
        }

        // Getters and Setters
        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public Double getMaxTemp() {
            return maxTemp;
        }

        public void setMaxTemp(Double maxTemp) {
            this.maxTemp = maxTemp;
        }

        public Double getMinTemp() {
            return minTemp;
        }

        public void setMinTemp(Double minTemp) {
            this.minTemp = minTemp;
        }

        public String getWeather() {
            return weather;
        }

        public void setWeather(String weather) {
            this.weather = weather;
        }

        @Override
        public String toString() {
            return "Forecast{" +
                    "date='" + date + '\'' +
                    ", maxTemp=" + maxTemp +
                    ", minTemp=" + minTemp +
                    ", weather='" + weather + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "WeatherResponse{" +
                "success=" + success +
                ", location='" + location + '\'' +
                ", temperature=" + temperature +
                ", weather='" + weather + '\'' +
                ", humidity=" + humidity +
                ", windSpeed=" + windSpeed +
                ", windDirection='" + windDirection + '\'' +
                ", pressure=" + pressure +
                ", visibility=" + visibility +
                ", aqi=" + aqi +
                ", updateTime='" + updateTime + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}


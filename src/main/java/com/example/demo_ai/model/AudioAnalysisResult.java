package com.example.demo_ai.model;

import java.io.Serializable;

/**
 * 音频分析结果
 */
public class AudioAnalysisResult implements Serializable {

    private static final long serialVersionUID = 1L;

    // 识别的文本
    private String transcript;

    // 语言类型
    private String language;

    // 情感分析：positive, negative, neutral
    private String sentiment;

    // 情感置信度 (0-1)
    private double sentimentScore;

    // 关键词
    private String keywords;

    // 音频时长（秒）
    private int duration;

    // 音频质量评分 (0-100)
    private int audioQuality;

    // 是否包含噪音
    private boolean hasNoise;

    // 原始响应（供扩展）
    private String rawResponse;

    public AudioAnalysisResult() {}

    public AudioAnalysisResult(String transcript, String language) {
        this.transcript = transcript;
        this.language = language;
    }

    public String getTranscript() {
        return transcript;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }

    public double getSentimentScore() {
        return sentimentScore;
    }

    public void setSentimentScore(double sentimentScore) {
        this.sentimentScore = sentimentScore;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getAudioQuality() {
        return audioQuality;
    }

    public void setAudioQuality(int audioQuality) {
        this.audioQuality = audioQuality;
    }

    public boolean isHasNoise() {
        return hasNoise;
    }

    public void setHasNoise(boolean hasNoise) {
        this.hasNoise = hasNoise;
    }

    public String getRawResponse() {
        return rawResponse;
    }

    public void setRawResponse(String rawResponse) {
        this.rawResponse = rawResponse;
    }

    @Override
    public String toString() {
        return "AudioAnalysisResult{" +
                "transcript='" + transcript + '\'' +
                ", language='" + language + '\'' +
                ", sentiment='" + sentiment + '\'' +
                ", sentimentScore=" + sentimentScore +
                ", keywords='" + keywords + '\'' +
                ", duration=" + duration +
                ", audioQuality=" + audioQuality +
                ", hasNoise=" + hasNoise +
                '}';
    }
}


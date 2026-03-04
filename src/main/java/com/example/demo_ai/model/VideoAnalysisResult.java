package com.example.demo_ai.model;

import java.io.Serializable;
import java.util.List;

/**
 * 视频分析结果
 */
public class VideoAnalysisResult implements Serializable {

    private static final long serialVersionUID = 1L;

    // 视频标题
    private String title;

    // 视频描述
    private String description;

    // 视频时长（秒）
    private int duration;

    // 帧率 (fps)
    private int frameRate;

    // 分辨率
    private String resolution;

    // 视频中的主要物体/人物
    private List<String> objects;

    // 视频中的场景描述
    private String sceneDescription;

    // 检测到的人脸数量
    private int faceCount;

    // 检测到的文字内容
    private String extractedText;

    // 音频转录
    private String audioTranscript;

    // 视频质量评分 (0-100)
    private int qualityScore;

    // 主要颜色
    private List<String> dominantColors;

    // 情感分析：positive, negative, neutral
    private String sentiment;

    // 推荐的视频类别
    private List<String> categories;

    // 原始响应（供扩展）
    private String rawResponse;

    public VideoAnalysisResult() {}

    public VideoAnalysisResult(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(int frameRate) {
        this.frameRate = frameRate;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public List<String> getObjects() {
        return objects;
    }

    public void setObjects(List<String> objects) {
        this.objects = objects;
    }

    public String getSceneDescription() {
        return sceneDescription;
    }

    public void setSceneDescription(String sceneDescription) {
        this.sceneDescription = sceneDescription;
    }

    public int getFaceCount() {
        return faceCount;
    }

    public void setFaceCount(int faceCount) {
        this.faceCount = faceCount;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }

    public String getAudioTranscript() {
        return audioTranscript;
    }

    public void setAudioTranscript(String audioTranscript) {
        this.audioTranscript = audioTranscript;
    }

    public int getQualityScore() {
        return qualityScore;
    }

    public void setQualityScore(int qualityScore) {
        this.qualityScore = qualityScore;
    }

    public List<String> getDominantColors() {
        return dominantColors;
    }

    public void setDominantColors(List<String> dominantColors) {
        this.dominantColors = dominantColors;
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public String getRawResponse() {
        return rawResponse;
    }

    public void setRawResponse(String rawResponse) {
        this.rawResponse = rawResponse;
    }

    @Override
    public String toString() {
        return "VideoAnalysisResult{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", duration=" + duration +
                ", frameRate=" + frameRate +
                ", resolution='" + resolution + '\'' +
                ", faceCount=" + faceCount +
                ", qualityScore=" + qualityScore +
                ", sentiment='" + sentiment + '\'' +
                '}';
    }
}


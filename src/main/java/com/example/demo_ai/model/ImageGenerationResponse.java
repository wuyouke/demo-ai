package com.example.demo_ai.model;

import java.util.List;

/**
 * 图像生成响应模型
 */
public class ImageGenerationResponse {

    /**
     * 生成的图像 URL 列表
     */
    private List<String> imageUrls;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 错误信息（如果失败）
     */
    private String errorMessage;

    /**
     * 提示词
     */
    private String prompt;

    public ImageGenerationResponse() {
        this.success = true;
    }

    public ImageGenerationResponse(List<String> imageUrls, String prompt) {
        this.imageUrls = imageUrls;
        this.prompt = prompt;
        this.success = true;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    @Override
    public String toString() {
        return "ImageGenerationResponse{" +
                "imageUrls=" + imageUrls +
                ", success=" + success +
                ", errorMessage='" + errorMessage + '\'' +
                ", prompt='" + prompt + '\'' +
                '}';
    }
}


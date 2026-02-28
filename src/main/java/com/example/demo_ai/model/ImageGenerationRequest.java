package com.example.demo_ai.model;

/**
 * 图像生成请求模型
 */
public class ImageGenerationRequest {

    /**
     * 提示词（描述要生成的图像）
     */
    private String prompt;

    /**
     * 图像尺寸：256x256, 512x512, 1024x1024
     */
    private String size;

    /**
     * 生成数量（1-4）
     */
    private Integer n;

    public ImageGenerationRequest() {
    }

    public ImageGenerationRequest(String prompt) {
        this.prompt = prompt;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public Integer getN() {
        return n;
    }

    public void setN(Integer n) {
        this.n = n;
    }

    @Override
    public String toString() {
        return "ImageGenerationRequest{" +
                "prompt='" + prompt + '\'' +
                ", size='" + size + '\'' +
                ", n=" + n +
                '}';
    }
}


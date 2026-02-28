package com.example.demo_ai.tools;

import com.example.demo_ai.model.ImageGenerationResponse;
import com.example.demo_ai.service.ImageGenerationService;
import dev.langchain4j.agent.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ImageToolManager {

    private static final Logger logger = LoggerFactory.getLogger(ImageToolManager.class);

    @Autowired
    private ImageGenerationService imageGenerationService;

    @Tool("根据用户的描述生成图片，并返回图片的URL。当用户要求画图、生成图片时调用此工具。注意：图片生成可能需要15-30秒，请耐心等待。")
    public String generateImage(String prompt) {
        logger.info("AI 调用图像生成工具，提示词: {}", prompt);
        try {
            // 返回一个中间状态消息，让用户知道正在处理
            logger.info("开始生成图片，用户将看到等待提示...");

            // 在调用 API 前输出状态信息
            String processingMsg = "🎨 正在生成图片中，这可能需要 15-30 秒，请稍候...";
            logger.info(processingMsg);

            ImageGenerationResponse response = imageGenerationService.generateImage(prompt);
            if (response.isSuccess() && response.getImageUrls() != null && !response.getImageUrls().isEmpty()) {
                String imageUrl = response.getImageUrls().get(0);
                logger.info("图像生成成功: {}", imageUrl);
                // 返回 Markdown 格式的图片，这样 AI 可以直接输出给用户
                return "✅ 图片生成成功！请直接将以下 Markdown 格式返回给用户：\n![" + prompt + "](" + imageUrl + ")";
            } else {
                return "❌ 图片生成失败: " + response.getErrorMessage();
            }
        } catch (Exception e) {
            logger.error("调用图像生成工具异常", e);
            return "❌ 图片生成发生异常: " + e.getMessage();
        }
    }
}


package com.example.demo_ai.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.zhipu.ZhipuAiChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * LangChain4j 配置类
 * 支持多种 AI 模型提供商：智谱 AI（zhipu）、OpenAI 等
 */
@Configuration
public class LangChain4jConfig {

    private static final Logger logger = LoggerFactory.getLogger(LangChain4jConfig.class);

    @Value("${langchain4j.provider:zhipu}")
    private String provider;

    // 智谱 AI 配置
    @Value("${langchain4j.zhipu.api-key}")
    private String zhipuApiKey;

    @Value("${langchain4j.zhipu.model-name}")
    private String zhipuModelName;

    @Value("${langchain4j.zhipu.temperature:0.7}")
    private Double zhipuTemperature;

    @Value("${langchain4j.zhipu.max-tokens:1000}")
    private Integer zhipuMaxTokens;

    @Value("${langchain4j.zhipu.timeout:60}")
    private Integer zhipuTimeout;

    // OpenAI 配置
    @Value("${langchain4j.openai.api-key}")
    private String openaiApiKey;

    @Value("${langchain4j.openai.model-name}")
    private String openaiModelName;

    @Value("${langchain4j.openai.temperature:0.7}")
    private Double openaiTemperature;

    @Value("${langchain4j.openai.max-tokens:1000}")
    private Integer openaiMaxTokens;

    @Value("${langchain4j.openai.timeout:60}")
    private Integer openaiTimeout;

    /**
     * 创建 ChatLanguageModel Bean
     * 根据 provider 配置选择不同的模型提供商
     */
    @Bean
    public ChatLanguageModel chatLanguageModel() {
        logger.info("初始化 AI 模型，提供商: {}", provider);

        switch (provider.toLowerCase()) {
            case "zhipu":
                logger.info("使用智谱 AI 模型: {}", zhipuModelName);
                return createZhipuModel();

            case "openai":
                logger.info("使用 OpenAI 模型: {}", openaiModelName);
                return createOpenAiModel();

            default:
                logger.warn("未知的提供商: {}，默认使用智谱 AI", provider);
                return createZhipuModel();
        }
    }

    /**
     * 创建智谱 AI 模型
     */
    private ChatLanguageModel createZhipuModel() {
        return ZhipuAiChatModel.builder()
                .apiKey(zhipuApiKey)
                .model(zhipuModelName)
                .temperature(zhipuTemperature)
                .maxToken(zhipuMaxTokens)
                .build();
    }

    /**
     * 创建 OpenAI 模型
     */
    private ChatLanguageModel createOpenAiModel() {
        return OpenAiChatModel.builder()
                .apiKey(openaiApiKey)
                .modelName(openaiModelName)
                .temperature(openaiTemperature)
                .maxTokens(openaiMaxTokens)
                .timeout(Duration.ofSeconds(openaiTimeout))
                .build();
    }
}


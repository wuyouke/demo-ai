package com.example.demo_ai.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 嵌入服务 - 负责文本到向量的转换
 * 使用简化的本地实现（不依赖外部 API）
 */
@Service
public class EmbeddingService {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddingService.class);

    private static final int EMBEDDING_DIMENSION = 384;
    private final EmbeddingModel embeddingModel;

    public EmbeddingService() {
        this.embeddingModel = new LocalEmbeddingModel();
        logger.info("嵌入服务初始化完成，维度: {}", EMBEDDING_DIMENSION);
    }

    /**
     * 获取单个文本的嵌入向量
     */
    public Embedding embedText(String text) {
        try {
            if (text == null || text.trim().isEmpty()) {
                throw new IllegalArgumentException("文本不能为空");
            }
            Response<Embedding> response = embeddingModel.embed(text);
            return response.content();
        } catch (Exception e) {
            logger.error("嵌入文本失败: {}", text, e);
            throw new RuntimeException("嵌入文本失败: " + e.getMessage());
        }
    }

    /**
     * 获取多个文本的嵌入向量
     */
    public List<Embedding> embedTexts(List<String> texts) {
        try {
            if (texts == null || texts.isEmpty()) {
                throw new IllegalArgumentException("文本列表不能为空");
            }
            List<TextSegment> segments = new ArrayList<>();
            for (String text : texts) {
                segments.add(TextSegment.from(text));
            }
            Response<List<Embedding>> response = embeddingModel.embedAll(segments);
            return response.content();
        } catch (Exception e) {
            logger.error("嵌入多个文本失败", e);
            throw new RuntimeException("嵌入多个文本失败: " + e.getMessage());
        }
    }

    /**
     * 本地嵌入模型实现
     * 基于文本特征生成稳定的嵌入向量
     */
    private static class LocalEmbeddingModel implements EmbeddingModel {

        @Override
        public Response<Embedding> embed(String text) {
            float[] vector = generateEmbedding(text);
            Embedding embedding = new Embedding(vector);
            return Response.from(embedding);
        }

        @Override
        public Response<List<Embedding>> embedAll(List<TextSegment> textSegments) {
            List<Embedding> embeddings = new ArrayList<>();
            for (TextSegment segment : textSegments) {
                float[] vector = generateEmbedding(segment.text());
                embeddings.add(new Embedding(vector));
            }
            return Response.from(embeddings);
        }

        @Override
        public int dimension() {
            return EMBEDDING_DIMENSION;
        }

        /**
         * 生成嵌入向量
         * 基于文本的统计特征：字符频率、长度、哈希值等
         */
        private float[] generateEmbedding(String text) {
            float[] vector = new float[EMBEDDING_DIMENSION];

            if (text == null || text.isEmpty()) {
                return vector;
            }

            // 基础特征：文本长度和哈希值（快速计算）
            int textLength = text.length();
            long hashCode = text.hashCode();

            // 快速计算样本字符统计而不是全文扫描
            int sampleSize = Math.min(1000, textLength);
            int sampleWordCount = 0;
            for (int j = 0; j < sampleSize; j++) {
                if (text.charAt(j) == ' ' || text.charAt(j) == '\n' || text.charAt(j) == '\t') {
                    sampleWordCount++;
                }
            }
            // 估算总字数
            int estimatedWordCount = (sampleWordCount * textLength) / Math.max(1, sampleSize);

            // 生成基础向量：使用文本的统计特征
            for (int i = 0; i < EMBEDDING_DIMENSION; i++) {
                // 组合多个特征来生成稳定的向量
                long seed = hashCode * 31 + i;
                double value = Math.sin(seed / 1000.0) * 0.5 +
                              Math.cos((seed + textLength) / 1000.0) * 0.3 +
                              Math.sin((seed + estimatedWordCount) / 1000.0) * 0.2;

                // 添加样本字符特征
                int charIndex = (int) ((i * textLength / EMBEDDING_DIMENSION) % textLength);
                if (charIndex < text.length()) {
                    value += (text.charAt(charIndex) % 256) / 256.0 * 0.1;
                }

                // 规范化到 [-1, 1] 范围
                vector[i] = (float) (value / 2.0);
            }

            // 向量归一化
            normalizeVector(vector);
            return vector;
        }

        /**
         * 向量归一化（L2 归一化）
         */
        private void normalizeVector(float[] vector) {
            float norm = 0;
            for (float v : vector) {
                norm += v * v;
            }
            norm = (float) Math.sqrt(norm);

            if (norm > 0) {
                for (int i = 0; i < vector.length; i++) {
                    vector[i] /= norm;
                }
            }
        }
    }

    /**
     * 获取嵌入模型的维度
     */
    public int getEmbeddingDimension() {
        return embeddingModel.dimension();
    }

    /**
     * 计算两个向量的相似度（余弦相似度）
     */
    public double cosineSimilarity(float[] vector1, float[] vector2) {
        if (vector1.length != vector2.length) {
            throw new IllegalArgumentException("向量维度不匹配");
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            norm1 += vector1[i] * vector1[i];
            norm2 += vector2[i] * vector2[i];
        }

        norm1 = Math.sqrt(norm1);
        norm2 = Math.sqrt(norm2);

        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }

        return dotProduct / (norm1 * norm2);
    }
}


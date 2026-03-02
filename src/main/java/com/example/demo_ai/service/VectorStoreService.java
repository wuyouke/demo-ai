package com.example.demo_ai.service;

import dev.langchain4j.data.embedding.Embedding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 向量存储服务 - 在内存中管理向量和文本
 * 实现本地向量库的基本功能（检索、存储、删除）
 */
@Service
public class VectorStoreService {

    private static final Logger logger = LoggerFactory.getLogger(VectorStoreService.class);

    private static final double SIMILARITY_THRESHOLD = 0.3; // 相似度阈值

    @Autowired
    private EmbeddingService embeddingService;

    /**
     * 存储结构：userId -> collectionId -> List<VectorEntry>
     */
    private final Map<String, Map<String, List<VectorEntry>>> vectorStore = new ConcurrentHashMap<>();

    /**
     * 向量条目
     */
    private static class VectorEntry {
        String id;
        String text;
        Embedding embedding;
        Map<String, String> metadata; // 存储额外的元数据（如原始文档ID）

        VectorEntry(String id, String text, Embedding embedding) {
            this.id = id;
            this.text = text;
            this.embedding = embedding;
            this.metadata = new HashMap<>();
        }
    }

    /**
     * 添加文本到向量库
     */
    public String addText(String userId, String collectionId, String text, Map<String, String> metadata) {
        try {
            if (text == null || text.trim().isEmpty()) {
                throw new IllegalArgumentException("文本不能为空");
            }

            // 获取或创建用户的向量库
            Map<String, List<VectorEntry>> userStore = vectorStore.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());
            List<VectorEntry> collection = userStore.computeIfAbsent(collectionId, k -> new ArrayList<>());

            // 生成嵌入
            Embedding embedding = embeddingService.embedText(text);

            // 创建向量条目
            String entryId = UUID.randomUUID().toString();
            VectorEntry entry = new VectorEntry(entryId, text, embedding);
            if (metadata != null) {
                entry.metadata.putAll(metadata);
            }

            // 添加到集合
            collection.add(entry);

            logger.info("文本已添加到向量库：用户={}, 集合={}, ID={}", userId, collectionId, entryId);
            return entryId;
        } catch (Exception e) {
            logger.error("添加文本到向量库失败", e);
            throw new RuntimeException("添加文本到向量库失败: " + e.getMessage());
        }
    }

    /**
     * 批量添加文本到向量库
     */
    public List<String> addTexts(String userId, String collectionId, List<String> texts, String documentId) {
        List<String> entryIds = new ArrayList<>();

        logger.info("开始批量添加文本到向量库：用户={}, 集合={}, 文本数量={}", userId, collectionId, texts.size());
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < texts.size(); i++) {
            String text = texts.get(i);
            Map<String, String> metadata = new HashMap<>();
            if (documentId != null) {
                metadata.put("documentId", documentId);
            }
            logger.debug("处理第 {}/{} 个文本块，大小：{}", i + 1, texts.size(), text.length());
            String entryId = addText(userId, collectionId, text, metadata);
            entryIds.add(entryId);
        }

        long duration = System.currentTimeMillis() - startTime;
        logger.info("批量添加文本完成：用户={}, 集合={}, 条目数={}, 耗时: {}ms", userId, collectionId, entryIds.size(), duration);

        return entryIds;
    }

    /**
     * 检索相似的文本
     */
    public List<RetrievedDocument> search(String userId, String collectionId, String query, int topK) {
        try {
            if (query == null || query.trim().isEmpty()) {
                throw new IllegalArgumentException("查询文本不能为空");
            }

            // 获取用户的向量库
            Map<String, List<VectorEntry>> userStore = vectorStore.get(userId);
            if (userStore == null) {
                logger.warn("用户的向量库不存在：userId={}", userId);
                return new ArrayList<>();
            }

            List<VectorEntry> collection = userStore.get(collectionId);
            if (collection == null || collection.isEmpty()) {
                logger.warn("集合不存在或为空：collectionId={}", collectionId);
                return new ArrayList<>();
            }

            // 获取查询的嵌入
            Embedding queryEmbedding = embeddingService.embedText(query);

            // 计算相似度
            List<SimilarityScore> scores = new ArrayList<>();
            for (VectorEntry entry : collection) {
                double similarity = embeddingService.cosineSimilarity(
                    queryEmbedding.vector(),
                    entry.embedding.vector()
                );

                if (similarity >= SIMILARITY_THRESHOLD) {
                    scores.add(new SimilarityScore(entry, similarity));
                }
            }

            // 按相似度排序
            scores.sort((a, b) -> Double.compare(b.similarity, a.similarity));

            // 返回前 topK 个结果
            List<RetrievedDocument> results = new ArrayList<>();
            int limit = Math.min(topK, scores.size());
            for (int i = 0; i < limit; i++) {
                SimilarityScore score = scores.get(i);
                results.add(new RetrievedDocument(
                    score.entry.id,
                    score.entry.text,
                    score.similarity,
                    score.entry.metadata
                ));
            }

            logger.info("检索完成：查询='{}', 结果数={}", query, results.size());
            return results;
        } catch (Exception e) {
            logger.error("检索失败", e);
            throw new RuntimeException("检索失败: " + e.getMessage());
        }
    }

    /**
     * 删除集合中的文本
     */
    public boolean deleteText(String userId, String collectionId, String entryId) {
        Map<String, List<VectorEntry>> userStore = vectorStore.get(userId);
        if (userStore == null) {
            return false;
        }

        List<VectorEntry> collection = userStore.get(collectionId);
        if (collection == null) {
            return false;
        }

        boolean removed = collection.removeIf(entry -> entry.id.equals(entryId));
        if (removed) {
            logger.info("文本已删除：用户={}, 集合={}, ID={}", userId, collectionId, entryId);
        }
        return removed;
    }

    /**
     * 删除集合
     */
    public boolean deleteCollection(String userId, String collectionId) {
        Map<String, List<VectorEntry>> userStore = vectorStore.get(userId);
        if (userStore == null) {
            return false;
        }

        List<VectorEntry> collection = userStore.remove(collectionId);
        if (collection != null) {
            logger.info("集合已删除：用户={}, 集合={}, 条目数={}", userId, collectionId, collection.size());
            return true;
        }
        return false;
    }

    /**
     * 删除用户的所有集合
     */
    public boolean deleteAllCollections(String userId) {
        Map<String, List<VectorEntry>> userStore = vectorStore.remove(userId);
        if (userStore != null) {
            logger.info("用户的所有集合已删除：userId={}, 集合数={}", userId, userStore.size());
            return true;
        }
        return false;
    }

    /**
     * 获取集合的统计信息
     */
    public CollectionStats getCollectionStats(String userId, String collectionId) {
        Map<String, List<VectorEntry>> userStore = vectorStore.get(userId);
        if (userStore == null) {
            return new CollectionStats(collectionId, 0, 0);
        }

        List<VectorEntry> collection = userStore.get(collectionId);
        if (collection == null) {
            return new CollectionStats(collectionId, 0, 0);
        }

        long totalCharacters = collection.stream()
            .mapToLong(entry -> entry.text.length())
            .sum();

        return new CollectionStats(collectionId, collection.size(), totalCharacters);
    }

    /**
     * 相似度分数
     */
    private static class SimilarityScore {
        VectorEntry entry;
        double similarity;

        SimilarityScore(VectorEntry entry, double similarity) {
            this.entry = entry;
            this.similarity = similarity;
        }
    }

    /**
     * 检索到的文档
     */
    public static class RetrievedDocument {
        public String id;
        public String text;
        public double similarity;
        public Map<String, String> metadata;

        public RetrievedDocument(String id, String text, double similarity, Map<String, String> metadata) {
            this.id = id;
            this.text = text;
            this.similarity = similarity;
            this.metadata = metadata;
        }

        public String getId() {
            return id;
        }

        public String getText() {
            return text;
        }

        public double getSimilarity() {
            return similarity;
        }

        public Map<String, String> getMetadata() {
            return metadata;
        }
    }

    /**
     * 集合统计信息
     */
    public static class CollectionStats {
        public String collectionId;
        public int documentCount;
        public long totalCharacters;

        public CollectionStats(String collectionId, int documentCount, long totalCharacters) {
            this.collectionId = collectionId;
            this.documentCount = documentCount;
            this.totalCharacters = totalCharacters;
        }

        public String getCollectionId() {
            return collectionId;
        }

        public int getDocumentCount() {
            return documentCount;
        }

        public long getTotalCharacters() {
            return totalCharacters;
        }
    }
}


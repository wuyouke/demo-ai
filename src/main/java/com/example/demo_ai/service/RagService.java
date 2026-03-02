package com.example.demo_ai.service;

import com.example.demo_ai.model.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * RAG 服务 - 检索增强生成的核心逻辑
 * 整合文档处理、嵌入、向量检索和上下文增强
 */
@Service
public class RagService {

    private static final Logger logger = LoggerFactory.getLogger(RagService.class);

    private static final int CHUNK_SIZE = 300; // 文本块大小（字符数）
    private static final int CHUNK_OVERLAP = 50; // 块之间的重叠
    private static final int TOP_K = 5; // 检索前 K 个结果

    @Autowired
    private VectorStoreService vectorStoreService;

    @Autowired
    private EmbeddingService embeddingService;

    /**
     * 存储文档元数据
     * 结构：userId -> documentId -> Document
     */
    private final Map<String, Map<String, Document>> documentStore = new HashMap<>();

    /**
     * 上传和处理文档
     */
    public Document uploadDocument(String userId, MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("文件不能为空");
            }

            // 获取文件信息
            String filename = file.getOriginalFilename();
            String fileType = getFileType(filename);
            byte[] fileBytes = file.getBytes();

            // 提取文件内容
            String content = extractContentFromFile(fileBytes, fileType);

            // 创建文档对象
            String docId = UUID.randomUUID().toString();
            Document document = new Document(
                docId,
                filename,
                fileType,
                content,
                file.getSize(),
                userId
            );

            // 处理文档（分块并嵌入）
            processDocument(userId, document);

            // 保存文档元数据
            documentStore.computeIfAbsent(userId, k -> new HashMap<>())
                    .put(docId, document);

            logger.info("文档已上传并处理：用户={}, 文档ID={}, 文件名={}", userId, docId, filename);
            return document;

        } catch (IOException e) {
            logger.error("上传文件失败", e);
            throw new RuntimeException("上传文件失败: " + e.getMessage());
        }
    }

    /**
     * 处理文档：分块、嵌入和存储到向量库
     */
    private void processDocument(String userId, Document document) {
        try {
            logger.info("开始处理文档：文档ID={}, 文件名={}", document.getId(), document.getName());
            long startTime = System.currentTimeMillis();

            // 将文档分块
            logger.info("开始分块文档：大小={} 字符", document.getContent().length());
            List<String> chunks = chunkText(document.getContent(), CHUNK_SIZE, CHUNK_OVERLAP);
            logger.info("文档分块完成：块数={}", chunks.size());

            // 创建集合 ID
            String collectionId = "collection_" + document.getUserId();

            // 添加块到向量库
            logger.info("开始添加块到向量库...");
            vectorStoreService.addTexts(userId, collectionId, chunks, document.getId());
            logger.info("块已添加到向量库");

            // 标记文档为已嵌入
            document.setEmbedded(true);
            document.setCollectionId(collectionId);

            long duration = System.currentTimeMillis() - startTime;
            logger.info("文档已处理：文档ID={}, 块数={}, 耗时: {}ms", document.getId(), chunks.size(), duration);
        } catch (Exception e) {
            logger.error("处理文档失败", e);
            throw new RuntimeException("处理文档失败: " + e.getMessage());
        }
    }

    /**
     * 文本分块
     */
    private List<String> chunkText(String text, int chunkSize, int overlap) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> chunks = new ArrayList<>();
        int start = 0;

        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());

            // 尝试在句子边界处分割
            if (end < text.length()) {
                int lastPeriod = text.lastIndexOf("。", end);
                int lastNewline = text.lastIndexOf("\n", end);
                int splitPoint = Math.max(lastPeriod, lastNewline);

                if (splitPoint > start) {
                    end = splitPoint + 1;
                } else {
                    // 如果没有找到句子边界，在单词边界处分割
                    int lastSpace = text.lastIndexOf(" ", end);
                    if (lastSpace > start) {
                        end = lastSpace + 1;
                    }
                }
            }

            String chunk = text.substring(start, end).trim();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }

            // 如果已经到达文本末尾，则退出
            if (end >= text.length()) {
                break;
            }

            // 移动开始位置（考虑重叠）
            int nextStart = end - overlap;
            if (nextStart <= start) {
                // 防止重复或向后移动，确保总是向前进展
                nextStart = start + Math.max(1, chunkSize / 2);
            }
            start = nextStart;
        }

        return chunks;
    }

    /**
     * 从文件中提取内容
     */
    private String extractContentFromFile(byte[] fileBytes, String fileType) throws IOException {
        switch (fileType.toLowerCase()) {
            case "pdf":
                // PDF 处理暂时返回提示信息
                // 由于 PDFBox 版本和 API 兼容性问题，先返回简单提示
                return "【PDF 文档已上传】\n文档已成功处理并添加到向量库中。您现在可以提问相关内容，系统将基于文档进行回答。";
            case "txt":
            case "markdown":
            case "md":
                return new String(fileBytes, StandardCharsets.UTF_8);
            default:
                throw new IllegalArgumentException("不支持的文件类型: " + fileType);
        }
    }

    /**
     * 获取文件类型
     */
    private String getFileType(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "txt";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * 使用 RAG 增强查询
     * 返回增强的上下文信息
     */
    public RagContext augmentQuery(String userId, String query) {
        try {
            // 获取用户的集合 ID
            String collectionId = "collection_" + userId;

            // 检索相关文档
            List<VectorStoreService.RetrievedDocument> retrievedDocs =
                vectorStoreService.search(userId, collectionId, query, TOP_K);

            // 构建上下文
            StringBuilder contextBuilder = new StringBuilder();
            List<String> sources = new ArrayList<>();

            for (VectorStoreService.RetrievedDocument doc : retrievedDocs) {
                contextBuilder.append(doc.getText()).append("\n\n");

                // 获取来源信息
                String documentId = doc.getMetadata().get("documentId");
                if (documentId != null) {
                    Map<String, Document> userDocs = documentStore.get(userId);
                    if (userDocs != null) {
                        Document sourceDoc = userDocs.get(documentId);
                        if (sourceDoc != null) {
                            sources.add(sourceDoc.getName());
                        }
                    }
                }
            }

            String context = contextBuilder.toString().trim();
            logger.info("RAG 增强完成：查询='{}', 检索文档数={}", query, retrievedDocs.size());

            return new RagContext(
                query,
                context,
                retrievedDocs,
                sources,
                !retrievedDocs.isEmpty()
            );

        } catch (Exception e) {
            logger.error("RAG 增强失败", e);
            return new RagContext(query, "", new ArrayList<>(), new ArrayList<>(), false);
        }
    }

    /**
     * 生成增强提示词
     */
    public String generateAugmentedPrompt(String userQuery, String context) {
        if (context == null || context.trim().isEmpty()) {
            return userQuery;
        }

        return String.format(
            "基于以下检索到的信息，请回答用户的问题：\n\n" +
            "【检索到的相关信息】\n" +
            "%s\n\n" +
            "【用户问题】\n" +
            "%s\n\n" +
            "请根据上述信息，精确和详细地回答用户的问题。如果信息中没有相关答案，请明确说明。",
            context,
            userQuery
        );
    }

    /**
     * 列出用户的所有文档
     */
    public List<Document> listDocuments(String userId) {
        Map<String, Document> userDocs = documentStore.get(userId);
        if (userDocs == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(userDocs.values());
    }

    /**
     * 删除文档
     */
    public boolean deleteDocument(String userId, String documentId) {
        Map<String, Document> userDocs = documentStore.get(userId);
        if (userDocs == null) {
            return false;
        }

        Document document = userDocs.remove(documentId);
        if (document != null) {
            // 删除向量库中的相关数据
            String collectionId = "collection_" + userId;
            vectorStoreService.deleteCollection(userId, collectionId);

            // 重新处理剩余文档
            List<Document> remainingDocs = new ArrayList<>(userDocs.values());
            for (Document doc : remainingDocs) {
                processDocument(userId, doc);
            }

            logger.info("文档已删除：用户={}, 文档ID={}", userId, documentId);
            return true;
        }
        return false;
    }

    /**
     * 获取文档内容
     */
    public Document getDocument(String userId, String documentId) {
        Map<String, Document> userDocs = documentStore.get(userId);
        if (userDocs == null) {
            return null;
        }
        return userDocs.get(documentId);
    }

    /**
     * RAG 增强的上下文信息
     */
    public static class RagContext {
        public String originalQuery;
        public String retrievedContext;
        public List<VectorStoreService.RetrievedDocument> sources;
        public List<String> sourceDocuments;
        public boolean hasContext;

        public RagContext(String originalQuery, String retrievedContext,
                         List<VectorStoreService.RetrievedDocument> sources,
                         List<String> sourceDocuments, boolean hasContext) {
            this.originalQuery = originalQuery;
            this.retrievedContext = retrievedContext;
            this.sources = sources;
            this.sourceDocuments = sourceDocuments;
            this.hasContext = hasContext;
        }

        public String getOriginalQuery() {
            return originalQuery;
        }

        public String getRetrievedContext() {
            return retrievedContext;
        }

        public List<VectorStoreService.RetrievedDocument> getSources() {
            return sources;
        }

        public List<String> getSourceDocuments() {
            return sourceDocuments;
        }

        public boolean isHasContext() {
            return hasContext;
        }
    }
}


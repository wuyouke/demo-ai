package com.example.demo_ai.controller;

import com.example.demo_ai.model.Document;
import com.example.demo_ai.service.RagService;
import com.example.demo_ai.service.VectorStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文档管理控制器 - 处理文档上传、删除、查询等操作
 */
@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DocumentController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);

    @Autowired
    private RagService ragService;

    @Autowired
    private VectorStoreService vectorStoreService;

    /**
     * 上传文档
     * POST /api/documents/upload
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocument(@RequestParam("file") MultipartFile file,
                                           @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            // 如果没有提供用户ID，使用默认值
            if (userId == null || userId.isEmpty()) {
                userId = "default_user";
            }

            // 验证文件
            if (file.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "文件不能为空");
                return ResponseEntity.badRequest().body(error);
            }

            String filename = file.getOriginalFilename();
            if (filename == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "无效的文件名");
                return ResponseEntity.badRequest().body(error);
            }

            // 检查文件类型
            String fileType = getFileType(filename);
            List<String> allowedTypes = Arrays.asList("pdf", "txt", "md", "markdown");
            if (!allowedTypes.contains(fileType.toLowerCase())) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "不支持的文件类型: " + fileType);
                return ResponseEntity.badRequest().body(error);
            }

            // 检查文件大小（最大 10MB）
            if (file.getSize() > 10 * 1024 * 1024) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "文件过大，最大支持 10MB");
                return ResponseEntity.badRequest().body(error);
            }

            // 上传和处理文档
            Document document = ragService.uploadDocument(userId, file);

            logger.info("文档上传成功：用户={}, 文档ID={}, 文件名={}", userId, document.getId(), filename);

            Map<String, Object> docMap = new HashMap<>();
            docMap.put("id", document.getId());
            docMap.put("name", document.getName());
            docMap.put("type", document.getType());
            docMap.put("size", document.getSize());
            docMap.put("createdAt", document.getCreatedAt());
            docMap.put("embedded", document.isEmbedded());

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "文档上传成功");
            response.put("document", docMap);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("文档上传失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "文档上传失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 列出用户的所有文档
     * GET /api/documents/list
     */
    @GetMapping("/list")
    public ResponseEntity<?> listDocuments(@RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            if (userId == null || userId.isEmpty()) {
                userId = "default_user";
            }

            List<Document> documents = ragService.listDocuments(userId);

            // 获取每个文档的统计信息
            List<Map<String, Object>> docList = new ArrayList<>();
            for (Document doc : documents) {
                String collectionId = "collection_" + userId;
                VectorStoreService.CollectionStats stats = vectorStoreService.getCollectionStats(userId, collectionId);

                Map<String, Object> docInfo = new HashMap<>();
                docInfo.put("id", doc.getId());
                docInfo.put("name", doc.getName());
                docInfo.put("type", doc.getType());
                docInfo.put("size", doc.getSize());
                docInfo.put("createdAt", doc.getCreatedAt());
                docInfo.put("embedded", doc.isEmbedded());
                docInfo.put("description", doc.getDescription());

                docList.add(docInfo);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("count", documents.size());
            response.put("documents", docList);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("获取文档列表失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取文档列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 获取文档详情
     * GET /api/documents/{documentId}
     */
    @GetMapping("/{documentId}")
    public ResponseEntity<?> getDocument(@PathVariable String documentId,
                                        @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            if (userId == null || userId.isEmpty()) {
                userId = "default_user";
            }

            Document document = ragService.getDocument(userId, documentId);
            if (document == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> docMap = new HashMap<>();
            docMap.put("id", document.getId());
            docMap.put("name", document.getName());
            docMap.put("type", document.getType());
            docMap.put("size", document.getSize());
            docMap.put("createdAt", document.getCreatedAt());
            docMap.put("embedded", document.isEmbedded());
            docMap.put("description", document.getDescription());
            docMap.put("content", document.getContent());

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("document", docMap);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("获取文档详情失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取文档详情失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 删除文档
     * DELETE /api/documents/{documentId}
     */
    @DeleteMapping("/{documentId}")
    public ResponseEntity<?> deleteDocument(@PathVariable String documentId,
                                           @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            if (userId == null || userId.isEmpty()) {
                userId = "default_user";
            }

            boolean deleted = ragService.deleteDocument(userId, documentId);
            if (!deleted) {
                return ResponseEntity.notFound().build();
            }

            logger.info("文档删除成功：用户={}, 文档ID={}", userId, documentId);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "文档删除成功");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("文档删除失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "文档删除失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 获取向量库统计信息
     * GET /api/documents/stats
     */
    @GetMapping("/stats/info")
    public ResponseEntity<?> getStats(@RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            if (userId == null || userId.isEmpty()) {
                userId = "default_user";
            }

            String collectionId = "collection_" + userId;
            VectorStoreService.CollectionStats stats = vectorStoreService.getCollectionStats(userId, collectionId);
            List<Document> documents = ragService.listDocuments(userId);

            Map<String, Object> statsMap = new HashMap<>();
            statsMap.put("documentCount", documents.size());
            statsMap.put("vectorCount", stats.getDocumentCount());
            statsMap.put("totalCharacters", stats.getTotalCharacters());

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("stats", statsMap);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("获取统计信息失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取统计信息失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 辅助方法：获取文件类型
     */
    private String getFileType(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "txt";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}


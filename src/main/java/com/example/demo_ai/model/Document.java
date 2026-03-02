package com.example.demo_ai.model;

import java.time.LocalDateTime;

/**
 * 文档模型 - 用于存储用户上传的文档信息
 */
public class Document {
    /**
     * 文档ID
     */
    private String id;

    /**
     * 文档名称
     */
    private String name;

    /**
     * 文档类型（pdf, txt, markdown等）
     */
    private String type;

    /**
     * 文档内容
     */
    private String content;

    /**
     * 文档大小（字节）
     */
    private long size;

    /**
     * 所属用户ID
     */
    private String userId;

    /**
     * 向量集合ID（用于Chroma）
     */
    private String collectionId;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 文档描述
     */
    private String description;

    /**
     * 是否已处理（嵌入）
     */
    private boolean embedded;

    // 构造函数
    public Document() {
    }

    public Document(String id, String name, String type, String content, long size, String userId) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.content = content;
        this.size = size;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEmbedded() {
        return embedded;
    }

    public void setEmbedded(boolean embedded) {
        this.embedded = embedded;
    }
}


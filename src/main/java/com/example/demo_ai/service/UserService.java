package com.example.demo_ai.service;

import com.example.demo_ai.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户服务（内存存储）
 */
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    /**
     * 用户存储（内存中）
     * Key: username, Value: User
     */
    private final Map<String, User> users = new ConcurrentHashMap<>();

    /**
     * 用户 ID 索引
     * Key: userId, Value: username
     */
    private final Map<String, String> userIdIndex = new ConcurrentHashMap<>();

    /**
     * 注册新用户
     */
    public User register(String username, String password, String email) {
        // 检查用户是否已存在
        if (users.containsKey(username)) {
            logger.warn("用户已存在: {}", username);
            return null;
        }

        // 创建新用户
        User user = new User(username, hashPassword(password), email);
        user.setUserId(UUID.randomUUID().toString());

        // 存储用户
        users.put(username, user);
        userIdIndex.put(user.getUserId(), username);

        logger.info("新用户注册: {}", username);
        return user;
    }

    /**
     * 用户登录
     */
    public User login(String username, String password) {
        User user = users.get(username);

        if (user == null) {
            logger.warn("用户不存在: {}", username);
            return null;
        }

        if (!verifyPassword(password, user.getPassword())) {
            logger.warn("密码错误: {}", username);
            return null;
        }

        if (!user.isActive()) {
            logger.warn("用户已禁用: {}", username);
            return null;
        }

        // 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());

        logger.info("用户登录成功: {}", username);
        return user;
    }

    /**
     * 根据用户名获取用户
     */
    public User getUserByUsername(String username) {
        return users.get(username);
    }

    /**
     * 根据用户 ID 获取用户名
     */
    public String getUsernameByUserId(String userId) {
        return userIdIndex.get(userId);
    }

    /**
     * 根据用户 ID 获取用户
     */
    public User getUserById(String userId) {
        String username = userIdIndex.get(userId);
        return username != null ? users.get(username) : null;
    }

    /**
     * 验证用户是否存在
     */
    public boolean userExists(String username) {
        return users.containsKey(username);
    }

    /**
     * 更新用户
     */
    public User updateUser(User user) {
        if (user == null || user.getUsername() == null) {
            return null;
        }

        User existingUser = users.get(user.getUsername());
        if (existingUser == null) {
            return null;
        }

        users.put(user.getUsername(), user);
        logger.info("用户已更新: {}", user.getUsername());
        return user;
    }

    /**
     * 获取所有用户数量
     */
    public int getTotalUsers() {
        return users.size();
    }

    /**
     * 密码加密
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] messageDigest = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : messageDigest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            logger.error("密码加密失败", e);
            return password;  // 备选方案
        }
    }

    /**
     * 密码验证
     */
    private boolean verifyPassword(String rawPassword, String hashedPassword) {
        return hashPassword(rawPassword).equals(hashedPassword);
    }
}


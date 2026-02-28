package com.example.demo_ai.service;

import com.example.demo_ai.model.AuthRequest;
import com.example.demo_ai.model.AuthResponse;
import com.example.demo_ai.model.User;
import com.example.demo_ai.util.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 认证服务
 */
@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * 用户注册
     */
    public AuthResponse register(AuthRequest request) {
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            return new AuthResponse(false, "用户名和密码不能为空");
        }

        if (userService.userExists(request.getUsername())) {
            return new AuthResponse(false, "用户已存在");
        }

        try {
            User user = userService.register(
                    request.getUsername(),
                    request.getPassword(),
                    request.getEmail()
            );

            if (user == null) {
                return new AuthResponse(false, "注册失败");
            }

            String token = jwtTokenProvider.generateToken(user.getUsername(), user.getUserId());
            logger.info("用户注册成功: {}", request.getUsername());
            return new AuthResponse(true, "注册成功", token, user);
        } catch (Exception e) {
            logger.error("注册时出错", e);
            return new AuthResponse(false, "注册失败: " + e.getMessage());
        }
    }

    /**
     * 用户登录
     */
    public AuthResponse login(AuthRequest request) {
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            return new AuthResponse(false, "用户名和密码不能为空");
        }

        try {
            User user = userService.login(request.getUsername(), request.getPassword());

            if (user == null) {
                return new AuthResponse(false, "用户名或密码错误");
            }

            String token = jwtTokenProvider.generateToken(user.getUsername(), user.getUserId());
            logger.info("用户登录成功: {}", request.getUsername());
            return new AuthResponse(true, "登录成功", token, user);
        } catch (Exception e) {
            logger.error("登录时出错", e);
            return new AuthResponse(false, "登录失败: " + e.getMessage());
        }
    }

    /**
     * 验证 Token
     */
    public boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        return jwtTokenProvider.validateToken(token);
    }

    /**
     * 从 Token 获取用户名
     */
    public String getUsernameFromToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        return jwtTokenProvider.getUsernameFromToken(token);
    }

    /**
     * 从 Token 获取用户 ID
     */
    public String getUserIdFromToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        return jwtTokenProvider.getUserIdFromToken(token);
    }

    /**
     * 从 Token 获取用户
     */
    public User getUserFromToken(String token) {
        String username = getUsernameFromToken(token);
        if (username == null) {
            return null;
        }
        return userService.getUserByUsername(username);
    }
}


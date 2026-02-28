package com.example.demo_ai.controller;

import com.example.demo_ai.model.AuthRequest;
import com.example.demo_ai.model.AuthResponse;
import com.example.demo_ai.model.User;
import com.example.demo_ai.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest request) {
        logger.info("注册请求: {}", request.getUsername());
        AuthResponse response = authService.register(request);
        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        logger.info("登录请求: {}", request.getUsername());
        AuthResponse response = authService.login(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    /**
     * 验证 Token（用于前端检查是否已登录）
     */
    @GetMapping("/verify")
    public ResponseEntity<AuthResponse> verify(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(false, "未提供 Token"));
        }

        // 去掉 "Bearer " 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        boolean valid = authService.validateToken(token);
        if (valid) {
            return ResponseEntity.ok(new AuthResponse(true, "Token 有效"));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(false, "Token 无效或已过期"));
        }
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(false, "未提供 Token"));
        }

        // 去掉 "Bearer " 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (!authService.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(false, "Token 无效或已过期"));
        }

        try {
            User user = authService.getUserFromToken(token);
            if (user != null) {
                return ResponseEntity.ok(new AuthResponse(true, "获取用户信息成功", token, user));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new AuthResponse(false, "用户不存在"));
            }
        } catch (Exception e) {
            logger.error("获取用户信息失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(false, "获取用户信息失败"));
        }
    }
}


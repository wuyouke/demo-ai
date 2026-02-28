package com.example.demo_ai.config;

import com.example.demo_ai.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT 认证拦截器
 */
@Component
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationInterceptor.class);

    @Autowired
    private AuthService authService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        logger.info("拦截器处理请求: {}", requestURI);

        // 从请求头中获取 Token
        String token = extractToken(request);

        if (token != null && authService.validateToken(token)) {
            // Token 有效，继续处理请求
            String userId = authService.getUserIdFromToken(token);
            String username = authService.getUsernameFromToken(token);
            request.setAttribute("userId", userId);
            request.setAttribute("username", username);
            logger.info("Token 有效，用户: {} 访问 {}", username, requestURI);
            return true;
        } else {
            // Token 无效或不存在
            logger.warn("无效或缺失的 Token，请求 URI: {}", requestURI);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"未授权，请先登录\"}");
            return false;
        }
    }

    /**
     * 从请求头中提取 Token
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}


package com.example.demo_ai.config;

import com.example.demo_ai.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT 认证过滤器（已停用，使用 JwtAuthenticationInterceptor 替代）
 * @deprecated 使用 JwtAuthenticationInterceptor 替代
 */
@Deprecated
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private AuthService authService;

    /**
     * 不需要认证的路径（公开路径）
     */
    private static final String[] PUBLIC_PATHS = {
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/verify",
            "/api/auth/me",
            "/static/",
            "/",
            "/index.html",
            "/chat.html",
            "/api/chat/health"  // 健康检查
    };

    /**
     * 需要认证的路径（受保护的 API）
     */
    private static final String[] PROTECTED_API_PATHS = {
            "/api/chat",
            "/api/conversation",
            "/api/image"  // 图像生成等其他受保护的 API
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String requestURI = request.getRequestURI();
            logger.debug("处理请求: {}", requestURI);

            // 检查是否是公开路径
            if (isPublicPath(requestURI)) {
                logger.debug("公开路径，无需认证: {}", requestURI);
                filterChain.doFilter(request, response);
                return;
            }

            // 检查是否是受保护的 API
            if (isProtectedApi(requestURI)) {
                logger.debug("受保护的 API，需要认证: {}", requestURI);

                // 从请求头中获取 Token
                String token = extractToken(request);

                if (token != null && authService.validateToken(token)) {
                    // Token 有效，继续处理请求
                    String userId = authService.getUserIdFromToken(token);
                    String username = authService.getUsernameFromToken(token);
                    request.setAttribute("userId", userId);
                    request.setAttribute("username", username);
                    logger.debug("Token 有效，用户: {}", username);
                    filterChain.doFilter(request, response);
                } else {
                    // Token 无效或不存在
                    logger.warn("无效或缺失的 Token: {}", requestURI);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"success\":false,\"message\":\"未授权，请提供有效的 JWT Token\"}");
                }
            } else {
                // 其他路径，直接放行
                filterChain.doFilter(request, response);
            }
        } catch (Exception e) {
            logger.error("JWT 认证过滤器出错", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"认证失败\"}");
        }
    }

    /**
     * 检查是否是公开路径
     */
    private boolean isPublicPath(String requestURI) {
        for (String publicPath : PUBLIC_PATHS) {
            if (requestURI.equals(publicPath) || requestURI.startsWith(publicPath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查是否是受保护的 API
     */
    private boolean isProtectedApi(String requestURI) {
        for (String protectedPath : PROTECTED_API_PATHS) {
            if (requestURI.startsWith(protectedPath)) {
                return true;
            }
        }
        return false;
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


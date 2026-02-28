package com.example.demo_ai.config;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 基于 IP 的细粒度限流拦截器
 */
@Component
public class IpRateLimitInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(IpRateLimitInterceptor.class);

    // 使用 Guava Cache 存储每个 IP 的 RateLimiter
    // 设置 1 小时无访问后自动过期，防止内存泄漏
    private final Cache<String, RateLimiter> limiters = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = getClientIp(request);

        try {
            // 获取该 IP 对应的 RateLimiter，如果没有则创建一个 (这里设置为每秒最多 2 次请求)
            RateLimiter rateLimiter = limiters.get(clientIp, () -> RateLimiter.create(2.0));

            // 尝试获取令牌
            if (rateLimiter.tryAcquire()) {
                return true; // 放行
            } else {
                logger.warn("IP [{}] 请求过于频繁，被限流，请求路径: {}", clientIp, request.getRequestURI());

                // 返回 429 Too Many Requests 状态码
                response.setStatus(429);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"success\":false,\"message\":\"您的请求过于频繁，请稍后再试\"}");
                return false; // 拦截
            }
        } catch (ExecutionException e) {
            logger.error("获取限流器失败", e);
            return true; // 发生异常时为了不影响业务，选择放行
        }
    }

    /**
     * 获取客户端真实 IP (处理经过 Nginx 等反向代理的情况)
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 对于通过多个代理的情况，第一个 IP 为客户端真实 IP，多个 IP 按照 ',' 分割
        if (ip != null && ip.length() > 15) {
            if (ip.indexOf(",") > 0) {
                ip = ip.substring(0, ip.indexOf(","));
            }
        }
        return ip;
    }
}


package com.example.demo_ai.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtAuthenticationInterceptor jwtAuthenticationInterceptor;

    @Autowired
    private IpRateLimitInterceptor ipRateLimitInterceptor;

    /**
     * 注册拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1. 注册 IP 限流拦截器 (优先级最高，最先执行，避免浪费资源去验证 Token)
        registry.addInterceptor(ipRateLimitInterceptor)
                .addPathPatterns("/api/**")
                .order(0);

        // 2. 注册 JWT 认证拦截器
        registry.addInterceptor(jwtAuthenticationInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/register",
                        "/api/auth/login",
                        "/api/auth/verify",
                        "/api/auth/me",
                        "/api/chat/health",
                        "/static/**",
                        "/index.html",
                        "/chat.html"
                )
                .order(1);
    }
}


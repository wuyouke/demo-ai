package com.example.demo_ai.model;

/**
 * 认证请求
 */
public class AuthRequest {
    private String username;
    private String password;
    private String email;  // 注册时使用

    public AuthRequest() {}

    public AuthRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "AuthRequest{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}


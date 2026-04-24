package com.example.homework.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 仅对登录接口配置
                .allowedOriginPatterns("http://192.168.240.24:3000")
                .allowedOrigins("http://localhost:3000")  // 前端地址
                .allowedMethods("GET", "POST", "OPTIONS", "PUT", "DELETE")  // 允许 POST 和 OPTIONS
                .allowedHeaders("*")  // 允许 所有 类型
                .allowCredentials(true) // 允许携带Cookie（若需要）
                .maxAge(3600);  // 预检请求缓存时间（1小时内不再重复发送）
    }
}
package com.example.homework.config;

import com.example.homework.common.JwtInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Bean
    public JwtInterceptor jwtInterceptor() {
        return new JwtInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/user/login","/user/register"); // 登录接口放行
                                                                // 拦截所有以/user/和/book/开头的路径（包括子路径）
                                                                //.addPathPatterns("/user/**", "/book/**")
                                                                // 可选：排除某些不需要拦截的子路径（例如登录接口）
                                                                //.excludePathPatterns("/user/login", "/book/public/**");

    }
}
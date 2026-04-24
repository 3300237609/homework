package com.example.homework.config;

import com.example.homework.filter.AccessEndFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<AccessEndFilter> accessEndFilterRegistration() {
        FilterRegistrationBean<AccessEndFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new AccessEndFilter());
        registration.addUrlPatterns("/*"); // 拦截所有请求
        registration.setName("AccessEndFilter");
        registration.setOrder(1); // 过滤器执行顺序（越小越先执行）
        return registration;
    }
}
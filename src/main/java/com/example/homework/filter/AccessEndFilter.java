package com.example.homework.filter;


import com.example.homework.common.UserContextHolder;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;

@WebFilter(urlPatterns = "/*", filterName = "AccessEndFilter")
public class AccessEndFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 过滤器初始化逻辑（可选）
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } finally {
            UserContextHolder.clear(); // 清理ThreadLocal
        }
    }

    @Override
    public void destroy() {
        // 过滤器销毁逻辑（可选）
    }
}
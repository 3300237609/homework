package com.example.homework.common;

import com.example.homework.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

public class JwtInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        // 关键：如果是OPTIONS预检请求，直接放行（不验证Token）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true; // 放行OPTIONS请求
        }

        // 1. 从Cookie中提取Token
        Cookie[] cookies = request.getCookies();
        String token = null;
        if (cookies != null) {
            token = Arrays.stream(cookies)
                    .filter(cookie -> "authToken".equals(cookie.getName()))
                    .findFirst()
                    .map(Cookie::getValue)
                    .orElse(null);
        }
        // 2. 验证Token
        if (!jwtUtil.validateToken(token)) {
            response.setStatus(401);
            return false;
        }

        // 3. （可选）将用户信息存入请求属性，供后续接口使用
        Claims claims = jwtUtil.parseToken(token);
        UserContextHolder.setUserId((String) claims.get("userId"));
        UserContextHolder.setRole(claims.get("roleId").toString());
        return true;
    }
}
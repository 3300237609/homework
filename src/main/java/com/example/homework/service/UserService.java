package com.example.homework.service;

import com.example.homework.common.R;
import com.example.homework.entity.User;

import javax.servlet.http.HttpServletResponse;

public interface UserService {
    R<User> login(String username, String password, HttpServletResponse response);
    R<String> register(User user);
}

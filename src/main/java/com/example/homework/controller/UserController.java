package com.example.homework.controller;

import com.example.homework.common.R;
import com.example.homework.entity.User;
import com.example.homework.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
public class UserController {
    @Autowired
    UserService userService;
    @PostMapping("/login")
    R<User> login(@RequestBody User user, HttpServletResponse response) {
        return userService.login(user.getUsername(), user.getPassword(), response);
    }

    @PostMapping("/register")
    R<String> register(@RequestBody User user) {
        return userService.register(user);
    }
}

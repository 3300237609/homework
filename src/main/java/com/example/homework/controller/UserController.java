package com.example.homework.controller;

import com.example.homework.common.R;
import com.example.homework.entity.User;
import com.example.homework.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserService userService;

    //登录
    @PostMapping("/login")
    R<User> login(@RequestBody User user, HttpServletResponse response) {
        return userService.login(user.getUsername(), user.getPassword(), response);
    }
    //注册
    @PostMapping("/register")
    public R<String> register(@RequestBody User user) {
        return userService.register(user);
    }

    //GET /list 获取所有用户 可按角色/班级筛选 分页 --管理员
    @GetMapping("/list")
    public R<ArrayList<User>> getUserList(
            @RequestParam(required = false, defaultValue = "") String role,
            @RequestParam(required = false, defaultValue = "") String clazzId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        return userService.getUserList(role, clazzId, pageNum, pageSize);
    }

    //POST /add 新增用户（批量/单个） --管理员
    @PostMapping("/add")
    public R<String> addUser(@RequestBody ArrayList<User> users) {
       return userService.addUser(users);
    }

    //PUT /update 修改用户信息（姓名、班级、角色） --管理员
    @PutMapping("/update") // 关键：补充PutMapping注解
    public R<String> updateUser(@RequestBody User user) {
        return userService.updateUser(user);
    }

    //DELETE /delete/{id }删除用户 --管理员
    @DeleteMapping("/delete/{id}")
    public R<String> deleteUser(@PathVariable Long id) {
       return userService.deleteUser(id);
    }
    @GetMapping("/getNameById")
    public R<String> getNameById(@RequestParam Long id) {
        return userService.getNameById(id);
    }

}

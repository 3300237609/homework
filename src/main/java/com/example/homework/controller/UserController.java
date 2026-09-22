package com.example.homework.controller;

import com.example.homework.common.R;
import com.example.homework.entity.User;
import com.example.homework.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserService userService;

    /**
     * 用户登录
     * 请求方式：POST
     * 请求路径：/user/login
     * 请求体：User对象（用户名、密码）
     * 返回值：登录用户信息（含Token）
     */
    @PostMapping("/login")
    R<User> login(@RequestBody User user, HttpServletResponse response) {
        return userService.login(user.getUsername(), user.getPassword(), response);
    }
    /**
     * 用户注册
     * 请求方式：POST
     * 请求路径：/user/register
     * 请求体：User对象（用户名、密码、角色等）
     * 返回值：操作结果
     */
    @PostMapping("/register")
    public R<String> register(@RequestBody User user) {
        return userService.register(user);
    }

    /**
     * 获取所有用户（可按角色/班级筛选，分页）--管理员
     * 请求方式：GET
     * 请求路径：/user/list
     * 请求参数：role（角色，可选）、clazzId（班级ID，可选）、pageNum（页码，默认1）、pageSize（每页条数，默认10）
     * 返回值：用户列表
     */
    @GetMapping("/list")
    public R<ArrayList<User>> getUserList(
            @RequestParam(required = false, defaultValue = "") String role,
            @RequestParam(required = false, defaultValue = "") String clazzId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        return userService.getUserList(role, clazzId, pageNum, pageSize);
    }

    /**
     * 新增用户（支持批量/单个）--管理员
     * 请求方式：POST
     * 请求路径：/user/add
     * 请求体：User对象列表（用户名、密码、角色、班级等）
     * 返回值：操作结果
     */
    @PostMapping("/add")
    public R<String> addUser(@RequestBody ArrayList<User> users) {
       return userService.addUser(users);
    }

    /**
     * 修改用户信息（姓名、班级、角色）--管理员
     * 请求方式：PUT
     * 请求路径：/user/update
     * 请求体：User对象（ID、姓名、班级、角色）
     * 返回值：操作结果
     */
    @PutMapping("/update") // 关键：补充PutMapping注解
    public R<String> updateUser(@RequestBody User user) {
        return userService.updateUser(user);
    }

    /**
     * 删除用户 --管理员
     * 请求方式：DELETE
     * 请求路径：/user/delete/{id}
     * 路径参数：id（用户ID）
     * 返回值：操作结果
     */
    @DeleteMapping("/delete/{id}")
    public R<String> deleteUser(@PathVariable Long id) {
       return userService.deleteUser(id);
    }
    /**
     * 根据用户ID获取用户姓名（用于展示转换）
     * 请求方式：GET
     * 请求路径：/user/getNameById
     * 请求参数：id（用户ID）
     * 返回值：用户姓名
     */
    @GetMapping("/getNameById")
    public R<String> getNameById(@RequestParam Long id) {
        return userService.getNameById(id);
    }

}

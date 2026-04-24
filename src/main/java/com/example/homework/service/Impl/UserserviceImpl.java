package com.example.homework.service.Impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.example.homework.common.R;
import com.example.homework.entity.User;
import com.example.homework.mapper.UserMapper;
import com.example.homework.service.UserService;
import com.example.homework.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
public class UserserviceImpl implements UserService {
    @Autowired
    UserMapper userMapper;
    @Autowired
    JwtUtil jwtUtil;
    @Override
    public R<User> login(String username, String password, HttpServletResponse response) {
        //判断用户名和密码是否为空
        if (StrUtil.isBlank(username) || StrUtil.isBlank(password)) {
            return R.error("数据异常！");
        }
        //MD5加密
        password = (SecureUtil.md5(password));
        //查询数据库
        User user = userMapper.login(username, password);
        //查询失败
        if (ObjectUtil.isEmpty(user)) {
            return R.error("账号或密码错误！");
        }
        //成功则生成Token
        String token = jwtUtil.generateToken(user.getId().toString(), user.getRoleId());
        //生成cookie
        Cookie cookie = new Cookie("authToken", token);
        // 设置为HttpOnly，防止XSS攻击
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(cookie);

        return R.success(user);
    }

    @Override
    public R<String> register(User user) {
        //判断用户名和密码是否为空
        if (StrUtil.isBlank(user.getUsername())
                || StrUtil.isBlank(user.getPassword())
                || StrUtil.isBlank(user.getName())
                || user.getRoleId() == null
                || user.getClassId() == null
        ) {
            return R.error("数据异常！");
        }
        //查询数据库昵称是否重复
        boolean userByName = userMapper.getUserByUsername(user.getUsername());
        if (userByName){
            //昵称重复
            return R.error("昵称重复！");
        }
        //MD5加密
        user.setPassword(SecureUtil.md5(user.getPassword()));
        boolean register = userMapper.register(user);
        //注册失败
        if (!register){
            return R.error("注册失败！");
        }
        return R.success("OK!");
    }
}

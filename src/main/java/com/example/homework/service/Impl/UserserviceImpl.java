package com.example.homework.service.Impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.example.homework.common.R;
import com.example.homework.common.UserContextHolder;
import com.example.homework.entity.User;
import com.example.homework.mapper.UserMapper;
import com.example.homework.service.UserService;
import com.example.homework.utils.JwtUtil;
import com.example.homework.utils.PermissionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Iterator;

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
        ) {
            return R.error("数据异常！");
        }
        //查询数据库昵称是否重复
        boolean userByName = userMapper.getUserByUsername(user.getUsername());
        if (userByName) {
            //昵称重复
            return R.error("昵称重复！");
        }
        //MD5加密
        user.setPassword(SecureUtil.md5(user.getPassword()));
        boolean register = userMapper.register(user);
        //注册失败
        if (!register) {
            return R.error("注册失败！");
        }
        return R.success("OK!");
    }

    @Override
    public R<ArrayList<User>> getUserList(String role, String clazzId, Integer pageNum, Integer pageSize) {

        if (!PermissionUtil.isAdmin()) {
            return R.error("权限不足！");
        }

        // 分页参数默认值
        if (pageNum == null) pageNum = 1;
        if (pageSize == null) pageSize = 10;

        // 计算偏移量
        Integer offset = (pageNum - 1) * pageSize;

        // 查询列表
        ArrayList<User> userList = userMapper.getUserList(role, clazzId, offset, pageSize);

        // 查询总数
        Integer total = userMapper.getUserCount(role, clazzId);

        // 按你的格式返回：data放list，分页信息放进map里
        return R.success(userList)
                .add("total", total)
                .add("pageNum", pageNum)
                .add("pageSize", pageSize);
    }

    @Override
    public R<String> addUser(ArrayList<User> users) {
        if (!PermissionUtil.isAdmin()) {
            return R.error("权限不足！");
        }

        ArrayList<User> usersOut = new ArrayList<>();
        Iterator<User> iterator = users.iterator();
        while (iterator.hasNext()) {
            User user = iterator.next();
            if (user.getUsername().isEmpty()) {
                iterator.remove();
                usersOut.add(user);
                continue;
            }
            if (userMapper.getUserByUsername(user.getUsername())) {
                usersOut.add(user);
                iterator.remove();
            }
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                user.setPassword("123456");
            }
        }
        if (!users.isEmpty()) {
            for (User user : users) {
                user.setPassword(SecureUtil.md5(user.getPassword()));
            }
            userMapper.addUser(users);
        }
        boolean success = usersOut.isEmpty();
        return success ? R.success("新增用户成功") : R.error("有" + users.size() + "条成功！有" + usersOut.size() + "条昵称重复添加失败：" + usersOut);
    }

    @Override
    public R<String> updateUser(User users) {
        if (!Long.valueOf(UserContextHolder.getUserId()).equals(users.getId())) {
            if (!PermissionUtil.isAdmin()) {
                return R.error("权限不足！");
            }
        }
        return userMapper.updateUser(users) ? R.success("更新成功！") : R.error("更新失败！");
    }

    @Override
    public R<String> deleteUser(Long id) {
        if (!PermissionUtil.isAdmin()) {
            return R.error("权限不足！");
        }
        if (id == null) {
            return R.error("数据为空！");
        }
        return userMapper.deleteUser(id) ? R.success("删除成功！") : R.error("删除失败！");
    }

    @Override
    public R<String> getNameById(Long id) {
        String userName = userMapper.getNameById(id);
        // 如果没查到，返回“未知用户”
        if (userName == null) {
            return R.error("用户不存在！");
        }
        return R.success(userName);
    }
}

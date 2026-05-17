package com.example.homework.service;

import com.example.homework.common.R;
import com.example.homework.entity.User;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

public interface UserService {
    R<User> login(String username, String password, HttpServletResponse response);
    R<String> register(User user);
    R<ArrayList<User>> getUserList(String role, String clazzId, Integer pageNum, Integer pageSize);
    R<String> addUser(ArrayList<User> users);
    R<String> updateUser(User user);
    R<String> deleteUser(Long id);
    R<String> getNameById(Long id);
}

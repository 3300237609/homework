package com.example.homework.mapper;

import com.example.homework.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;

@Mapper
public interface UserMapper {
    User login(@Param("username") String username, @Param("password") String password);

    boolean register(User user);

    boolean getUserByUsername(String username);

    // UserMapper.java
    ArrayList<User> getUserList(
            @Param("role") String role,
            @Param("clazz") String clazz,
            @Param("offset") Integer offset,  // 提前计算的偏移量
            @Param("pageSize") Integer pageSize
    );

    boolean addUser(@Param("users") ArrayList<User> users);

    boolean updateUser(User user);

    boolean deleteUser(@Param("id") Long id);

    boolean checkIsTeacherById(@Param("id") Long id);
}

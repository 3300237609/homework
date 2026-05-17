package com.example.homework.mapper;

import com.example.homework.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface UserMapper {
    User login(@Param("username") String username, @Param("password") String password);

    boolean register(User user);

    boolean getUserByUsername(String username);

    // UserMapper.java
    ArrayList<User> getUserList(
            @Param("role") String role,
            @Param("clazzId") String clazzId,
            @Param("offset") Integer offset,  // 提前计算的偏移量
            @Param("pageSize") Integer pageSize
    );

    boolean addUser(@Param("users") ArrayList<User> users);

    boolean updateUser(User user);

    boolean deleteUser(@Param("id") Long id);

    boolean checkIsTeacherById(@Param("id") Long id);

    Integer getUserCount(
            @Param("role") String role,
            @Param("clazz") String clazz
    );

    String getNameById(@Param("id") Long id);
    // 根据用户id查班级id
    Long getClazzIdByUserId(@Param("userId") Long userId);
}

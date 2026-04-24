package com.example.homework.mapper;

import com.example.homework.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    User login(@Param("username") String username, @Param("password") String password);
    boolean register(User user);
    boolean getUserByUsername(String username);
}

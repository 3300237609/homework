package com.example.homework.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 账号
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 真实姓名
     */
    private String name;

    /**
     * 头像图片
     */
    private String img;

    /**
     * 角色ID 1-管理员 2-老师 3-学生
     */
    private Long roleId;

    /**
     * 班级ID（学生才有）
     */
    @JsonProperty("clazzId") // 强制指定JSON字段名
    private Long clazzId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
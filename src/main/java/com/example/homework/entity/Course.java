package com.example.homework.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Course {

    private Long id;
    private String courseName;  // 课程名称
    private Long teacherId;     // 授课教师ID
    private Long clazzId;       // 班级ID
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
package com.example.homework.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Homework {

    private Long id;
    private String title;        // 作业标题
    private String content;      // 作业要求
    private Long teacherId;      // 发布教师
    private Long clazzId;        // 班级ID
    private Long courseId;       // 课程ID
    private Integer scoreTotal;  // 满分
    private LocalDateTime startTime;  // 开始时间
    private LocalDateTime deadline;   // 截止时间
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
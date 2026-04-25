package com.example.homework.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Clazz {

    private Long id;
    private String clazzName;   // 班级名称
    private Long teacherId;     // 教师ID
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
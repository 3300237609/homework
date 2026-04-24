package com.example.homework.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ClassInfo {

    /**
     * 班级ID
     */
    private Long id;

    /**
     * 班级名称
     */
    private String className;

    /**
     * 授课老师ID
     */
    private Long teacherId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
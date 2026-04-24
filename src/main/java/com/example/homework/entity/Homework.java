package com.example.homework.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Homework {

    /**
     * 作业ID
     */
    private Long id;

    /**
     * 作业标题
     */
    private String title;

    /**
     * 作业内容/要求
     */
    private String content;

    /**
     * 发布老师ID
     */
    private Long teacherId;

    /**
     * 发布班级ID
     */
    private Long classId;

    /**
     * 截止时间
     */
    private LocalDateTime deadline;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
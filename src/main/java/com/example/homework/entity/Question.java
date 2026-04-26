package com.example.homework.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties({"handler", "hibernateLazyInitializer"})
public class Question {
    private Long id;
    private Long courseId;        // 课程ID
    private String title;         // 题干
    private String type;          // 单选/多选/判断/简答
    private String options;       // 选项 JSON
    private String answer;        // 标准答案
    private Integer score;        // 默认分值
    private String difficulty;    // 难度
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
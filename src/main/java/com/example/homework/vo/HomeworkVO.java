package com.example.homework.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class HomeworkVO {

    // 作业信息
    private Long id;
    private String title;        // 作业标题
    private String content;      // 作业描述
    private LocalDateTime startTime;  // 开始时间
    private LocalDateTime deadline;    // 截止时间
    private Integer totalScore;       // 总分/分数
    private String courseName;   // 课程名称
    private String clazzName;    // 班级名称
    private String status;       // 未提交 / 已提交(待批改) / 已批改
    private LocalDateTime submitTime; // 提交时间
}
package com.example.homework.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 智能发布作业请求参数
 * AI 按题型比例、难度、题量自动生成题目并发布
 */
@Data
public class SmartHomeworkDTO {

    private Long clazzId;                   // 班级ID
    private String content;                 // 作业要求/说明
    private Long courseId;                  // 课程ID
    private LocalDateTime deadline;         // 截止时间
    private String difficulty;              // 整体难度（简单/中等/困难）
    private Integer questionCount;          // 题目总数
    private String title;                   // 作业标题
    private Integer totalScore;             // 作业总分
    private Map<String, Integer> typePercent; // 各题型占比，如 {选择题:76,判断题:12,简答题:12}
}

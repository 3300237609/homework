package com.example.homework.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties({"handler", "hibernateLazyInitializer"})
public class HomeworkQuestion {
    private Long id;
    private Long homeworkId;      // 作业ID
    private Long questionId;      // 题库题目ID
    private Integer score;        // 该作业中本题分值
    private Integer sort;         // 排序
    private LocalDateTime createTime;

    // 非数据库字段：题目详情（查询时用）
    private transient Question question;
}
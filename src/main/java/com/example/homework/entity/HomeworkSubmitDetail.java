package com.example.homework.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties({"handler", "hibernateLazyInitializer"})
public class HomeworkSubmitDetail {
    private Long id;
    private Long submitId;        // 提交ID
    private Long questionId;      // 题目ID
    private String studentAnswer; // 学生答案
    private Integer score;        // 本题得分
    private String remark;        // 批改评语
    private LocalDateTime createTime;
}
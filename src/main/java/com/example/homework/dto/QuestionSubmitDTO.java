package com.example.homework.dto;

import lombok.Data;

@Data
public class QuestionSubmitDTO {
    private Long homeworkId;    // 作业ID
    private Long questionId;    // 题目ID
    private String studentAnswer; // 学生答案
}
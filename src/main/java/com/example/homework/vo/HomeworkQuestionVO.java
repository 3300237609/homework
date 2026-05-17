package com.example.homework.vo;

import lombok.Data;

@Data
public class HomeworkQuestionVO {
    private Long questionId;
    private Long homeworkId;
    private String title;
    private Integer questionScore;
    private Integer questionScoreGot;
    private String questionType;
    private String options;
    private String studentAnswer;
    private String correctAnswer;
    private String questionSubmitStatus;
}
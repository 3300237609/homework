package com.example.homework.vo;

import lombok.Data;

@Data
public class QuestionCorrectionVO {
    private Long questionId;
    private String title;
    private String type;
    private String options;
    private String correctAnswer;
    private Integer fullScore;
    private String studentAnswer;
    private Integer studentScore;
    private String comment;
}
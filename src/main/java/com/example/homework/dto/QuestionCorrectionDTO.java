package com.example.homework.dto;

import lombok.Data;

@Data
public class QuestionCorrectionDTO {
    private Long questionId;
    private Integer score;
    private String comment;
}
package com.example.homework.vo;

import lombok.Data;
import java.util.List;

@Data
public class StudentSubmitVO {
    private Long submitId;
    private Long studentId;
    private String studentName;
    private List<QuestionCorrectionVO> questions;
}
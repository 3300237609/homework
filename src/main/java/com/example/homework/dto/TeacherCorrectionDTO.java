package com.example.homework.dto;

import lombok.Data;
import java.util.List;

@Data
public class TeacherCorrectionDTO {
    private Long homeworkId;
    private Long submitId;
    private Integer totalScore;
    private List<QuestionCorrectionDTO> corrections;
}
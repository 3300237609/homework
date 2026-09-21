package com.example.homework.vo;

import lombok.Data;
import java.util.List;

@Data
public class HomeworkPendingCorrectionVO {
    private Long homeworkId;
    private String homeworkTitle;
    private String homeworkContent;
    private String courseName;
    private String clazzName;
    private List<StudentSubmitVO> studentList;
}
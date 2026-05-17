package com.example.homework.vo;

import lombok.Data;
import java.util.List;

@Data
public class HomeworkDetailVO {
    private Long homeworkId;
    private String title;
    private String requirement;
    private Integer fullScore;
    private String subject;
    private String homeworkStatus;
    private List<HomeworkQuestionVO> questionList; // 引用独立类
}
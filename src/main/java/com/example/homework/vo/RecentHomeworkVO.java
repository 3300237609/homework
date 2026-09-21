package com.example.homework.vo;

import lombok.Data;

@Data
public class RecentHomeworkVO {
    private String title;
    private String className;
    private String subject;
    private String deadline;
    private String submitInfo;
    private Double avgScore;
    private String status;
    private Long submitNum;
    private Long totalStudent;
}
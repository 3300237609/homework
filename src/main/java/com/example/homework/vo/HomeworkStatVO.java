package com.example.homework.vo;

import lombok.Data;

import java.util.List;

@Data
public class HomeworkStatVO {
    private Long totalHomework;
    private Long totalStudent;
    private Double submitRate;
    private Double avgScore;

    private List<HomeworkBarVO> barList;
    private List<HomeworkLineVO> lineList;
    private List<RecentHomeworkVO> recentHomeworkList;
}
package com.example.homework.vo;

import lombok.Data;
import java.util.List;

/**
 * VO：作业作答查询结果，返回前端
 */
@Data
public class HomeworkAnswerVO {
    // 作业基础信息
    private Long homeworkId;
    private String homeworkTitle;
    private Integer totalScore;

    // 班级学生作答列表
    private List<StudentAnswerVO> studentList;

    @Data
    public static class StudentAnswerVO {
        private Long studentId;
        private String studentName;
        // 是否作答 true=已提交，false=未提交
        private Boolean isSubmit;
        // 作业得分，未提交默认0
        private Integer totalScore;
        private String submitTime;
        private String submitStatus;
    }
}
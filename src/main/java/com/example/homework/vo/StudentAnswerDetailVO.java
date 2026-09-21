package com.example.homework.vo;

import lombok.Data;
import java.util.List;

/**
 * VO：学生作业作答详情（含顶部元信息 + 题目列表）
 * 用于作答详情页展示
 */
@Data
public class StudentAnswerDetailVO {
    // ===== 顶部元信息 =====
    private String homeworkTitle;   // 作业标题
    private String studentName;     // 学生姓名
    private String studentNumber;   // 学号（用 username 字段）
    private String submitTime;      // 提交时间
    private Integer totalScore;     // 得分
    private Integer fullScore;     // 满分

    // ===== 题目列表 =====
    private List<QuestionCorrectionVO> questions;
}

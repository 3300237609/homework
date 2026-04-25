package com.example.homework.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class HomeworkSubmit {

    private Long id;
    private Long homeworkId;     // 作业ID
    private Long studentId;      // 学生ID
    private String content;      // 提交内容
    private String fileUrl;      // 文件地址
    private Integer score;       // 得分
    private String remark;       // 评语
    private Integer status;      // 0未批改 1已批改
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
package com.example.homework.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class HomeworkSubmit {

    /**
     * 提交记录ID
     */
    private Long id;

    /**
     * 作业ID
     */
    private Long homeworkId;

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 提交内容
     */
    private String content;

    /**
     * 附件地址
     */
    private String fileUrl;

    /**
     * 得分
     */
    private Integer score;

    /**
     * 老师评语
     */
    private String remark;

    /**
     * 状态 1-已提交 2-已批改
     */
    private Integer status;

    /**
     * 提交时间
     */
    private LocalDateTime createTime;

    /**
     * 更新/批改时间
     */
    private LocalDateTime updateTime;
}
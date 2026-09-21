package com.example.homework.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 作业分析报告记录表 homework_report
 */
@Data
public class HomeworkReport {

    private Long id;                  // 主键ID
    private Long homeworkId;          // 作业ID
    private String content;           // 报告内容（Markdown）
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
}

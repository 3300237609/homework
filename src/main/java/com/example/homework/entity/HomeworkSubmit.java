package com.example.homework.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties({"handler", "hibernateLazyInitializer"})
public class HomeworkSubmit {
    private Long id;
    private Long homeworkId;
    private Long studentId;
    private Integer totalScore;   // 总得分
    private String status;        // 未批改/已批改
    private String content;       // 附件/备注
    private LocalDateTime submitTime;
}
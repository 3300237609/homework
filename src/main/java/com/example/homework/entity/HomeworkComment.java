package com.example.homework.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class HomeworkComment {
    private Long id;
    private Long submitDetailId;
    private String comment;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
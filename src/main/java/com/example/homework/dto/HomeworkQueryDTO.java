package com.example.homework.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

/**
 * DTO：接收前端查询作业作答情况的请求参数
 */
@Data
public class HomeworkQueryDTO {
    @NotNull(message = "作业id不能为空")
    private Long homeworkId;

    @NotNull(message = "学生id不能为空")
    private Long studentId;
}
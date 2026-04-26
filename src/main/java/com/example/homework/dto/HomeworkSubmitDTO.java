package com.example.homework.dto;

import com.example.homework.entity.HomeworkSubmitDetail;
import lombok.Data;
import java.util.List;

@Data
public class HomeworkSubmitDTO {
    private Long homeworkId;            // 作业ID（必传）
    private String content;             // 备注/说明（可不传）
    private List<HomeworkSubmitDetail> answerList; // 答案列表（必传）
}
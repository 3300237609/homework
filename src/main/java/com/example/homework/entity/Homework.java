package com.example.homework.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties({"handler", "hibernateLazyInitializer"})
public class Homework {
    private Long id;
    private String title;
    private String content;
    private Integer totalScore;
    private Long teacherId;
    private Long clazzId;
    private Long courseId;
    private LocalDateTime startTime;
    private LocalDateTime deadline;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private Integer totalStudent;    // 该班级总人数
    private Integer submitCount;    // 已提交人数
    private Integer unSubmitCount;  // 未提交人数
    private Integer correctedCount; // 已批改人数
    private Integer unCorrectCount;  // 未批改人数

    // 非数据库字段：作业包含的题目
    private transient List<HomeworkQuestion> questionList;

    // 扩展数据Map，用来存提交统计、提交状态等
    private Map<String, Object> extendData = new HashMap<>();

    // 添加扩展数据的方法
    public void addExtendData(String key, Object value) {
        if (this.extendData == null) {
            this.extendData = new HashMap<>();
        }
        this.extendData.put(key, value);
    }
}
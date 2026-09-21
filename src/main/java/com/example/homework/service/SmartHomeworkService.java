package com.example.homework.service;

import com.example.homework.common.R;
import com.example.homework.dto.SmartHomeworkDTO;

/**
 * 智能发布作业业务层接口
 * AI 按要求自动生成题目并完成作业发布
 */
public interface SmartHomeworkService {

    /**
     * 智能生成题目并发布作业
     *
     * @param dto 作业要求（班级、课程、难度、题量、题型占比、总分等）
     * @return 发布结果
     */
    R<String> smartPublish(SmartHomeworkDTO dto);
}

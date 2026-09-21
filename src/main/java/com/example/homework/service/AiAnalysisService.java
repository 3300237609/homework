package com.example.homework.service;

import com.example.homework.common.R;

/**
 * AI 作业分析业务层接口
 * 通过 Function Calling 查询数据库，对作业完成情况进行分析并给出建议
 */
public interface AiAnalysisService {

    /**
     * 根据作业ID分析作业整体情况并给出教学建议
     *
     * @param homeworkId 作业ID
     * @return Markdown 格式的分析报告
     */
    R<String> analyzeHomework(Long homeworkId);
}

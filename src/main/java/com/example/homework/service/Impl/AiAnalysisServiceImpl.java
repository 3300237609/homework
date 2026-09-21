package com.example.homework.service.Impl;

import com.example.homework.common.R;
import com.example.homework.constant.AiConstants;
import com.example.homework.entity.HomeworkReport;
import com.example.homework.mapper.HomeworkReportMapper;
import com.example.homework.service.AiAnalysisService;
import com.example.homework.tool.HomeworkAnalysisTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * AI 作业分析业务层实现
 * ChatClient 携带 @Tool 工具，由模型自动决定调用哪些工具查询数据库
 */
@Service
public class AiAnalysisServiceImpl implements AiAnalysisService {

    private final ChatClient chatClient;

    @Autowired
    private HomeworkAnalysisTools homeworkAnalysisTools;
    @Autowired
    private HomeworkReportMapper homeworkReportMapper;

    public AiAnalysisServiceImpl(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public R<String> analyzeHomework(Long homeworkId) {
        // 1. 参数校验
        if (homeworkId == null) {
            return R.error("作业ID不能为空！");
        }

        // 2. 调用大模型：携带工具集，模型自动 Function Calling 查询真实数据
        String report = chatClient.prompt()
                .system(AiConstants.HOMEWORK_ANALYSIS_PROMPT)
                .user("请对作业ID为 " + homeworkId
                        + " 的这份作业进行全面分析。请务必调用工具查询数据库中的真实数据，" +
                        "在此基础上分析作业完成情况、学生成绩与薄弱环节，并给出具体可行的教学改进建议。")
                .tools(homeworkAnalysisTools)
                .call()
                .content();

        // 3. 兜底校验：报告为空则不落库
        if (report == null || report.isBlank()) {
            return R.error("分析报告生成失败，请稍后重试！");
        }

        // 4. 落库：该作业已有分析记录则更新，没有则新增
        HomeworkReport existing = homeworkReportMapper.selectByHomeworkId(homeworkId);
        if (existing == null) {
            HomeworkReport homeworkReport = new HomeworkReport();
            homeworkReport.setHomeworkId(homeworkId);
            homeworkReport.setContent(report);
            homeworkReportMapper.insert(homeworkReport);
        } else {
            existing.setContent(report);
            homeworkReportMapper.updateByHomeworkId(existing);
        }

        return R.success(report);
    }
}

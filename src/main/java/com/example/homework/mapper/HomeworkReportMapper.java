package com.example.homework.mapper;

import com.example.homework.entity.HomeworkReport;
import org.apache.ibatis.annotations.Mapper;

/**
 * 作业分析报告 Mapper
 */
@Mapper
public interface HomeworkReportMapper {

    // 根据作业ID查询分析报告
    HomeworkReport selectByHomeworkId(Long homeworkId);

    // 新增分析报告
    int insert(HomeworkReport homeworkReport);

    // 根据作业ID更新分析报告内容
    int updateByHomeworkId(HomeworkReport homeworkReport);
}

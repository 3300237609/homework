package com.example.homework.mapper;

import com.example.homework.vo.HomeworkBarVO;
import com.example.homework.vo.HomeworkLineVO;
import com.example.homework.vo.RecentHomeworkVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface HomeworkStatMapper {
    Long getTotalHomework(Long teacherId);
    Long getTotalStudent(Long teacherId);
    Map<String, Object> getSubmitTotal(Long teacherId);
    Double getAvgScore(Long teacherId);
    List<HomeworkBarVO> getBarData(Long teacherId);
    List<HomeworkLineVO> getLineData(Long teacherId);
    List<RecentHomeworkVO> getRecentHomework(Long teacherId);
}
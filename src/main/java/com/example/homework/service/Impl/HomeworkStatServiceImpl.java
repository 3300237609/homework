package com.example.homework.service.Impl;

import com.example.homework.common.R;
import com.example.homework.common.UserContextHolder;
import com.example.homework.mapper.HomeworkStatMapper;
import com.example.homework.service.HomeworkStatService;
import com.example.homework.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class HomeworkStatServiceImpl implements HomeworkStatService {

    @Autowired
    private HomeworkStatMapper homeworkStatMapper;

    @Override
    public R<HomeworkStatVO> getDashboardData() {
        Long teacherId = Long.valueOf(UserContextHolder.getUserId());
        HomeworkStatVO vo = new HomeworkStatVO();

        // ===================== 卡片数据 =====================
        vo.setTotalHomework(homeworkStatMapper.getTotalHomework(teacherId));
        vo.setTotalStudent(homeworkStatMapper.getTotalStudent(teacherId));

        Map<String, Object> submitMap = homeworkStatMapper.getSubmitTotal(teacherId);

        Number submitTotal = (Number) submitMap.getOrDefault("submitTotal", 0L);
        Number shouldTotal = (Number) submitMap.getOrDefault("shouldTotal", 1L);

        double rate = Math.round(submitTotal.longValue() * 1000.0 / shouldTotal.longValue()) / 10.0;
        vo.setSubmitRate(rate);

        Double avg = homeworkStatMapper.getAvgScore(teacherId);
        vo.setAvgScore(avg == null ? 0.0 : Math.round(avg * 10) / 10.0);

        // ===================== 图表 =====================
        vo.setBarList(homeworkStatMapper.getBarData(teacherId));
        vo.setLineList(homeworkStatMapper.getLineData(teacherId));

        // ===================== 最近作业列表 =====================
        List<RecentHomeworkVO> list = homeworkStatMapper.getRecentHomework(teacherId);
        for (RecentHomeworkVO v : list) {
            v.setSubmitInfo(v.getSubmitNum() + "/" + v.getTotalStudent());
        }
        vo.setRecentHomeworkList(list);

        return R.success(vo);
    }
}
package com.example.homework.service.Impl;

import com.example.homework.common.R;
import com.example.homework.dto.QuestionCorrectionDTO;
import com.example.homework.dto.TeacherCorrectionDTO;
import com.example.homework.mapper.HomeworkCorrectionMapper;
import com.example.homework.mapper.HomeworkSubmitDetailMapper;
import com.example.homework.mapper.HomeworkSubmitMapper;
import com.example.homework.service.CorrectionService;
import com.example.homework.vo.HomeworkPendingCorrectionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CorreectionServiceImpl implements CorrectionService {
    @Autowired
    HomeworkCorrectionMapper homeworkCorrectionMapper;
    @Autowired
    HomeworkSubmitMapper homeworkSubmitMapper;
    @Autowired
    HomeworkSubmitDetailMapper homeworkSubmitDetailMapper;

    @Override
    public HomeworkPendingCorrectionVO getPendingCorrection(Long homeworkId) {
        return homeworkCorrectionMapper.getPendingCorrectionByHomeworkId(homeworkId);
    }

    @Transactional
    public R<String> saveCorrection(TeacherCorrectionDTO dto) {
        Long submitId = dto.getSubmitId();
        Integer addScore = dto.getTotalScore(); // 前端传的是【要加的分数】
        List<QuestionCorrectionDTO> corrections = dto.getCorrections();

        // 1. 遍历批改每题
        for (QuestionCorrectionDTO c : corrections) {
            Long questionId = c.getQuestionId();
            Integer score = c.getScore();
            String comment = c.getComment();

            // 获取提交详情ID
            Long detailId = homeworkSubmitDetailMapper.getDetailId(submitId, questionId);
            if (detailId == null) continue;

            // 更新题目分数
            homeworkSubmitDetailMapper.updateDetailScore(detailId, score);

            // 空评语不保存
            if (comment != null && !comment.trim().isEmpty()) {
                homeworkCorrectionMapper.insertComment(detailId, comment);
            }
        }

        // 2. 主表分数：原来的分数 + 前端传的分数（累加）
        homeworkSubmitMapper.addScoreToSubmit(submitId, addScore);

        return R.success("批改成功");
    }
}

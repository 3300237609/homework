package com.example.homework.service.Impl;

import com.example.homework.common.R;
import com.example.homework.common.UserContextHolder;
import com.example.homework.dto.QuestionSubmitDTO;
import com.example.homework.entity.HomeworkQuestion;
import com.example.homework.entity.HomeworkSubmit;
import com.example.homework.entity.HomeworkSubmitDetail;
import com.example.homework.entity.Question;
import com.example.homework.mapper.HomeworkQuestionMapper;
import com.example.homework.mapper.HomeworkSubmitDetailMapper;
import com.example.homework.mapper.HomeworkSubmitMapper;
import com.example.homework.mapper.QuestionMapper;
import com.example.homework.service.HomeworkSubmitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HomeworkSubmitServiceImpl implements HomeworkSubmitService {
    @Autowired
    HomeworkSubmitMapper homeworkSubmitMapper;
    @Autowired
    HomeworkSubmitDetailMapper homeworkSubmitDetailMapper;
    @Autowired
    HomeworkQuestionMapper homeworkQuestionMapper;
    @Autowired
    QuestionMapper questionMapper;

    @Override
    @Transactional  // 事务已加
    public R<String> submitQuestion(QuestionSubmitDTO dto) {
        Long homeworkId = Long.valueOf(dto.getHomeworkId());
        Long questionId = Long.valueOf(dto.getQuestionId());
        String studentAnswer = dto.getStudentAnswer();
        Long studentId = Long.valueOf(UserContextHolder.getUserId());

        HomeworkSubmit submit = homeworkSubmitMapper.selectByHomeworkAndStudent(homeworkId, studentId);
        if (submit == null) {
            submit = new HomeworkSubmit();
            submit.setHomeworkId(homeworkId);
            submit.setStudentId(studentId);
            submit.setStatus("未提交");
            submit.setTotalScore(0);
            homeworkSubmitMapper.insert(submit);
        }

        Long submitId = submit.getId();

        HomeworkSubmitDetail detail = homeworkSubmitDetailMapper.selectBySubmitAndQuestion(submitId, questionId);

        if (detail == null) {
            detail = new HomeworkSubmitDetail();
            detail.setSubmitId(submitId);
            detail.setQuestionId(questionId);
            detail.setStudentAnswer(studentAnswer);
            detail.setQuestionSubmitStatus("已提交");
            detail.setScore(0);
            homeworkSubmitDetailMapper.insert(detail);
        } else {
            detail.setStudentAnswer(studentAnswer);
            detail.setQuestionSubmitStatus("已提交");
            homeworkSubmitDetailMapper.updateById(detail);
        }

        return R.success("ok");
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> submitWork(Long homeworkId) {
        // 1. 获取当前登录学生ID
        Long studentId = Long.valueOf(UserContextHolder.getUserId());

        // 2. 查询作业提交主表
        HomeworkSubmit submit = homeworkSubmitMapper.selectByHomeworkAndStudent(homeworkId, studentId);
        if (submit == null) {
            return R.error("未找到该作业的提交记录");
        }
        Long submitId = submit.getId();

        // 3. 查询该学生所有题目的提交详情
        List<HomeworkSubmitDetail> detailList = homeworkSubmitDetailMapper.selectBySubmitId(submitId);
        if (detailList == null || detailList.isEmpty()) {
            return R.error("未找到答题记录");
        }

        int totalScore = 0;
        boolean hasShortAnswer = false; // 标记是否有简答题

        // 4. 遍历每一道题 → 比对答案 → 算分
        for (HomeworkSubmitDetail detail : detailList) {
            Long questionId = detail.getQuestionId();
            String studentAnswer = detail.getStudentAnswer();

            // 5. 从作业-题目中间表，获取本题满分
            HomeworkQuestion homeworkQuestion = homeworkQuestionMapper.selectByHomeworkAndQuestion(homeworkId, questionId);
            if (homeworkQuestion == null) {
                continue;
            }
            Integer questionFullScore = homeworkQuestion.getScore();

            // 6. 去题目表，获取题目类型和标准答案
            Question question = questionMapper.selectById(questionId);
            if (question == null) {
                continue;
            }
            String correctAnswer = question.getAnswer();
            String questionType = question.getType();

            // 7. 计算本题得分
            int finalScore = 0;
            if ("单选".equals(questionType) || "判断".equals(questionType)) {
                // 客观题：自动判分
                if (studentAnswer != null && studentAnswer.equals(correctAnswer)) {
                    finalScore = questionFullScore;
                }
                detail.setQuestionSubmitStatus("已批改");
            } else {
                // 主观题（简答/其他）
                hasShortAnswer = true; // 标记存在简答题
                finalScore = 0;
                detail.setQuestionSubmitStatus("已提交（待批改）");
            }

            totalScore += finalScore;
            detail.setScore(finalScore);
            homeworkSubmitDetailMapper.updateById(detail);
        }

        // 8. 更新作业主表：总分 + 智能判断状态
        submit.setTotalScore(totalScore);
        if (hasShortAnswer) {
            // 有简答题 → 待批改
            submit.setStatus("已提交（待批改）");
        } else {
            // 没有简答题 → 全部自动批改完成
            submit.setStatus("已批改");
        }
        homeworkSubmitMapper.updateById(submit);

        return R.success("作业提交成功");
    }
}


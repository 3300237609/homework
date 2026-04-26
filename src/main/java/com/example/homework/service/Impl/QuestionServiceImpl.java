package com.example.homework.service.Impl;

import com.example.homework.common.R;
import com.example.homework.common.UserContextHolder;
import com.example.homework.entity.HomeworkQuestion;
import com.example.homework.entity.Question;
import com.example.homework.mapper.HomeworkQuestionMapper;
import com.example.homework.mapper.QuestionMapper;
import com.example.homework.service.QuestionService;
import com.example.homework.utils.PermissionUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 题库业务层实现类
 * 实现题目管理的增删改查业务逻辑
 */
@Service
public class QuestionServiceImpl implements QuestionService {

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private HomeworkQuestionMapper homeworkQuestionMapper;

    /**
     * 添加单个题目
     * 仅教师/管理员可操作
     */
    @Override
    public R<String> addQuestion(Question question) {
        // 1. 权限校验
        if (!PermissionUtil.isAdminOrTeacher()) {
            return R.error("权限不足！仅教师可添加题目");
        }

        // 2. 参数校验
        if (!StringUtils.hasText(question.getTitle())
                || !StringUtils.hasText(question.getType())
                || question.getCourseId() == null
                || question.getScore() == null) {
            return R.error("题目信息不完整！标题、题型、课程、分值不能为空");
        }

        // 客观题必须有答案
        if (("单选".equals(question.getType()) || "多选".equals(question.getType()) || "判断".equals(question.getType()))
                && !StringUtils.hasText(question.getAnswer())) {
            return R.error("客观题必须填写标准答案！");
        }

        // 4. 保存
        int result = questionMapper.insert(question);
        if (result <= 0) {
            return R.error("题目添加失败！");
        }

        return R.success("题目添加成功！");
    }

    /**
     * 批量添加题目
     * 事务保证全部成功或全部失败
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> batchAddQuestion(List<Question> questionList) {
        if (questionList == null || questionList.isEmpty()) {
            return R.error("题目列表不能为空！");
        }

        // 权限校验
        if (!PermissionUtil.isAdminOrTeacher()) {
            return R.error("权限不足！仅教师可批量添加题目");
        }

        LocalDateTime now = LocalDateTime.now();
        Long userId = Long.valueOf(UserContextHolder.getUserId());

        for (Question question : questionList) {
            // 基础校验
            if (!StringUtils.hasText(question.getTitle())
                    || !StringUtils.hasText(question.getType())
                    || question.getCourseId() == null
                    || question.getScore() == null) {
                throw new RuntimeException("批量添加失败：存在题目信息不完整");
            }

            question.setCreateTime(now);
            question.setUpdateTime(now);
            questionMapper.insert(question);
        }

        return R.success("批量添加题目成功，共添加：" + questionList.size() + " 题");
    }

    /**
     * 分页条件查询题目列表
     * 支持：课程ID、题型、关键词搜索
     */
    @Override
    public R<List<Question>> getQuestionList(Long courseId, String type, String keyword, Integer pageNum, Integer pageSize) {
        // 权限校验
        if (!PermissionUtil.isAdminOrTeacher()) {
            return R.error("权限不足！仅教师可查看题库");
        }

        // 分页
        PageHelper.startPage(pageNum, pageSize);
        List<Question> questionList = questionMapper.selectListByCondition(courseId, type, keyword);

        PageInfo<Question> pageInfo = new PageInfo<>(questionList);
        return R.success(questionList)
                .add("total", pageInfo.getTotal())
                .add("pageNum", pageInfo.getPageNum())
                .add("pageSize", pageInfo.getPageSize())
                .add("pages", pageInfo.getPages());
    }

    /**
     * 查询题目详情
     */
    @Override
    public R<Question> getQuestionDetail(Long questionId) {
        if (questionId == null) {
            return R.error("题目ID不能为空！");
        }

        Question question = questionMapper.selectById(questionId);
        if (question == null) {
            return R.error("题目不存在！");
        }

        return R.success(question);
    }

    /**
     * 修改题目
     * 仅创建者/管理员可修改
     */
    @Override
    public R<String> updateQuestion(Question question) {
        if (question.getId() == null) {
            return R.error("题目ID不能为空！");
        }

        // 查询原题目
        Question oldQuestion = questionMapper.selectById(question.getId());
        if (oldQuestion == null) {
            return R.error("题目不存在！");
        }


        // 校验必填项
        if (!StringUtils.hasText(question.getTitle())
                || !StringUtils.hasText(question.getType())) {
            return R.error("题目标题、题型不能为空！");
        }

        // 封装更新字段
        Question updateQuestion = new Question();
        updateQuestion.setId(question.getId());
        updateQuestion.setTitle(question.getTitle());
        updateQuestion.setType(question.getType());
        updateQuestion.setOptions(question.getOptions());
        updateQuestion.setAnswer(question.getAnswer());
        updateQuestion.setScore(question.getScore());
        updateQuestion.setCourseId(question.getCourseId());
        updateQuestion.setUpdateTime(LocalDateTime.now());

        int result = questionMapper.update(updateQuestion);
        if (result <= 0) {
            return R.error("题目修改失败！");
        }

        return R.success("题目修改成功！");
    }

    /**
     * 删除题目
     * 若题目已被作业引用，则不允许删除
     */
    @Override
    public R<String> deleteQuestion(Long questionId) {
        if (questionId == null) {
            return R.error("题目ID不能为空！");
        }

        Question question = questionMapper.selectById(questionId);
        if (question == null) {
            return R.error("题目不存在！");
        }


        // 判断是否被作业引用
        List<HomeworkQuestion> relateList = homeworkQuestionMapper.selectByQuestionId(questionId);
        if (!relateList.isEmpty()) {
            return R.error("该题目已被作业引用，无法删除！");
        }

        // 执行删除
        int result = questionMapper.delete(questionId);
        if (result <= 0) {
            return R.error("题目删除失败！");
        }

        return R.success("题目删除成功！");
    }

    /**
     * 根据课程ID获取所有题目（用于作业发布时选择题目）
     */
    @Override
    public R<List<Question>> getQuestionByCourseId(Long courseId) {
        if (courseId == null) {
            return R.error("课程ID不能为空！");
        }

        List<Question> list = questionMapper.selectListByCourseId(courseId);
        return R.success(list);
    }
}
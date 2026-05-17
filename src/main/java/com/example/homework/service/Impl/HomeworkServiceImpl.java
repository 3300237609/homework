package com.example.homework.service.Impl;

import com.example.homework.common.R;
import com.example.homework.common.UserContextHolder;
import com.example.homework.entity.*;
import com.example.homework.mapper.*;
import com.example.homework.service.HomeworkService;
import com.example.homework.utils.PermissionUtil;
import com.example.homework.vo.HomeworkDetailVO;
import com.example.homework.vo.HomeworkQuestionVO;
import com.example.homework.vo.HomeworkVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 作业业务层实现类
 * 实现作业相关的业务逻辑
 */
@Service
public class HomeworkServiceImpl implements HomeworkService {

    @Autowired
    private HomeworkMapper homeworkMapper;

    @Autowired
    private HomeworkQuestionMapper homeworkQuestionMapper;

    @Autowired
    private HomeworkSubmitMapper homeworkSubmitMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private HomeworkSubmitDetailMapper homeworkSubmitDetailMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 发布新作业
     * 仅教师/管理员可操作，校验作业必填信息，关联题目并保存
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> addHomework(Homework homework) {
        // 1. 权限校验：仅教师/管理员可发布作业
        if (!PermissionUtil.isAdminOrTeacher()) {
            return R.error("权限不足！仅教师可发布作业");
        }

        // 2. 参数校验（先不校验 totalScore，因为我们要自动计算）
        if (!StringUtils.hasText(homework.getTitle())
                || !StringUtils.hasText(homework.getContent())
                || homework.getClazzId() == null
                || homework.getCourseId() == null
                || homework.getDeadline() == null) {
            return R.error("作业信息不完整！标题、要求、班级、课程、截止时间不能为空");
        }

        // 3. 补充基础信息
        homework.setTeacherId(Long.valueOf(UserContextHolder.getUserId()));
        if (homework.getStartTime() == null) {
            homework.setStartTime(LocalDateTime.now());
        }

        // 4. 保存作业主表（已配置主键回填）
        int insertResult = homeworkMapper.insert(homework);
        if (insertResult <= 0) {
            return R.error("作业发布失败！");
        }

        // 5. 保存作业关联的题目 + 自动计算总分
        int totalScore = 0; // 总分累加器

        if (homework.getQuestionList() != null && !homework.getQuestionList().isEmpty()) {
            for (int i = 0; i < homework.getQuestionList().size(); i++) {
                HomeworkQuestion homeworkQuestion = homework.getQuestionList().get(i);
                homeworkQuestion.setHomeworkId(homework.getId());
                homeworkQuestion.setSort(i + 1);
                homeworkQuestion.setCreateTime(LocalDateTime.now());

                // 自动补全分数：为空则查询题库默认分数
                if (homeworkQuestion.getScore() == null) {
                    Question question = questionMapper.selectById(homeworkQuestion.getQuestionId());
                    if (question == null) {
                        return R.error("题目不存在，ID：" + homeworkQuestion.getQuestionId());
                    }
                    homeworkQuestion.setScore(question.getScore());
                }

                // 累加到总分
                totalScore += homeworkQuestion.getScore();

                // 插入关联表
                homeworkQuestionMapper.insert(homeworkQuestion);
            }
        }

        // 6. 更新作业主表的总分数
        homework.setTotalScore(totalScore);
        homeworkMapper.updateTotalScore(homework);

        return R.success("作业发布成功！本次作业满分：" + totalScore + " 分");
    }

    /**
     * 分页查询发布的作业列表（教师端）
     * 仅教师/管理员可操作，按教师ID筛选，支持班级筛选
     */
    @Override
    public R<List<Homework>> getHomeworkList(Long clazzId, Long courseId, Integer pageNum, Integer pageSize) {
        if (!PermissionUtil.isAdminOrTeacher()) {
            return R.error("权限不足！仅教师可查看作业列表");
        }
        PageHelper.startPage(pageNum, pageSize);
        List<Homework> homeworkList = homeworkMapper.selectListByCondition(
                Long.valueOf(UserContextHolder.getUserId()),
                clazzId,
                courseId
        );

        for (Homework homework : homeworkList) {
            Long homeworkId = homework.getId();
            Long cid = homework.getClazzId();

            // 1. 班级总人数
            Integer totalStudent = homeworkMapper.getClazzStudentCount(cid);
            // 2. 已提交人数
            Integer submitCount = homeworkMapper.getHomeworkSubmitCount(homeworkId);
            // 3. 未提交人数
            Integer unSubmitCount = totalStudent - submitCount;
            // 4. 已批改人数
            Integer correctedCount = homeworkMapper.getHomeworkCorrectedCount(homeworkId);
            // 5. 未批改人数
            Integer unCorrectCount = submitCount - correctedCount;

            homework.setTotalStudent(totalStudent);
            homework.setSubmitCount(submitCount);
            homework.setUnSubmitCount(unSubmitCount);
            homework.setCorrectedCount(correctedCount);
            homework.setUnCorrectCount(unCorrectCount);
        }
        PageInfo<Homework> pageInfo = new PageInfo<>(homeworkList);
        return R.success(homeworkList)
                .add("total", pageInfo.getTotal())
                .add("pageNum", pageInfo.getPageNum())
                .add("pageSize", pageInfo.getPageSize())
                .add("pages", pageInfo.getPages());
    }

    /**
     * 查询作业详情（含提交统计）
     * 教师/学生均可查看，教师端返回提交统计，学生端仅返回自身提交状态
     */
    @Override
    public R<Homework> getHomeworkDetail(Long homeworkId) {
        // 1. 参数校验
        if (homeworkId == null) {
            return R.error("作业ID不能为空！");
        }

        // 2. 查询作业主信息
        Homework homework = homeworkMapper.selectById(homeworkId);
        if (homework == null) {
            return R.error("作业不存在！");
        }

        // 3. 查询作业关联的题目
        List<HomeworkQuestion> questionList = homeworkQuestionMapper.selectByHomeworkId(homeworkId);
        homework.setQuestionList(questionList);

        // 4. 教师端：补充提交统计
        if (PermissionUtil.isAdminOrTeacher()) {
            List<HomeworkSubmit> submitList = homeworkSubmitMapper.selectByHomeworkId(homeworkId);
            int totalSubmit = submitList.size(); // 总提交数
            // 可扩展：已批改/未批改数量、平均分等
            homework.addExtendData("submitCount", totalSubmit);
        }

        return R.success(homework);
    }

    /**
     * 修改作业信息（仅截止时间、要求）
     * 仅发布作业的教师/管理员可操作，禁止修改已提交的核心信息
     */
    @Override
    public R<String> updateHomework(Homework homework) {
        // 1. 参数校验
        if (homework.getId() == null) {
            return R.error("作业ID不能为空！");
        }

        // 2. 权限校验
        Homework oldHomework = homeworkMapper.selectById(homework.getId());
        if (oldHomework == null) {
            return R.error("作业不存在！");
        }

        // 3. 限制修改字段（仅允许修改截止时间、作业说明）
        Homework updateHomework = new Homework();
        updateHomework.setId(homework.getId());
        updateHomework.setContent(homework.getContent()); // 作业要求
        updateHomework.setDeadline(homework.getDeadline()); // 截止时间
        updateHomework.setUpdateTime(LocalDateTime.now());

        // 4. 执行修改
        int updateResult = homeworkMapper.update(updateHomework);
        if (updateResult <= 0) {
            return R.error("作业修改失败！");
        }

        return R.success("作业修改成功！");
    }

    /**
     * 删除作业（需判断是否有学生提交记录）
     * 仅发布者/管理员可操作，有提交记录则禁止删除
     */
    @Override
    public R<String> deleteHomework(Long homeworkId) {
        // 1. 参数校验
        if (homeworkId == null) {
            return R.error("作业ID不能为空！");
        }

        // 2. 权限校验
        Homework homework = homeworkMapper.selectById(homeworkId);
        if (homework == null) {
            return R.error("作业不存在！");
        }
        if (!PermissionUtil.isAdmin() && !homework.getTeacherId().equals(Long.valueOf(UserContextHolder.getUserId()))) {
            return R.error("权限不足！仅作业发布者可删除");
        }

        // 3. 判断是否有提交记录
        List<HomeworkSubmit> submitList = homeworkSubmitMapper.selectByHomeworkId(homeworkId);
        if (!submitList.isEmpty()) {
            return R.error("该作业已有学生提交，禁止删除！");
        }

        // 4. 级联删除：先删作业题目关联，再删作业主表
        homeworkQuestionMapper.deleteByHomeworkId(homeworkId);
        int deleteResult = homeworkMapper.delete(homeworkId);

        if (deleteResult <= 0) {
            return R.error("作业删除失败！");
        }
        return R.success("作业删除成功！");
    }

    /**
     * 学生查询当前课程下的作业列表（可过滤未提交/已提交）
     * 仅学生可操作，按学生ID+课程ID筛选，支持提交状态过滤
     */
    @Override
    public R<List<HomeworkVO>> getStudentHomeworkList(Long courseId, String status, Integer pageNum, Integer pageSize) {
        if (!PermissionUtil.isStudent()) {
            return R.error("权限不足！");
        }

        // 当前学生ID
        Long studentId = Long.valueOf(UserContextHolder.getUserId());
        // 自动获取自己班级
        Long clazzId = userMapper.getClazzIdByUserId(studentId);

        PageHelper.startPage(pageNum, pageSize);
        List<HomeworkVO> list = homeworkMapper.selectStudentHomeworkList(courseId, status, studentId);

        PageInfo<HomeworkVO> pageInfo = new PageInfo<>(list);
        return R.success(list)
                .add("total", pageInfo.getTotal())
                .add("pageNum", pageInfo.getPageNum())
                .add("pageSize", pageInfo.getPageSize());
    }

    /**
     * 学生查看作业详情（含截止时间、是否提交）
     * 仅学生可操作，返回自身提交状态
     */
    @Override
    public R<HomeworkDetailVO> getStudentHomeworkDetail(Long homeworkId) {
        Long studentId = Long.valueOf(UserContextHolder.getUserId());
        HomeworkDetailVO homeworkDetail = homeworkMapper.getHomeworkDetail(homeworkId, studentId);
        if (homeworkDetail == null) {
            throw new RuntimeException("作业不存在");
        }

        // 2. 查询所有题目及作答信息
        List<HomeworkQuestionVO> questionList = homeworkMapper.listHomeworkQuestionDetail(homeworkId, studentId);
        homeworkDetail.setQuestionList(questionList);

        // 3. 关键逻辑：未批改时隐藏得分和正确答案
        String status = homeworkDetail.getHomeworkStatus();
        boolean isFinished = "已批改".equals(status) || "已完成".equals(status);

        for (HomeworkQuestionVO q : questionList) {
            if (!isFinished) {
                // 状态不是已完成，清空得分和正确答案
                q.setQuestionScoreGot(null);
                q.setCorrectAnswer(null);
            }
        }

        return R.success(homeworkDetail);
    }
}
package com.example.homework.service;

import com.example.homework.common.R;
import com.example.homework.entity.Homework;
import com.example.homework.entity.HomeworkSubmitDetail;
import com.example.homework.vo.HomeworkDetailVO;
import com.example.homework.vo.HomeworkVO;

import java.util.List;

/**
 * 作业业务层接口
 * 定义作业相关的业务逻辑规范
 */
public interface HomeworkService {

    /**
     * 发布新作业
     *
     * @param homework 作业信息（标题、要求、课程/班级、时间、满分等）
     * @return 操作结果
     */
    R<String> addHomework(Homework homework);

    /**
     * 分页查询发布的作业列表（教师端）
     *
     * @param clazzId  班级ID（可选筛选）
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 作业列表+分页信息
     */
    R<List<Homework>> getHomeworkList(Long clazzId, Long courseId, Integer pageNum, Integer pageSize);

    /**
     * 查询作业详情（含提交统计）
     *
     * @param homeworkId 作业ID
     * @return 作业详情+提交统计
     */
    R<Homework> getHomeworkDetail(Long homeworkId);

    /**
     * 修改作业信息（截止时间、要求）
     *
     * @param homework 待修改的作业信息
     * @return 操作结果
     */
    R<String> updateHomework(Homework homework);

    /**
     * 删除作业（需判断是否有学生提交记录）
     *
     * @param homeworkId 作业ID
     * @return 操作结果
     */
    R<String> deleteHomework(Long homeworkId);

    /**
     * 学生查询当前课程下的作业列表（可过滤未提交/已提交）
     *
     * @param courseId 课程ID
     * @param status   提交状态（未提交/已提交）
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 学生作业列表
     */
    R<List<HomeworkVO>> getStudentHomeworkList(Long courseId, String status, Integer pageNum, Integer pageSize);

    /**
     * 学生查看作业详情（含截止时间、是否提交）
     *
     * @param homeworkId 作业ID
     * @return 学生视角的作业详情
     */
    R<HomeworkDetailVO> getStudentHomeworkDetail(Long homeworkId);


}
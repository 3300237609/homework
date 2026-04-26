package com.example.homework.mapper;

import com.example.homework.entity.Homework;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HomeworkMapper {
    List<Homework> selectList();
    Homework selectById(Long id);
    int insert(Homework homework);
    int update(Homework homework);
    int updateTotalScore(Homework homework);
    int delete(Long id);
    //按条件查询教师发布的作业列表
    List<Homework> selectListByCondition(@Param("teacherId") Long teacherId,
                                         @Param("clazzId") Long clazzId);

    //查询学生视角的作业列表（过滤提交状态）
    List<Homework> selectStudentHomeworkList(@Param("studentId") Long studentId,
                                             @Param("courseId") Long courseId,
                                             @Param("status") String status);
}
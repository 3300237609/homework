package com.example.homework.mapper;

import com.example.homework.entity.HomeworkSubmit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HomeworkSubmitMapper {
    List<HomeworkSubmit> selectByHomeworkId(Long homeworkId);
    HomeworkSubmit selectById(Long id);
    int insert(HomeworkSubmit submit);
    int updateScore(HomeworkSubmit submit);
    HomeworkSubmit selectByHomeworkIdAndStudentId(@Param("homeworkId") Long homeworkId,
                                                  @Param("studentId") Long studentId);

}
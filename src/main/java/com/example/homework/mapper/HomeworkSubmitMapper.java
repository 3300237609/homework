package com.example.homework.mapper;

import com.example.homework.entity.HomeworkSubmit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HomeworkSubmitMapper {
    List<HomeworkSubmit> selectByHomeworkId(Long homeworkId);


    int insert(HomeworkSubmit submit);

    HomeworkSubmit selectByHomeworkAndStudent(
            @Param("homeworkId") Long homeworkId,
            @Param("studentId") Long studentId
    );

    void updateById(HomeworkSubmit homeworkSubmit);

}
package com.example.homework.mapper;

import com.example.homework.entity.HomeworkSubmitDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param; // 👈 这个才对！

import java.util.List;

@Mapper
public interface HomeworkSubmitDetailMapper {

    List<HomeworkSubmitDetail> selectBySubmitId(Long submitId);

    int insert(HomeworkSubmitDetail detail);

    void updateById(HomeworkSubmitDetail detail);

    int updateScore(HomeworkSubmitDetail detail);

    // MyBatis 专用 @Param
    HomeworkSubmitDetail selectBySubmitAndQuestion(
            @Param("submitId") Long submitId,
            @Param("questionId") Long questionId
    );
}
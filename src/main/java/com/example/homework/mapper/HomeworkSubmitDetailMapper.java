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

    HomeworkSubmitDetail selectBySubmitAndQuestion(
            @Param("submitId") Long submitId,
            @Param("questionId") Long questionId
    );
    // 根据 submitId 和 questionId 查询详情ID
    Long getDetailId(@Param("submitId") Long submitId,
                     @Param("questionId") Long questionId);

    // 更新题目得分
    int updateDetailScore(@Param("detailId") Long detailId,
                          @Param("score") Integer score);
}
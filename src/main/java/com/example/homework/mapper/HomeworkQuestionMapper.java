package com.example.homework.mapper;

import com.example.homework.entity.HomeworkQuestion;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface HomeworkQuestionMapper {
    List<HomeworkQuestion> selectByHomeworkId(Long homeworkId);
    int insert(HomeworkQuestion homeworkQuestion);
    int deleteByHomeworkId(Long homeworkId);
    List<HomeworkQuestion> selectByQuestionId(Long questionId);
}
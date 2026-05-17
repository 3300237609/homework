package com.example.homework.mapper;

import com.example.homework.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface QuestionMapper {
    List<Question> selectList();
    Question selectById(Long id);
    int insert(Question question);
    int update(Question question);
    int delete(Long id);
    List<Question> selectListByCondition(@Param("courseId") Long courseId,
                                         @Param("type") String type,
                                         @Param("keyword") String keyword,
                                         @Param("difficulty") String difficulty);
    List<Question> selectListByCourseId(Long courseId);
}
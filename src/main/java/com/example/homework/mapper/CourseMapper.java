package com.example.homework.mapper;

import com.example.homework.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
@Mapper
public interface CourseMapper {
    List<Course> list(  @Param("teacherId") Long teacherId,
                        @Param("clazzId") Long clazzId);
    void add(Course course);
    void update(Course course);
    boolean hasHomework(Long id);
    void delete(Long id);
}
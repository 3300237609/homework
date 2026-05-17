package com.example.homework.mapper;

import com.example.homework.entity.Course;
import com.example.homework.vo.CourseVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CourseMapper {
    void add(Course course);

    void update(Course course);

    boolean hasHomework(Long id);

    void delete(Long id);
    int deleteCourse(Long id);

    List<CourseVo> list(
            @Param("teacherId") Long teacherId,
            @Param("clazzId") Long clazzId,
            @Param("pageSize") Integer pageSize,
            @Param("offset") Integer offset
    );

    Integer countCourse(
            @Param("teacherId") Long teacherId,
            @Param("clazzId") Long clazzId
    );
    List<CourseVo> listAllCourseName();

    int addCourse(Course course);
}
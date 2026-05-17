package com.example.homework.service;

import com.example.homework.common.R;
import com.example.homework.entity.Course;
import com.example.homework.vo.CourseVo;

import java.util.List;

public interface CourseService {
    R<List<CourseVo>> list(Long teacherId, Long clazzId, Integer pageSize, Integer pageNum);

    R<String> add(Course course);

    R<String> update(Course course);

    R<String> delete(Long id);

    List<CourseVo> listAllCourseName();

    R<String> addCourse(Course course);
    R<String> deleteCourse(Long id);
}
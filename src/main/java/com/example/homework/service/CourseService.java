package com.example.homework.service;

import com.example.homework.common.R;
import com.example.homework.entity.Course;

import java.util.List;

public interface CourseService {
    R<List<Course>> list(Long teacherId, Long clazzId);
    R<String> add(Course course);
    R<String> update(Course course);
    R<String> delete(Long id);
}
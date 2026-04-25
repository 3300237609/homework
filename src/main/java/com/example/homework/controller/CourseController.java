package com.example.homework.controller;

import com.example.homework.common.R;
import com.example.homework.entity.Course;
import com.example.homework.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/course")
public class CourseController {

    @Autowired
    private CourseService courseService;

    // 列表
    @GetMapping("/list")
    public R<List<Course>> list(@RequestParam(required = false) Long teacherId,
                                @RequestParam(required = false) Long clazzId) {
        return courseService.list(teacherId, clazzId);
    }

    // 新增
    @PostMapping("/add")
    public R<String> add(@RequestBody Course course) {
        return courseService.add(course);
    }

    // 修改
    @PutMapping("/update")
    public R<String> update(@RequestBody Course course) {
        return courseService.update(course);
    }

    // 删除
    @DeleteMapping("/delete/{id}")
    public R<String> delete(@PathVariable Long id) {
        return courseService.delete(id);
    }

}

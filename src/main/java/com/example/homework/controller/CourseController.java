package com.example.homework.controller;

import com.example.homework.common.R;
import com.example.homework.entity.Course;
import com.example.homework.service.CourseService;
import com.example.homework.vo.CourseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/course")
public class CourseController {

    @Autowired
    private CourseService courseService;

    /**
     * 课程列表（分页，可按教师/班级筛选）
     * 请求方式：GET
     * 请求路径：/course/list
     * 请求参数：teacherId（教师ID，可选）、clazzId（班级ID，可选）、pageNum（页码，默认1）、pageSize（每页条数，默认10）
     * 返回值：课程列表
     */
    @GetMapping("/list")
    public R<List<CourseVo>> list(
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) Long clazzId,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "1") Integer pageNum) {

        return courseService.list(teacherId, clazzId, pageSize, pageNum);
    }

    /**
     * 新增课程
     * 请求方式：POST
     * 请求路径：/course/add
     * 请求体：Course对象（课程名、教师ID、班级ID等）
     * 返回值：操作结果
     */
    @PostMapping("/add")
    public R<String> add(@RequestBody Course course) {
        return courseService.add(course);
    }

    /**
     * 修改课程信息
     * 请求方式：PUT
     * 请求路径：/course/update
     * 请求体：Course对象（ID、课程名等）
     * 返回值：操作结果
     */
    @PutMapping("/update")
    public R<String> update(@RequestBody Course course) {
        return courseService.update(course);
    }

    /**
     * 删除课程
     * 请求方式：DELETE
     * 请求路径：/course/delete/{id}
     * 路径参数：id（课程ID）
     * 返回值：操作结果
     */
    @DeleteMapping("/delete/{id}")
    public R<String> delete(@PathVariable Long id) {
        return courseService.delete(id);
    }

    /**
     * 获取所有课程名称列表（用于下拉选择等场景）
     * 请求方式：GET
     * 请求路径：/course/listAllName
     * 返回值：课程名称列表
     */
    @GetMapping("/listAllName")
    public R<List<CourseVo>> listAllCourseName() {
        return R.success(courseService.listAllCourseName());
    }
    /**
     * 新增课程（绑定教师与班级）
     * 请求方式：POST
     * 请求路径：/course/addCourse
     * 请求体：Course对象（课程名、教师ID、班级ID等）
     * 返回值：操作结果
     */
    @PostMapping("/addCourse")
    public R<String> addCourse(@RequestBody Course course) {
        return courseService.addCourse(course);
    }
    /**
     * 删除课程（绑定关系）
     * 请求方式：DELETE
     * 请求路径：/course/deleteCourse/{id}
     * 路径参数：id（课程ID）
     * 返回值：操作结果
     */
    @DeleteMapping("/deleteCourse/{id}")
    public R<String> deleteCourse(@PathVariable Long id) {
        return courseService.deleteCourse(id);
    }

}

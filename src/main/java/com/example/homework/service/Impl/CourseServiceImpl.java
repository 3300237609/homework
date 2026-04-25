package com.example.homework.service.Impl;

import com.example.homework.common.R;
import com.example.homework.entity.Course;
import com.example.homework.mapper.CourseMapper;
import com.example.homework.service.CourseService;
import com.example.homework.utils.PermissionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CourseServiceImpl implements CourseService {

    @Autowired
    private CourseMapper courseMapper;

    // ====================== 列表 ======================
    @Override
    public R<List<Course>> list(Long teacherId, Long clazzId) {
        if (!PermissionUtil.isAdminOrTeacher()) {
            return R.error("权限不足");
        }
        List<Course> list = courseMapper.list(teacherId, clazzId);
        return R.success(list);
    }

    // ====================== 新增 ======================
    @Override
    public R<String> add(Course course) {
        if (!PermissionUtil.isAdmin()) {
            return R.error("权限不足");
        }
        course.setCreateTime(LocalDateTime.now());
        course.setUpdateTime(LocalDateTime.now());
        courseMapper.add(course);
        return R.success("新增课程成功");
    }

    // ====================== 修改 ======================
    @Override
    public R<String> update(Course course) {
        if (!PermissionUtil.isAdmin()) {
            return R.error("权限不足");
        }
        course.setUpdateTime(LocalDateTime.now());
        courseMapper.update(course);
        return R.success("修改课程成功");
    }

    // ====================== 删除 ======================
    @Override
    public R<String> delete(Long id) {
        if (!PermissionUtil.isAdmin()) {
            return R.error("权限不足");
        }

        // 检查是否有关联作业
        boolean has = courseMapper.hasHomework(id);
        if (has) {
            return R.error("该课程存在关联作业，无法删除");
        }

        courseMapper.delete(id);
        return R.success("删除课程成功");
    }
}
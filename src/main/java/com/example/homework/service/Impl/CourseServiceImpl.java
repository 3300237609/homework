package com.example.homework.service.Impl;

import com.example.homework.common.R;
import com.example.homework.entity.Course;
import com.example.homework.mapper.CourseMapper;
import com.example.homework.service.CourseService;
import com.example.homework.utils.PermissionUtil;
import com.example.homework.vo.CourseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
public  class CourseServiceImpl implements CourseService {

    @Autowired
    private CourseMapper courseMapper;

    @Override
    public R<List<CourseVo>> list(Long teacherId, Long clazzId, Integer pageSize, Integer pageNum) {
        if (!PermissionUtil.isAdminOrTeacher()) {
            return R.error("权限不足");
        }

        // 分页参数默认值
        if (pageNum == null) pageNum = 1;
        if (pageSize == null) pageSize = 10;

        // 计算偏移量
        Integer offset = (pageNum - 1) * pageSize;

        // 查询列表（现在返回 CourseVo）
        List<CourseVo> list = courseMapper.list(teacherId, clazzId, pageSize, offset);

        // 查询总数
        Integer total = courseMapper.countCourse(teacherId, clazzId);

        // 按统一格式返回 + 分页信息
        return R.success(list)
                .add("total", total)
                .add("pageNum", pageNum)
                .add("pageSize", pageSize);
    }

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

    @Override
    public R<String> update(Course course) {
        if (!PermissionUtil.isAdmin()) {
            return R.error("权限不足");
        }
        course.setUpdateTime(LocalDateTime.now());
        courseMapper.update(course);
        return R.success("修改课程成功");
    }

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

    @Override
    public List<CourseVo> listAllCourseName() {
        return courseMapper.listAllCourseName();
    }

    @Override
    public R<String> addCourse(Course course) {
        // 非空判断
        if (course.getCourseName() == null || course.getCourseName().trim().isEmpty()) {
            return R.error("课程名称不能为空");
        }

        // 自动填充时间
        course.setCreateTime(LocalDateTime.now());
        course.setUpdateTime(LocalDateTime.now());

        // 插入 course 表
        int rows = courseMapper.addCourse(course);
        if (rows <= 0) {
            return R.error("新增课程失败");
        }

        return R.success("新增课程成功");
    }
    @Override
    public R<String> deleteCourse(Long id) {
        int rows = courseMapper.deleteCourse(id);
        if (rows == 0) {
            return R.error("删除失败，课程不存在");
        }
        return R.success("删除成功");
    }
}
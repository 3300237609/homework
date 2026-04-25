package com.example.homework.service;

import com.example.homework.common.R;
import com.example.homework.entity.Clazz;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.List;

public interface ClazzService {
    // 分页查询所有班级 可根据teacher_id进行筛选
    R<List<Clazz>> getClazzList(Long teacherId, Integer pageSize, Integer offset);

    // 根据id查询班级
    R<Clazz> getClazzById(Long id);

    // 添加新班级
    R<String> addClazz(Clazz clazz);

    // 修改班级信息
    R<String> updateClazz(Clazz clazz);

    // 根据id删除班级
    R<String> deleteClazz(Long id);

}

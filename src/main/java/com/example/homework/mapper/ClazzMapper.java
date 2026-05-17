package com.example.homework.mapper;

import com.example.homework.entity.Clazz;
import com.example.homework.vo.ClazzVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ClazzMapper {

    // 分页查询所有班级 可根据teacher_id进行筛选
    List<ClazzVo> getClazzList(@Param("teacherId") Long teacherId,
                               @Param("pageSize") Integer pageSize,
                               @Param("offset") Integer offset);

    // 根据id查询班级
    Clazz getClazzById(@Param("id") Long id);

    // 添加新班级
    boolean addClazz(Clazz clazz);

    // 修改班级信息
    boolean updateClazz(Clazz clazz);

    // 根据id删除班级
    boolean deleteClazz(@Param("id") Long id);
    Integer getClazzCount(
            @Param("teacherId") Long teacherId
    );
}
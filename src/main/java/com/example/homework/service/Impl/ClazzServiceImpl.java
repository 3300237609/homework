package com.example.homework.service.Impl;

import com.example.homework.common.R;
import com.example.homework.entity.Clazz;
import com.example.homework.mapper.ClazzMapper;
import com.example.homework.mapper.UserMapper;
import com.example.homework.service.ClazzService;
import com.example.homework.vo.ClazzVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class ClazzServiceImpl implements ClazzService {
    @Autowired
    ClazzMapper clazzMapper;
    @Autowired
    UserMapper userMapper;
    @Override
    public R<List<ClazzVo>> getClazzList(Long teacherId, Integer pageSize, Integer pageNum) {

        // 分页参数默认值
        if (pageNum == null) pageNum = 1;
        if (pageSize == null) pageSize = 10;

        // 提前计算偏移量：(页码-1)*每页条数
        Integer offset = (pageNum - 1) * pageSize;

        // 查询列表
        List<ClazzVo> clazzList = clazzMapper.getClazzList(teacherId, pageSize, offset);

        // 查询总数
        Integer total = clazzMapper.getClazzCount(teacherId);

        // 按统一格式返回：list + 分页信息
        return R.success(clazzList)
                .add("total", total)
                .add("pageNum", pageNum)
                .add("pageSize", pageSize);
    }
    @Override
    public R<Clazz> getClazzById(Long id) {
        return  R.success(clazzMapper.getClazzById(id));
    }

    @Override
    public R<String> addClazz(Clazz clazz) {
        if (clazz.getClazzName().isEmpty() || clazz.getTeacherId()==null) {
            return R.error("添加失败！数据异常");
        }
        //查询教师id是否存在
        if (!userMapper.checkIsTeacherById(clazz.getTeacherId())){
            return R.error("添加失败！教师不存在");
        }

        return clazzMapper.addClazz(clazz) ? R.success("添加成功！") : R.error("添加失败！");
    }

    @Override
    public R<String> updateClazz(Clazz clazz) {

        if (clazz.getTeacherId() != null) {
            //判断教师是否存在
            if (!userMapper.checkIsTeacherById(clazz.getTeacherId())){
                return R.error("修改失败！教师不存在");
            }

        }
        return clazzMapper.updateClazz(clazz) ? R.success("修改成功！") : R.error("修改失败！");
    }

    @Override
    public R<String> deleteClazz(Long id) {
        //判断是否有学生关联


        //判断是否有作业关联


        return clazzMapper.deleteClazz(id) ? R.success("删除成功！") : R.error("删除失败！");
    }


}

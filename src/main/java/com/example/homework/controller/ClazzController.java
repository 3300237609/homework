package com.example.homework.controller;

import com.example.homework.common.R;
import com.example.homework.entity.Clazz;
import com.example.homework.service.ClazzService;
import com.example.homework.vo.ClazzVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clazz")
public class ClazzController {
    @Autowired
    ClazzService clazzService;

    /**
     * 获取班级列表（分页+筛选）
     * 请求方式：GET
     * 请求路径：/clazz/list
     * 请求参数：teacherId（教师ID，可选）、pageNum（页码，默认1）、pageSize（每页条数，默认10）
     * 返回值：班级列表
     */
    @GetMapping("/list")
    public R<List<ClazzVo>> getClazzList(
            @RequestParam(required = false, defaultValue = "") Long teacherId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ){
        return clazzService.getClazzList(teacherId, pageSize, pageNum);
    };

    /**
     * 新增班级（绑定班主任ID）
     * 请求方式：POST
     * 请求路径：/clazz/add
     * 请求体：Clazz对象（班级名、班主任ID等）
     * 返回值：操作结果
     */
    @PostMapping("/add")
    public R<String> addClazz(@RequestBody Clazz clazz){
        return clazzService.addClazz(clazz);
    }

    /**
     * 修改班级信息（调整班主任/班级名）
     * 请求方式：PUT
     * 请求路径：/clazz/update
     * 请求体：Clazz对象（ID、班级名、班主任ID）
     * 返回值：操作结果
     */
    @PutMapping("/update")
    public R<String> updateClazz(@RequestBody Clazz clazz){
        return clazzService.updateClazz(clazz);
    }

    /**
     * 删除班级（需判断是否有课程/学生关联）
     * 请求方式：DELETE
     * 请求路径：/clazz/delete/{id}
     * 路径参数：id（班级ID）
     * 返回值：操作结果
     */
    @DeleteMapping("/delete/{id}")
    public  R<String> deleteClazz(@PathVariable Long id){
        return clazzService.deleteClazz(id);
    }

}

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

    //GET /list 获取班级列表 分页+筛选
    @GetMapping("/list")
    public R<List<ClazzVo>> getClazzList(
            @RequestParam(required = false, defaultValue = "") Long teacherId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ){
        return clazzService.getClazzList(teacherId, pageSize, pageNum);
    };

    //POST /add 新增班级 绑定班主任id
    @PostMapping("/add")
    public R<String> addClazz(@RequestBody Clazz clazz){
        return clazzService.addClazz(clazz);
    }

    //PUT /update 修改班级信息 调整班主任/班级名
    @PutMapping("/update")
    public R<String> updateClazz(@RequestBody Clazz clazz){
        return clazzService.updateClazz(clazz);
    }

    //DELETE /delete/{id} 删除班级 需判断是否有课程/学生关联
    @DeleteMapping("/delete/{id}")
    public  R<String> deleteClazz(@PathVariable Long id){
        return clazzService.deleteClazz(id);
    }

}

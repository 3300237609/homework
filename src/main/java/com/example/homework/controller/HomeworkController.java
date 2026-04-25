package com.example.homework.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/homework")
public class HomeworkController {
    //POST /add 发布新作业（标题、要求、课程/班级、时间、满分）--教师

    //GET /list 查询发布的作业列表（分页） --教师

    //GET /detail/{id} 作业详情（含提交统计） --老师/学生

    //PUT /update 修改作业信息（截止时间、要求） --教师

    //DELETE /delete/{id} 删除作业（需判断判断是否有学生提交记录） --教师

    //GET /student/list 学生当前课程下的作业列表（可过滤 未提交/已提交） --学生

    //GET /student/detail/{id} 学生查看作业详情（含截止时间、是否提交） --学生
}
